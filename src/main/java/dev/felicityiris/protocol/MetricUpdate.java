package dev.felicityiris.protocol;

import java.util.Map;

public class MetricUpdate {
    private final String type = "metrics";
    private final long timestamp;
    private final Map<String, Double> data;

    public MetricUpdate(long timestamp, Map<String, Double> data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Double> getData() {
        return data;
    }
}
