package com.hhwyz;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyTester {
    private static final int DOWNLOAD_SIZE = 1024 * 1024; // 1MB
    public static final String SPEED_TEST_URL = "https://speed.cloudflare.com/__down?bytes=" + DOWNLOAD_SIZE;
    private final List<String> speedTestUrls;
    private final int timeoutSeconds;

    public ProxyTester(List<String> speedTestUrls, int timeoutSeconds) {
        this.speedTestUrls = speedTestUrls;
        this.timeoutSeconds = timeoutSeconds;
    }

    public static int getAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public SpeedTestResult testProxy(Map<String, Object> proxyConfig) {
        String name = (String) proxyConfig.get("name");

        Process trojanProcess = null;
        File configFile = null;

        try {
            String server = (String) proxyConfig.get("server");
            int port = ((Number) proxyConfig.get("port")).intValue();
            String password = (String) proxyConfig.get("password");

            int localPort = getAvailablePort();
            TrojanConfig config = new TrojanConfig(server, port, password, localPort);
            configFile = config.generateConfigFile();

            // 启动trojan
            ProcessBuilder pb = new ProcessBuilder("./trojan", "-c", configFile.getAbsolutePath());
            pb.directory(new File("trojan"));
            trojanProcess = pb.start();

            // 等待trojan启动
            Thread.sleep(1000);

            // 创建代理连接
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", localPort));

            OkHttpClient client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .readTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();

            Map<String, Long> latencies = new HashMap<>();

            for (String url : speedTestUrls) {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                long startTime = System.currentTimeMillis();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("测速失败");
                    response.body().bytes();
                    long latency = System.currentTimeMillis() - startTime;
                    latencies.put(url, latency);
                } catch (Exception e) {
                    latencies.put(url, Long.MAX_VALUE);
                }
            }

            // 测试下载速度
            double downloadSpeed = testDownloadSpeed(client);

            return new SpeedTestResult(name, "trojan", server, port, latencies, downloadSpeed);

        } catch (Exception e) {
            Map<String, Long> latencies = new HashMap<>();
            for (String url : speedTestUrls) {
                latencies.put(url, Long.MAX_VALUE);
            }
            return new SpeedTestResult(name, "trojan", "", 0, latencies, -1);
        } finally {
            // 清理资源
            if (trojanProcess != null) {
                trojanProcess.destroy();
            }
            if (configFile != null) {
                configFile.delete();
            }
        }
    }

    public double testDownloadSpeed(OkHttpClient client) {
        return testDownloadSpeed(client, SPEED_TEST_URL);
    }

    public double testDownloadSpeed(OkHttpClient client, String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            long startTime = System.currentTimeMillis();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("下载测速失败");

                byte[] data = response.body().bytes();
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // 如果下载时间为0或接近0，返回0
                if (duration < 1) return 0;

                return (double) DOWNLOAD_SIZE / duration;
            }
        } catch (Exception e) {
            System.err.println("下载测速失败: " + e.getMessage());
            return -1;
        }
    }
}
