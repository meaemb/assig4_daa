package com.meaemb.graph.topo;

import com.meaemb.metrics.Metrics;
import com.meaemb.model.Edge;
import com.meaemb.model.Graph;

import java.util.*;

public class KahnTopo {
    private final Graph dag;
    private final Metrics m;

    public KahnTopo(Graph dag, Metrics metrics) {
        this.dag = dag;
        this.m = metrics;
    }

    public List<Integer> order() {
        int[] indeg = new int[dag.n];
        for (int u = 0; u < dag.n; u++) {
            for (Edge e : dag.adj.get(u)) indeg[e.to]++;
        }

        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < dag.n; v++) if (indeg[v] == 0) {
            q.add(v);
            m.inc("topo.pushes");
        }

        m.start();
        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.removeFirst();
            m.inc("topo.pops");
            order.add(u);
            for (Edge e : dag.adj.get(u)) {
                int v = e.to;
                if (--indeg[v] == 0) {
                    q.add(v);
                    m.inc("topo.pushes");
                }
            }
        }
        m.stop();

        if (order.size() != dag.n)
            throw new IllegalStateException("Graph is not a DAG (cycle detected in condensation)");
        return order;
    }
}
