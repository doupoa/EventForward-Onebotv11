package com.qaq.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.qaq.EventForwardobv11;

public class OneBotV11Filter {
    private static final String GROUP_MSG_TYPE = "group";

    /**
     * 检查是否为合法的群消息事件
     *
     * @param jsonStr 原始JSON字符串
     * @return 如果是群消息返回解析后的JsonObject，否则返回null
     */
    public static JsonObject filterGroupMessage(String jsonStr) {
        try {
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

            // 基础协议字段检查
            if (!json.has("post_type") || !json.has("message_type") || !json.has("group_id")) {
                return null;
            }

            // 确认为群消息事件
            if ("message".equals(json.get("post_type").getAsString()) && GROUP_MSG_TYPE.equals(json.get("message_type").getAsString())) {
                return json;
            }

        } catch (JsonSyntaxException | IllegalStateException e) {
            // 非法JSON格式或类型转换错误
            EventForwardobv11.LOGGER.error("Invalid OneBot message: {}", e.getMessage());
        }
        return null;
    }
}