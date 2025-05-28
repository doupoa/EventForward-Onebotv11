package com.qaq.websocket;

import org.java_websocket.WebSocket;


public interface OneBotV11Handler {
    void handleEvent(WebSocket conn, String jsonMessage); // 处理收到的OneBot协议消息

    void onClientConnected(WebSocket conn); // 新连接建立

    void onClientDisconnected(WebSocket conn); // 连接断开
}

