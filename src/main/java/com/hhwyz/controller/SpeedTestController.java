package com.hhwyz.controller;

import com.hhwyz.*;
import com.hhwyz.model.SpeedTestModel;
import com.hhwyz.model.SpeedTestResult;
import com.hhwyz.util.ConfigUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SpeedTestController {
    private static final String SUBSCRIPTION_URL_KEY = "subscription.url";
    private static final List<String> SPEED_TEST_URLS = List.of(
            "https://captive.apple.com",
            "https://cp.cloudflare.com",
            "https://www.google.com/generate_204"
    );
    private static final int TIMEOUT_SECONDS = 10;
    private static final int THREAD_POOL_SIZE = 10;

    @FXML private TableView<SpeedTestResult> resultsTable;
    @FXML private TableColumn<SpeedTestResult, String> nameColumn;
    @FXML private TableColumn<SpeedTestResult, String> typeColumn;
    @FXML private TableColumn<SpeedTestResult, String> appleLatencyColumn;
    @FXML private TableColumn<SpeedTestResult, String> cfLatencyColumn;
    @FXML private TableColumn<SpeedTestResult, String> googleLatencyColumn;
    @FXML private TableColumn<SpeedTestResult, String> avgLatencyColumn;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Button startButton;
    @FXML private TextField subscriptionUrlField;

    private final SpeedTestModel model = new SpeedTestModel();
    private ExecutorService executor;
    private volatile boolean isRunning = false;

    @FXML
    public void initialize() {
        setupTableColumns();
        resultsTable.setItems(model.getResults());
        setupUnixStyleKeyBindings();
        
        // 加载保存的订阅地址
        String savedUrl = ConfigUtil.getProperty(SUBSCRIPTION_URL_KEY);
        if (savedUrl != null && !savedUrl.isEmpty()) {
            subscriptionUrlField.setText(savedUrl);
        }
        
        // 监听订阅地址变化
        subscriptionUrlField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                ConfigUtil.setProperty(SUBSCRIPTION_URL_KEY, newValue);
            }
        });
    }

    private void setupUnixStyleKeyBindings() {
        subscriptionUrlField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case A:
                    if (event.isControlDown()) {
                        subscriptionUrlField.positionCaret(0);
                        event.consume();
                    }
                    break;
                case E:
                    if (event.isControlDown()) {
                        subscriptionUrlField.positionCaret(subscriptionUrlField.getText().length());
                        event.consume();
                    }
                    break;
            }
        });
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        appleLatencyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getLatencyProperty(SPEED_TEST_URLS.get(0)));
        cfLatencyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getLatencyProperty(SPEED_TEST_URLS.get(1)));
        googleLatencyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getLatencyProperty(SPEED_TEST_URLS.get(2)));
        avgLatencyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getAverageLatencyProperty());
            
        // 添加排序功能
        resultsTable.getSortOrder().add(avgLatencyColumn);
    }

    @FXML
    private void startTest() {
        if (isRunning) {
            stopTest();
            return;
        }

        isRunning = true;
        startButton.setText("停止测试");
        model.clearResults();
        resetProgress();
        
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        CompletableFuture.runAsync(() -> {
            try {
                runSpeedTest();
            } catch (Exception e) {
                handleError(e);
            }
        });
    }

    private void stopTest() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        isRunning = false;
        startButton.setText("开始测试");
        statusLabel.setText("测试已停止");
    }

    private void resetProgress() {
        progressBar.setProgress(0);
        statusLabel.setText("正在下载订阅...");
    }

    private void runSpeedTest() throws Exception {
        String subscriptionUrl = subscriptionUrlField.getText().trim();
        if (subscriptionUrl.isEmpty()) {
            throw new Exception("请输入订阅地址");
        }
        
        SubscriptionService subscriptionService = new SubscriptionService(subscriptionUrl);
        ProxyTester proxyTester = new ProxyTester(SPEED_TEST_URLS, TIMEOUT_SECONDS);
        
        List<Map<String, Object>> proxies = subscriptionService.getProxies();
        if (proxies.isEmpty()) {
            throw new Exception("未找到可用节点");
        }

        updateStatus("正在测速...");
        testProxies(proxies, proxyTester);
    }

    private void testProxies(List<Map<String, Object>> proxies, ProxyTester proxyTester) {
        int total = proxies.size();
        int[] completed = {0};
        
        List<CompletableFuture<Void>> futures = proxies.stream()
            .map(proxy -> CompletableFuture.runAsync(() -> {
                if (!isRunning) return;
                SpeedTestResult result = proxyTester.testProxy(proxy);
                updateProgress(completed, total, result);
            }, executor))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenComplete((v, e) -> finishTest(e));
    }

    private void updateProgress(int[] completed, int total, SpeedTestResult result) {
        Platform.runLater(() -> {
            model.addResult(result);
            completed[0]++;
            double progress = (double) completed[0] / total;
            progressBar.setProgress(progress);
            statusLabel.setText(String.format("测速进度: %d/%d", completed[0], total));
        });
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void finishTest(Throwable error) {
        Platform.runLater(() -> {
            if (error == null && isRunning) {
                statusLabel.setText("测速完成");
                progressBar.setProgress(1.0);
            }
            isRunning = false;
            startButton.setText("开始测试");
            if (executor != null) {
                executor.shutdown();
                executor = null;
            }
        });
    }

    private void handleError(Exception e) {
        Platform.runLater(() -> {
            statusLabel.setText("测速失败: " + e.getMessage());
            progressBar.setProgress(0);
            isRunning = false;
            startButton.setText("开始测试");
        });
    }
}
