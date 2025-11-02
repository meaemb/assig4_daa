package com.meaemb.graph.dagsp;

import com.meaemb.metrics.Metrics;
import com.meaemb.model.Edge;
import com.meaemb.model.Graph;

import java.util.*;

public class DagShortestPaths {
    private final Graph dagWeighted; // исходный DAG (после SCC-компрессии веса на рёбрах можно пронести из исходного графа при необходимости)
    private final Metrics m;

    public DagShortestPaths(Graph dagWeighted, Metrics metrics) {
        this.dagWeighted = dagWeighted;
        this.m = metrics;
    }

    public Result shortest(int src, List<Integer> topoOrder) {
        long[] dist = new long[dagWeighted.n];
        int[] parent = new int[dagWeighted.n];
        Arrays.fill(dist, Long.MAX_VALUE / 4);
        Arrays.fill(parent, -1);
        dist[src] = 0;

        m.start();
        for (int u : topoOrder) {
            if (dist[u] == Long.MAX_VALUE / 4) continue;
            for (Edge e : dagWeighted.adj.get(u)) {
                long nd = dist[u] + e.w;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    parent[e.to] = u;
                    m.inc("dagsp.relaxations");
                }
            }
        }
        m.stop();
        return new Result(dist, parent);
    }

    public Result longest(int src, List<Integer> topoOrder) {
        long[] best = new long[dagWeighted.n];
        int[] parent = new int[dagWeighted.n];
        Arrays.fill(best, Long.MIN_VALUE / 4);
        Arrays.fill(parent, -1);
        best[src] = 0;

        m.start();
        for (int u : topoOrder) {
            if (best[u] == Long.MIN_VALUE / 4) continue;
            for (Edge e : dagWeighted.adj.get(u)) {
                long nd = best[u] + e.w;
                if (nd > best[e.to]) {
                    best[e.to] = nd;
                    parent[e.to] = u;
                    m.inc("dagsp.relaxations");
                }
            }
        }
        m.stop();
        return new Result(best, parent);
    }

    public static List<Integer> buildPath(int target, int[] parent) {
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = parent[v]) path.add(v);
        Collections.reverse(path);
        return path;
    }

    public static class Result {
        public final long[] dist;
        public final int[] parent;
        public Result(long[] dist, int[] parent) { this.dist = dist; this.parent = parent; }
    }
}
