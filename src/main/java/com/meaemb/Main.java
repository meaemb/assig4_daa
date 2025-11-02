package com.meaemb;

import com.meaemb.graph.dagsp.DagShortestPaths;
import com.meaemb.graph.scc.CondensationBuilder;
import com.meaemb.graph.scc.TarjanSCC;
import com.meaemb.graph.topo.KahnTopo;
import com.meaemb.io.GraphReader;
import com.meaemb.metrics.Metrics;
import com.meaemb.metrics.SimpleMetrics;
import com.meaemb.model.Edge;
import com.meaemb.model.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // Always resolve paths relative to project root (not /src)
        File currentDir = new File(System.getProperty("user.dir"));
        File projectRoot = currentDir.getName().equals("src") ? currentDir.getParentFile() : currentDir;

        File dataDir = new File(projectRoot, "data");
        File outDir = new File(projectRoot, "out");

        if (!outDir.exists()) outDir.mkdirs();

        File[] jsons = dataDir.listFiles((d, name) -> name.endsWith(".json"));
        if (jsons == null || jsons.length == 0) {
            System.out.println("Put your datasets into /data as *.json (in project root)");
            return;
        }

        for (File f : jsons) runOnce(f, outDir);
        System.out.println("Done. See reports in " + outDir.getAbsolutePath());
    }

    private static void runOnce(File json, File outDir) throws Exception {
        GraphReader.LoadedGraph lg = GraphReader.load(json);
        Graph g = lg.graph;
        int source = lg.source;

        // 1) Strongly Connected Components (Tarjan)
        Metrics sccM = new SimpleMetrics();
        TarjanSCC scc = new TarjanSCC(g, sccM);
        TarjanSCC.Result sccRes = scc.run();

        // 2) Condensation DAG (unweighted)
        Graph dag = CondensationBuilder.build(g, sccRes);

        // 3) Topological order
        Metrics topoM = new SimpleMetrics();
        KahnTopo topo = new KahnTopo(dag, topoM);
        List<Integer> order = topo.order();

        // 4) Weighted DAG (each edge = weight 1, for demo purposes)
        Graph dagWeighted = toWeightedByCount(dag);

        // 5) Shortest paths
        Metrics spM = new SimpleMetrics();
        DagShortestPaths sp = new DagShortestPaths(dagWeighted, spM);
        DagShortestPaths.Result shortest = sp.shortest(findComponentOf(source, sccRes), order);

        // Find target component with max distance
        int bestT = 0;
        for (int v = 0; v < dagWeighted.n; v++) {
            if (shortest.dist[v] < Long.MAX_VALUE / 4 && shortest.dist[v] > shortest.dist[bestT])
                bestT = v;
        }

        // 6) Longest (critical) path
        Metrics lpM = new SimpleMetrics();
        DagShortestPaths sp2 = new DagShortestPaths(dagWeighted, lpM);
        DagShortestPaths.Result longest = sp2.longest(findComponentOf(source, sccRes), order);
        int bestL = 0;
        for (int v = 0; v < dagWeighted.n; v++) {
            if (longest.dist[v] > longest.dist[bestL]) bestL = v;
        }

        // 7) Write report
        File out = new File(outDir, json.getName().replace(".json", ".txt"));
        try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            w.write("DATASET: " + json.getName() + "\n");
            w.write("n=" + g.n + ", directed=" + g.directed + ", source=" + source + ", weight_model=edge\n\n");

            // SCC
            w.write("SCC components: " + sccRes.compCount + "\n");
            for (int cid = 0; cid < sccRes.components.size(); cid++) {
                w.write("  C" + cid + " size=" + sccRes.components.get(cid).size() +
                        " vertices=" + sccRes.components.get(cid) + "\n");
            }
            w.write("SCC time(ns)=" + ((SimpleMetrics) sccM).elapsedNanos() +
                    " visits=" + sccM.get("scc.dfs.visits") +
                    " edges=" + sccM.get("scc.dfs.edges") + "\n\n");

            // Topo
            w.write("Condensation DAG: V=" + dag.n + "\n");
            w.write("Topo order: " + order + "\n");
            w.write("Topo time(ns)=" + ((SimpleMetrics) topoM).elapsedNanos() +
                    " pushes=" + topoM.get("topo.pushes") +
                    " pops=" + topoM.get("topo.pops") + "\n\n");

            // Shortest
            w.write("DAG-SP (shortest) from comp(source)=" + findComponentOf(source, sccRes) + "\n");
            w.write("Distances: " + Arrays.toString(shortest.dist) + "\n");
            List<Integer> pathS = DagShortestPaths.buildPath(bestT, shortest.parent);
            w.write("One shortest path to comp " + bestT + ": " + pathS + " , length=" + shortest.dist[bestT] + "\n");
            w.write("SP relaxations=" + spM.get("dagsp.relaxations") +
                    " time(ns)=" + ((SimpleMetrics) spM).elapsedNanos() + "\n\n");

            // Longest
            w.write("Critical path (longest) from comp(source)\n");
            List<Integer> pathL = DagShortestPaths.buildPath(bestL, longest.parent);
            w.write("Longest path to comp " + bestL + ": " + pathL +
                    " , length=" + longest.dist[bestL] + "\n");
            w.write("LP relaxations=" + lpM.get("dagsp.relaxations") +
                    " time(ns)=" + ((SimpleMetrics) lpM).elapsedNanos() + "\n");
        }
    }

    private static int findComponentOf(int v, TarjanSCC.Result scc) {
        return scc.compIdOfVertex[v];
    }

    private static Graph toWeightedByCount(Graph dag) {
        Graph g = new Graph(dag.n, true);
        for (int u = 0; u < dag.n; u++) {
            for (Edge e : dag.adj.get(u)) {
                g.addEdge(u, e.to, 1); // simple unit weight; real DAG datasets use edge weights from JSON
            }
        }
        return g;
    }
}
