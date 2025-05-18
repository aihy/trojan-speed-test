package com.hhwyz.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.Map;

public class SpeedTestResult {
    public final String name;
    public final String type;
    public final String server;
    public final int port;
    public final Map<String, Long> latencies;
    public final double averageLatency;

    public SpeedTestResult(String name, String type, String server, int port, Map<String, Long> latencies) {
        this.name = name;
        this.type = type;
        this.server = server;
        this.port = port;
        this.latencies = latencies;
        
        // 计算平均延迟，忽略超时的值
        long validCount = latencies.values().stream()
                .filter(l -> l != Long.MAX_VALUE)
                .count();
        
        if (validCount == 0) {
            this.averageLatency = Long.MAX_VALUE;
        } else {
            this.averageLatency = latencies.values().stream()
                    .filter(l -> l != Long.MAX_VALUE)
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(Long.MAX_VALUE);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }

    private String formatLatency(Long latency) {
        if (latency == null || latency == Long.MAX_VALUE) {
            return "超时";
        }
        return latency + "ms";
    }
    
    public StringProperty getLatencyProperty(String url) {
        return new SimpleStringProperty(formatLatency(latencies.get(url)));
    }
    
    public StringProperty getAverageLatencyProperty() {
        return new SimpleStringProperty(averageLatency == Long.MAX_VALUE ? "超时" : String.format("%.0fms", averageLatency));
    }
}
