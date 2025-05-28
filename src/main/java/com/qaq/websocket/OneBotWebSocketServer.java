package com.qaq.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qaq.EventForwardobv11;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

// WebSocket服务器实现
public class OneBotWebSocketServer extends WebSocketServer {
    private static final String TARGET_PATH = "/forward/v11";
    private final Map<String, OneBotV11Handler> handlers = new HashMap<>();
    private final OneBotMessageBuilder messageBuilder = new OneBotMessageBuilder(EventForwardobv11.CONFIG.forwardGroup);


    public OneBotWebSocketServer(int port) {
        super(new InetSocketAddress(port));

    }

    public void registerHandler(String path, OneBotV11Handler handler) {
        handlers.put(path, handler);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String path = handshake.getResourceDescriptor();
        OneBotV11Handler handler = handlers.get(path);
        if (handler != null && TARGET_PATH.equals(path)) {
            handler.onClientConnected(conn);
            EventForwardobv11.LOGGER.info("New connection from: {}", conn.getRemoteSocketAddress());
        } else {
            conn.close(1003, "Invalid path");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        handlers.values().forEach(handler -> handler.onClientDisconnected(conn));
        EventForwardobv11.LOGGER.info("Closed Connection: {}", reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {

            // 验证是否为合法OneBot V11 JSON消息
            if (isValidOneBotMessage(message)) {
                handlers.values().forEach(handler -> handler.handleEvent(conn, message));
            }
        } catch (Exception e) {
            conn.close(1007, "Invalid message format");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        EventForwardobv11.LOGGER.error("WebSocket error:", ex);
    }

    @Override
    public void onStart() {
        EventForwardobv11.LOGGER.info("OneBot V11 WebSocket server started on port {}", getPort());
    }

    public void shutdownWebsocket() {
        try {
            getConnections().stream().filter(WebSocket::isOpen).forEach(WebSocket::close);
            stop(1000);
            EventForwardobv11.LOGGER.info("WebSocket server stopped");
        } catch (Exception e) {
            EventForwardobv11.LOGGER.error("WebSocket server stop error:", e);
            Thread.currentThread().interrupt();
        }
    }


    private boolean isValidOneBotMessage(String json) {
        // 这里添加你的OneBot协议验证逻辑
        return json.contains("\"post_type\"") && json.contains("\"time\"");
    }



    // 向所有连接的客户端广播游戏事件
    public void broadcastGameEvent(String jsonEvent) {
        String message = messageBuilder.buildTextMessage(jsonEvent);
        getConnections().stream().filter(WebSocket::isOpen).forEach(conn -> conn.send(message));
    }

    public void replyMessage(String message, String messageId, WebSocket conn) {
        if (conn.isOpen()) {
            conn.send(messageBuilder.buildReplyMessage(message, messageId));
        }
    }

    public static class OneBotMessageBuilder {
        private static final String ACTION_SEND = "send_group_msg";
        private final String groupId;

        public OneBotMessageBuilder(String groupId) {
            this.groupId = groupId;
        }


        // 基础消息结构
        private JsonObject createBaseMessage() {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", ACTION_SEND);
            JsonObject params = new JsonObject();
            params.addProperty("group_id", this.groupId);
            obj.add("params", params);
            return obj;
        }

        // 构建纯文本消息
        public String buildTextMessage(String text) {
            JsonObject msg = createBaseMessage();
            msg.getAsJsonObject("params").addProperty("message", text);
            return msg.toString();
        }

        // 构建带回复的消息
        public String buildReplyMessage(String text, String replyId) {
            JsonObject msg = createBaseMessage();
            JsonArray messages = new JsonArray();

            // 回复段
            if (replyId != null) {
                JsonObject replySeg = new JsonObject();
                replySeg.addProperty("type", "reply");
                JsonObject replyData = new JsonObject();
                replyData.addProperty("id", replyId);
                replySeg.add("data", replyData);
                messages.add(replySeg);
            }

            // 文本段
            JsonObject textSeg = new JsonObject();
            textSeg.addProperty("type", "text");
            JsonObject textData = new JsonObject();
            textData.addProperty("text", text);
            textSeg.add("data", textData);
            messages.add(textSeg);

            msg.getAsJsonObject("params").add("message", messages);
            return msg.toString();
        }
    }
}