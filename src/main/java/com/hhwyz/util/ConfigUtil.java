package com.hhwyz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator
            + ".config" + File.separator
            + "trojan-speed-test" + File.separator
            + "config.properties";
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }
    
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
