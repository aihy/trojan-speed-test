package com.hhwyz;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyTesterTest {

    @Test
    void testDownloadSpeed_Success() throws Exception {
        ProxyTester proxyTester = new ProxyTester(List.of(
                "https://captive.apple.com",
                "https://cp.cloudflare.com",
                "https://www.google.com/generate_204"
        ), 10);
        int localPort = ProxyTester.getAvailablePort();
        TrojanConfig config = new TrojanConfig("fw-jp-test1.trojanwheel.com", 5011, "6Fe3emZNpEGxzrprMb", localPort);
        File configFile = config.generateConfigFile();

        // 启动trojan
        ProcessBuilder pb = new ProcessBuilder("./trojan", "-c", configFile.getAbsolutePath());
        pb.directory(new File("trojan"));
        Process start = pb.start();

        // 等待trojan启动
        Thread.sleep(1000);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", localPort));
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        double speed = proxyTester.testDownloadSpeed(client, ProxyTester.SPEED_TEST_URL);

        // 验证结果
        assertTrue(speed > 0, "下载速度应该大于0");
        System.out.println("测试下载速度: " + speed + " Kbps");
    }
}