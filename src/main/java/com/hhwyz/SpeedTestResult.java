package com.hhwyz;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.Map;

public class SpeedTestResult {
    private final String name;
    private final String type;
    private final String server;
    private final int port;
    private final Map<String, Long> latencies;
    private final double averageLatency;
    private final double downloadSpeed; // Mbps

    public SpeedTestResult(String name, String type, String server, int port, Map<String, Long> latencies, double downloadSpeed) {
        this.name = name;
        this.type = type;
        this.server = server;
        this.port = port;
        this.latencies = latencies;
        this.downloadSpeed = downloadSpeed;
        
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

    public double getAverageLatency() {
        return averageLatency;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
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
        return new SimpleStringProperty(averageLatency == Long.MAX_VALUE ? "超时" : String.format("%.0f ms", averageLatency));
    }

    public StringProperty getDownloadSpeedProperty() {
        return new SimpleStringProperty(downloadSpeed == -1 ? "超时" : String.format("%.0f Kbps", downloadSpeed));
    }
}
