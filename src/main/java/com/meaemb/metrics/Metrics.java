package com.meaemb.metrics;

import java.util.Map;

public interface Metrics {
    void start();
    void stop();
    long elapsedNanos();

    void inc(String key, long delta);
    void inc(String key);
    long get(String key);
    Map<String, Long> snapshot();
}
