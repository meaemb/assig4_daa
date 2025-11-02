package com.meaemb.metrics;

import java.util.*;

public class SimpleMetrics implements Metrics {
    private final Map<String, Long> counters = new LinkedHashMap<>();
    private long t0 = -1, t1 = -1;

    @Override public void start() { t0 = System.nanoTime(); }
    @Override public void stop()  { t1 = System.nanoTime(); }
    @Override public long elapsedNanos() { return (t0 >= 0 && t1 >= 0) ? (t1 - t0) : -1; }

    @Override public void inc(String key, long delta) { counters.put(key, counters.getOrDefault(key,0L)+delta); }
    @Override public void inc(String key) { inc(key,1); }
    @Override public long get(String key) { return counters.getOrDefault(key,0L); }
    @Override public Map<String, Long> snapshot() { return new LinkedHashMap<>(counters); }
}
