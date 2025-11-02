package com.meaemb.graph.scc;

import com.meaemb.model.Edge;
import com.meaemb.model.Graph;

import java.util.*;

public class CondensationBuilder {
    public static Graph build(Graph g, TarjanSCC.Result scc) {
        Graph dag = new Graph(scc.compCount, true);
        Set<Long> seen = new HashSet<>();
        for (int u = 0; u < g.n; u++) {
            int cu = scc.compIdOfVertex[u];
            for (Edge e : g.adj.get(u)) {
                int cv = scc.compIdOfVertex[e.to];
                if (cu != cv) {
                    long key = (((long) cu) << 32) ^ cv;
                    if (seen.add(key)) {
                        dag.addEdge(cu, cv, 0);
                    }
                }
            }
        }
        return dag;
    }
}
