package com.meaemb.io;

import java.util.List;

public class GraphJson {
    public boolean directed;
    public int n;
    public List<EdgeJson> edges;
    public Integer source;      // может отсутствовать
    public String weight_model; // "edge" или "node" (мы используем "edge")

    public static class EdgeJson {
        public int u;
        public int v;
        public int w;
    }
}
