package com.qaq;

import com.qaq.config.ConfigManager;
import com.qaq.config.ModConfig;
import com.qaq.http.HttpUtil;
import com.qaq.websocket.MessageQueue;
import com.qaq.websocket.OneBotV11HandlerImpl;
import com.qaq.websocket.OneBotWebSocketServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;

// OneBot V11 协议消息处理器接口


public class EventForwardobv11 implements ModInitializer {
    public static final String MOD_ID = "event-forward-obv11";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig CONFIG;
    public static OneBotWebSocketServer wsServer;
    public static HashSet<String> OnlinePlayers = new HashSet<>();

    @Override
    public void onInitialize() {
        CONFIG = ConfigManager.loadConfig();


        if (Objects.equals(CONFIG.forwardMethod, "ws")) {
            wsServer = new OneBotWebSocketServer(CONFIG.obPort);
            wsServer.registerHandler("/forward/v11", new OneBotV11HandlerImpl()); // 注册OneBot协议处理器
            wsServer.start();
        } else {
            wsServer = null;
            LOGGER.info("OneBot HTTP client will send http://{}:{} Push server events", CONFIG.obServer, CONFIG.obPort);
        }

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (Objects.equals(CONFIG.forwardMethod, "ws")) {
                if (wsServer != null) {
                    wsServer.broadcastGameEvent("[ " + sender.getName().getString() + " ] " + message.getContent().getString());
                }
            } else if (Objects.equals(CONFIG.forwardMethod, "http")) {
                HttpUtil.sendGet("[" + sender.getName().getString() + "] " + message.getContent().getString());
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            OnlinePlayers.add(handler.player.getName().getString());
            if (Objects.equals(CONFIG.forwardMethod, "ws")) {
                if (wsServer != null) {
                    wsServer.broadcastGameEvent(handler.player.getName().getString() + "加入服务器 (" + (server.getCurrentPlayerCount() + 1) + "/" + server.getMaxPlayerCount() + ")");
                }
            } else if (Objects.equals(CONFIG.forwardMethod, "http")) {
                ServerPlayerEntity player = handler.player;
                HttpUtil.sendGet(player.getName().getString() + "加入服务器 (" + (server.getCurrentPlayerCount() + 1) + "/" + server.getMaxPlayerCount() + ")");
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            OnlinePlayers.remove(handler.player.getName().getString());
            if (Objects.equals(CONFIG.forwardMethod, "ws")) {
                if (wsServer != null) {
                    wsServer.broadcastGameEvent(handler.player.getName().getString() + "退出服务器");
                }
            } else if (Objects.equals(CONFIG.forwardMethod, "http")) {
                ServerPlayerEntity player = handler.player;
                HttpUtil.sendGet(player.getName().getString() + "退出服务器");
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MessageQueue.getInstance().processMessages(server); // 处理消息队列
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (wsServer != null) wsServer.shutdownWebsocket();
        });
    }
}