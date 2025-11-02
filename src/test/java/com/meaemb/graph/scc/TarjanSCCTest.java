package com.meaemb.graph.scc;

import com.meaemb.metrics.SimpleMetrics;
import com.meaemb.model.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TarjanSCCTest {
    @Test
    void simpleCycle() {
        Graph g = new Graph(3, true);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1);
        TarjanSCC r = new TarjanSCC(g, new SimpleMetrics());
        TarjanSCC.Result res = r.run();
        assertEquals(1, res.compCount);
        assertEquals(3, res.components.get(0).size());
    }
}
