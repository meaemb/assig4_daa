package com.meaemb.graph.dagsp;

import com.meaemb.metrics.SimpleMetrics;
import com.meaemb.model.Graph;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DagShortestPathsTest {
    @Test
    void sssp() {
        Graph g = new Graph(4, true);
        g.addEdge(0,1,2); g.addEdge(0,2,1); g.addEdge(2,3,2); g.addEdge(1,3,3);
        List<Integer> topo = Arrays.asList(0,1,2,3);
        DagShortestPaths dsp = new DagShortestPaths(g, new SimpleMetrics());
        DagShortestPaths.Result res = dsp.shortest(0, topo);
        assertEquals(3, res.dist[3]);
    }
}
