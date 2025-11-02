package com.meaemb.graph.scc;

import com.meaemb.metrics.Metrics;
import com.meaemb.model.Edge;
import com.meaemb.model.Graph;

import java.util.*;

public class TarjanSCC {
    private final Graph g;
    private final Metrics m;

    private int time = 0, compCount = 0;
    private final int[] disc, low, compId;
    private final boolean[] inStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final List<List<Integer>> components = new ArrayList<>();

    public TarjanSCC(Graph g, Metrics metrics) {
        this.g = g;
        this.m = metrics;
        disc = new int[g.n];
        low = new int[g.n];
        compId = new int[g.n];
        inStack = new boolean[g.n];
        Arrays.fill(disc, -1);
        Arrays.fill(low, -1);
        Arrays.fill(compId, -1);
    }

    public Result run() {
        m.start();
        for (int v = 0; v < g.n; v++) {
            if (disc[v] == -1) dfs(v);
        }
        m.stop();
        return new Result(components, compId, compCount);
    }

    private void dfs(int u) {
        m.inc("scc.dfs.visits");
        disc[u] = low[u] = time++;
        stack.push(u);
        inStack[u] = true;

        for (Edge e : g.adj.get(u)) {
            m.inc("scc.dfs.edges");
            int v = e.to;
            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (inStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) { // root of SCC
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int v = stack.pop();
                inStack[v] = false;
                compId[v] = compCount;
                comp.add(v);
                if (v == u) break;
            }
            components.add(comp);
            compCount++;
        }
    }

    public static class Result {
        public final List<List<Integer>> components; // compId -> list of vertices
        public final int[] compIdOfVertex;           // vertex -> compId
        public final int compCount;

        public Result(List<List<Integer>> components, int[] compIdOfVertex, int compCount) {
            this.components = components;
            this.compIdOfVertex = compIdOfVertex;
            this.compCount = compCount;
        }
    }
}
