package dev.felicityiris.metrics;

public interface MetricCollector {
    String getMetricID();
    double collect();
}
