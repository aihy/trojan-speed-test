package com.hhwyz;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SubscriptionService {
    private final String subscriptionUrl;
    
    public SubscriptionService(String subscriptionUrl) {
        this.subscriptionUrl = subscriptionUrl;
    }
    
    public List<Map<String, Object>> getProxies() throws IOException {
        Map<String, Object> config = downloadSubscription();
        return extractProxies(config);
    }
    
    private Map<String, Object> downloadSubscription() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();

        Request request = new Request.Builder()
                .url(subscriptionUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("订阅下载失败");

            Yaml yaml = new Yaml();
            return yaml.load(response.body().string());
        }
    }

    private List<Map<String, Object>> extractProxies(Map<String, Object> config) {
        Object proxies = config.get("proxies");
        if (proxies instanceof List) {
            return (List<Map<String, Object>>) proxies;
        }
        return Collections.emptyList();
    }
}
