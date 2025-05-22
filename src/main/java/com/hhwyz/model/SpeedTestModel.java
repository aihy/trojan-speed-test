package com.hhwyz.model;

import com.hhwyz.SpeedTestResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SpeedTestModel {
    private final ObservableList<SpeedTestResult> results = FXCollections.observableArrayList();
    
    public ObservableList<SpeedTestResult> getResults() {
        return results;
    }
    
    public void addResult(SpeedTestResult result) {
        results.add(result);
        sortResults();
    }
    
    public void clearResults() {
        results.clear();
    }
    
    private void sortResults() {
        FXCollections.sort(results, (a, b) -> {
            // 首先比较平均延迟
            int latencyCompare = Double.compare(a.getAverageLatency(), b.getAverageLatency());
            if (latencyCompare != 0) {
                return latencyCompare;
            }
            // 如果延迟相同，则比较下载速度（降序）
            return Double.compare(b.getDownloadSpeed(), a.getDownloadSpeed());
        });
    }
}
