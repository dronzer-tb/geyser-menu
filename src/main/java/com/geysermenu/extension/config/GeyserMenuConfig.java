package com.geysermenu.extension.config;

import com.geysermenu.extension.GeyserMenuExtension;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeyserMenuConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // TCP Server settings
    private int tcpPort = 19133;
    private String tcpHost = "0.0.0.0";

    // Security settings
    private String secretKey = "CHANGE_ME_" + System.currentTimeMillis();
    private boolean requireAuthentication = true;
    private boolean enableSsl = false; // Disabled by default for compatibility

    // Double-click settings
    private boolean enableDoubleClickMenu = true;
    private int doubleClickThresholdMs = 200;

    // Menu settings
    private String defaultMenuTitle = "Server Menu";

    // Debug settings
    private boolean debugMode = false;

    public static GeyserMenuConfig load(GeyserMenuExtension extension) {
        // Try YAML first, then JSON for backwards compatibility
        Path yamlPath = extension.dataFolder().resolve("config.yml");
        Path jsonPath = extension.dataFolder().resolve("config.json");

        // Try loading YAML config
        if (Files.exists(yamlPath)) {
            try {
                GeyserMenuConfig config = loadFromYaml(yamlPath, extension);
                if (config != null) {
                    return config;
                }
            } catch (Exception e) {
                extension.logger().error("Failed to load config.yml", e);
            }
        }

        // Fallback to JSON config
        if (Files.exists(jsonPath)) {
            try (Reader reader = Files.newBufferedReader(jsonPath)) {
                GeyserMenuConfig config = GSON.fromJson(reader, GeyserMenuConfig.class);
                if (config != null) {
                    // Migrate to YAML and save
                    config.saveYaml(extension);
                    extension.logger().info("Migrated config from JSON to YAML format");
                    return config;
                }
            } catch (IOException e) {
                extension.logger().error("Failed to load config.json", e);
            }
        }

        // Create default config
        GeyserMenuConfig config = new GeyserMenuConfig();
        config.saveYaml(extension);
        return config;
    }

    private static GeyserMenuConfig loadFromYaml(Path yamlPath, GeyserMenuExtension extension) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(yamlPath)) {
            Map<String, Object> configMap = yaml.load(in);
            if (configMap == null) {
                return null;
            }

            GeyserMenuConfig config = new GeyserMenuConfig();

            // TCP settings
            if (configMap.containsKey("tcp-port")) {
                config.tcpPort = ((Number) configMap.get("tcp-port")).intValue();
            }
            if (configMap.containsKey("tcp-host")) {
                config.tcpHost = (String) configMap.get("tcp-host");
            }

            // Security settings
            if (configMap.containsKey("secret-key")) {
                config.secretKey = (String) configMap.get("secret-key");
            }
            if (configMap.containsKey("require-authentication")) {
                config.requireAuthentication = (Boolean) configMap.get("require-authentication");
            }
            if (configMap.containsKey("enable-ssl")) {
                config.enableSsl = (Boolean) configMap.get("enable-ssl");
            }

            // Double-click settings
            if (configMap.containsKey("enable-double-click-menu")) {
                config.enableDoubleClickMenu = (Boolean) configMap.get("enable-double-click-menu");
            }
            if (configMap.containsKey("double-click-threshold-ms")) {
                config.doubleClickThresholdMs = ((Number) configMap.get("double-click-threshold-ms")).intValue();
            }

            // Menu settings
            if (configMap.containsKey("default-menu-title")) {
                config.defaultMenuTitle = (String) configMap.get("default-menu-title");
            }

            // Debug settings
            if (configMap.containsKey("debug-mode")) {
                config.debugMode = (Boolean) configMap.get("debug-mode");
            }

            extension.logger().info("Loaded config from config.yml");
            return config;
        }
    }

    public void saveYaml(GeyserMenuExtension extension) {
        try {
            Files.createDirectories(extension.dataFolder());
            Path yamlPath = extension.dataFolder().resolve("config.yml");

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);

            Map<String, Object> configMap = new LinkedHashMap<>();
            
            // Header comment is not directly supported, so we'll write manually
            StringBuilder sb = new StringBuilder();
            sb.append("# GeyserMenu Extension Configuration\n");
            sb.append("# This extension provides reliable Bedrock form delivery\n");
            sb.append("# Credits: Based on FormsAPI by DronzerStudios (https://dronzerstudios.tech/)\n\n");
            sb.append("# TCP Server Settings\n");
            sb.append("tcp-port: ").append(tcpPort).append("\n");
            sb.append("tcp-host: \"").append(tcpHost).append("\"\n\n");
            sb.append("# Security Settings\n");
            sb.append("secret-key: \"").append(secretKey).append("\"\n");
            sb.append("require-authentication: ").append(requireAuthentication).append("\n");
            sb.append("enable-ssl: ").append(enableSsl).append("\n\n");
            sb.append("# Double-Click Menu Settings\n");
            sb.append("# When enabled, double-clicking inventory opens the GeyserMenu\n");
            sb.append("enable-double-click-menu: ").append(enableDoubleClickMenu).append("\n");
            sb.append("double-click-threshold-ms: ").append(doubleClickThresholdMs).append("\n\n");
            sb.append("# Menu Settings\n");
            sb.append("default-menu-title: \"").append(defaultMenuTitle).append("\"\n\n");
            sb.append("# Debug Settings\n");
            sb.append("debug-mode: ").append(debugMode).append("\n");

            Files.writeString(yamlPath, sb.toString());
        } catch (IOException e) {
            extension.logger().error("Failed to save config.yml", e);
        }
    }

    @Deprecated
    public void save(GeyserMenuExtension extension) {
        try {
            Files.createDirectories(extension.dataFolder());
            Path configPath = extension.dataFolder().resolve("config.json");

            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            extension.logger().error("Failed to save config", e);
        }
    }

    // Getters
    public int getTcpPort() {
        return tcpPort;
    }

    public String getTcpHost() {
        return tcpHost;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isRequireAuthentication() {
        return requireAuthentication;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public boolean isEnableDoubleClickMenu() {
        return enableDoubleClickMenu;
    }

    public int getDoubleClickThresholdMs() {
        return doubleClickThresholdMs;
    }

    public String getDefaultMenuTitle() {
        return defaultMenuTitle;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    // Setters
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
