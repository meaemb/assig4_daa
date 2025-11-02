package com.meaemb.io;

import java.util.List;

public class GraphJson {
    public boolean directed;
    public int n;
    public List<EdgeJson> edges;
    public Integer source;
    public String weight_model;

    public static class EdgeJson {
        public int u;
        public int v;
        public int w;
    }
}
