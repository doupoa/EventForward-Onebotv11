package com.qaq.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qaq.EventForwardobv11;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "event-forward-obv11.json");

    public static ModConfig loadConfig() {
        try {
            File configFile = CONFIG_PATH.toFile();
            if (!configFile.exists()) {
                return createDefaultConfig();
            }

            // 读取旧配置
            ModConfig oldConfig;
            try (FileReader reader = new FileReader(configFile)) {
                oldConfig = GSON.fromJson(reader, ModConfig.class);
            }

            // 创建新配置默认值
            ModConfig newConfig = new ModConfig();

            // 合并配置 - 将旧配置的值复制到新配置中
            if (oldConfig != null) {
                newConfig.obServer = oldConfig.obServer != null ? oldConfig.obServer : newConfig.obServer;
                newConfig.obPort = oldConfig.obPort != 0 ? oldConfig.obPort : newConfig.obPort;
                newConfig.obToken = oldConfig.obToken != null ? oldConfig.obToken : newConfig.obToken;
                newConfig.forwardMethod = oldConfig.forwardMethod != null ? oldConfig.forwardMethod : newConfig.forwardMethod;
                newConfig.forwardGroup = oldConfig.forwardGroup != null ? oldConfig.forwardGroup : newConfig.forwardGroup;
                newConfig.adminUsers = oldConfig.adminUsers != null ? oldConfig.adminUsers : newConfig.adminUsers;
            }

            // 保存合并后的配置
            saveConfig(newConfig);

            return newConfig;
        } catch (Exception e) {
            EventForwardobv11.LOGGER.error("Unable to load configuration file, use default configuration：{}", e.getMessage());
            return new ModConfig();
        }
    }

    private static void saveConfig(ModConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            EventForwardobv11.LOGGER.error("Failed to save updated config: {}", e.getMessage());
        }
    }

    public static ModConfig createDefaultConfig() {
        try {
            File configFile = CONFIG_PATH.toFile();
            configFile.getParentFile().mkdirs(); // 确保目录存在
            //如果config 为空则新创建
            ModConfig defaultConfig = new ModConfig();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(defaultConfig, writer); // 写入默认配置
            }
            return defaultConfig;
        } catch (Exception e) {
            EventForwardobv11.LOGGER.error("Unable to create default profile:{}", e.getMessage());
            return new ModConfig();
        }
    }
}