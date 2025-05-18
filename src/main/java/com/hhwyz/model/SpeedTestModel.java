package com.hhwyz.model;

import com.hhwyz.model.SpeedTestResult;
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
        FXCollections.sort(results, (a, b) -> Double.compare(a.averageLatency, b.averageLatency));
    }
}
