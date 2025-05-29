package com.qaq.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qaq.EventForwardobv11;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "event-forward-obv11.json");

    private static void mergeConfigs(Object oldConfig, Object newConfig){
        if (oldConfig != null) {
            Field[] fields = oldConfig.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // 允许访问私有字段
                try {
                    Object oldValue = field.get(oldConfig);
                    Object newValue = field.get(newConfig);

                    // 根据字段类型进行不同的默认值判断
                    if (oldValue != null && !oldValue.equals(newValue)) {
                        field.set(newConfig, oldValue);
                    }
                } catch (IllegalAccessException e) {
                    EventForwardobv11.LOGGER.error(e.toString());
                }
            }
        }
    }
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
            mergeConfigs(oldConfig, newConfig);

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