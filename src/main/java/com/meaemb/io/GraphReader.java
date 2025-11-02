package com.meaemb.io;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.meaemb.model.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GraphReader {
    private static final Gson GSON = new Gson();

    public static LoadedGraph load(File jsonFile) throws IOException {
        try (InputStream is = new FileInputStream(jsonFile);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             JsonReader jr = new JsonReader(isr)) {

            GraphJson gj = GSON.fromJson(jr, GraphJson.class);
            Graph g = new Graph(gj.n, gj.directed);
            if (gj.edges != null) {
                for (GraphJson.EdgeJson e : gj.edges) {
                    g.addEdge(e.u, e.v, e.w);
                }
            }
            Integer src = gj.source != null ? gj.source : 0;
            String weightModel = gj.weight_model != null ? gj.weight_model : "edge";
            return new LoadedGraph(g, src, weightModel);
        }
    }

    public static class LoadedGraph {
        public final Graph graph;
        public final int source;
        public final String weightModel;

        public LoadedGraph(Graph graph, int source, String weightModel) {
            this.graph = graph;
            this.source = source;
            this.weightModel = weightModel;
        }
    }
}
