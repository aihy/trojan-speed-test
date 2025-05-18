package com.hhwyz;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TrojanConfig {
    private final String server;
    private final int port;
    private final String password;
    private final int localPort;

    public TrojanConfig(String server, int port, String password, int localPort) {
        this.server = server;
        this.port = port;
        this.password = password;
        this.localPort = localPort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public File generateConfigFile() throws IOException {
        File configFile = File.createTempFile("trojan_", ".json");
        JSONObject config = new JSONObject();
        
        config.put("run_type", "client");
        config.put("local_addr", "127.0.0.1");
        config.put("local_port", localPort);
        config.put("remote_addr", server);
        config.put("remote_port", port);
        
        JSONArray passwords = new JSONArray();
        passwords.add(password);
        config.put("password", passwords);
        
        config.put("log_level", 1);
        
        // SSL配置
        JSONObject ssl = new JSONObject();
        ssl.put("verify", true);
        ssl.put("verify_hostname", true);
        ssl.put("cert", "");
        ssl.put("cipher", "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES128-SHA:ECDHE-RSA-AES256-SHA:DHE-RSA-AES128-SHA:DHE-RSA-AES256-SHA:AES128-SHA:AES256-SHA:DES-CBC3-SHA");
        ssl.put("cipher_tls13", "TLS_AES_128_GCM_SHA256:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_256_GCM_SHA384");
        ssl.put("sni", "");
        JSONArray alpn = new JSONArray();
        alpn.add("h2");
        alpn.add("http/1.1");
        ssl.put("alpn", alpn);
        ssl.put("reuse_session", true);
        ssl.put("session_ticket", false);
        ssl.put("curves", "");
        config.put("ssl", ssl);
        
        // TCP配置
        JSONObject tcp = new JSONObject();
        tcp.put("no_delay", true);
        tcp.put("keep_alive", true);
        tcp.put("reuse_port", false);
        tcp.put("fast_open", false);
        tcp.put("fast_open_qlen", 20);
        config.put("tcp", tcp);
        
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(config.toJSONString());
        }
        
        return configFile;
    }
}
