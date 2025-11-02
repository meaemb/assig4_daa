package com.meaemb.graph.topo;

import com.meaemb.metrics.SimpleMetrics;
import com.meaemb.model.Graph;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class KahnTopoTest {
    @Test
    void dagOrder() {
        Graph g = new Graph(4, true);
        g.addEdge(0,1,1); g.addEdge(0,2,1); g.addEdge(1,3,1); g.addEdge(2,3,1);
        KahnTopo kt = new KahnTopo(g, new SimpleMetrics());
        List<Integer> ord = kt.order();
        assertEquals(4, ord.size());
        assertTrue(ord.indexOf(0) < ord.indexOf(3));
    }
}
