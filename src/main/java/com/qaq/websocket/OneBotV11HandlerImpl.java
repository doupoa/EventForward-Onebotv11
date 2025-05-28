package com.qaq.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qaq.EventForwardobv11;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.java_websocket.WebSocket;


// OneBot协议处理器
public class OneBotV11HandlerImpl implements OneBotV11Handler {

    @Override
    public void handleEvent(WebSocket conn, String jsonMessage) {
        // 1. 过滤非群消息
        JsonObject groupMsg = OneBotV11Filter.filterGroupMessage(jsonMessage);

        if (groupMsg == null) return;

        try {
            // 2. 提取基础字段
            long selfId = groupMsg.get("self_id").getAsLong();
            JsonElement messages = groupMsg.get("message");
            JsonObject sender = groupMsg.get("sender").getAsJsonObject();
            String userId = groupMsg.get("user_id").getAsString();
            String messageId = groupMsg.get("message_id").getAsString();
            String nickname = sender.get("nickname").getAsString();

            // 3. 解析消息段
            ParsedMessage parsed = parseMessageContent(messages, selfId);

            // 4. 处理特殊消息类型
            if (parsed.isReply && parsed.toMe) {
                // 将回复消息转换为 ~ 开头
                parsed.content = "~" + parsed.content;
            }

            // 5. 根据消息前缀处理
            if (parsed.content.startsWith("~")) {
                handleForwardMessage(nickname, parsed.content.substring(1));
            } else if (parsed.content.startsWith("!")) {
                if (parsed.content.equals("!list")) {
                    handleGetPlayerList(userId);
                    return;
                }
                handleCommand(userId, parsed.content.substring(1), messageId, conn);
            }

        } catch (Exception e) {
            EventForwardobv11.LOGGER.error("Message processing failed", e);
            conn.close(1008, "Processing error");
        }
    }

    // 解析消息内容（支持reply类型和文本拼接）
    private ParsedMessage parseMessageContent(JsonElement messages, long selfId) {
        ParsedMessage result = new ParsedMessage();
        StringBuilder builder = new StringBuilder();

        if (messages instanceof JsonArray) {
            for (JsonElement element : messages.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject msgObj = element.getAsJsonObject();
                    String type = msgObj.get("type").getAsString();

                    if ("reply".equals(type)) {
                        result.isReply = true;
                        continue;
                    }

                    if ("at".equals(type)) {
                        if (selfId == msgObj.get("data").getAsJsonObject().get("qq").getAsLong()) {
                            result.toMe = true;
                            continue;
                        }
                    }

                    if ("text".equals(type)) {
                        builder.append(msgObj.get("data").getAsJsonObject().get("text").getAsString());
                    }
                }
            }
        } else if (messages.isJsonObject()) {
            JsonObject msgObj = messages.getAsJsonObject();
            if ("reply".equals(msgObj.get("type").getAsString())) {
                result.isReply = true;
            } else if ("at".equals(msgObj.get("type").getAsString())) {
                if (selfId == msgObj.get("data").getAsJsonObject().get("qq").getAsLong()) {
                    result.toMe = true;
                }
            } else if ("text".equals(msgObj.get("type").getAsString())) {
                builder.append(msgObj.get("data").getAsJsonObject().get("text").getAsString());
            }
        } else {
            builder.append(messages.getAsString());
        }
        result.content = builder.toString().trim();
        return result;
    }

    // 处理转发消息
    private void handleForwardMessage(String nickname, String message) {
        if (message.isEmpty()) return;
        MessageQueue.getInstance().enqueue(server -> {
            if (server.getCurrentPlayerCount() <= 0) return;
            Text text = Text.literal("[群消息] ").append(Text.literal(nickname).formatted(Formatting.AQUA).append(": ").append(Text.literal(message).formatted(Formatting.WHITE)));

            server.getPlayerManager().broadcast(text, false);
        });
    }

    // 处理指令
    private void handleCommand(String userId, String command, String messageId, WebSocket conn) {
        if (command.isEmpty()) return;
        if (!EventForwardobv11.CONFIG.adminUsers.contains(userId)) {
            EventForwardobv11.LOGGER.warn("Unauthorized command attempt by {}, Command: {}", userId, command);
            EventForwardobv11.wsServer.replyMessage("您没有权限执行该指令", messageId, conn);
            return;
        }
        EventForwardobv11.LOGGER.info("[Command] {} executed: {}", userId, command);

        MessageQueue.getInstance().enqueue(server -> {
            // 执行Minecraft命令
            server.getCommandManager().executeWithPrefix(server.getCommandSource(), command);
        });
    }

    private void handleGetPlayerList(String userId) {
        EventForwardobv11.LOGGER.info("[Command] {} executed: /list", userId);
        // 该指令所有成员均可执行
        int onlineCount = EventForwardobv11.OnlinePlayers.size();
        if (onlineCount == 0) EventForwardobv11.wsServer.broadcastGameEvent("当前没有玩家在线");
        else
            EventForwardobv11.wsServer.broadcastGameEvent("当前" + onlineCount + "位在线玩家: " + String.join(", ", EventForwardobv11.OnlinePlayers));
    }

    @Override
    public void onClientConnected(WebSocket conn) {
        // 向游戏内发送消息
        MessageQueue.getInstance().enqueue(server -> { //onOpen
            if (server.getCurrentPlayerCount() <= 0) return;
            server.getPlayerManager().broadcast(Text.literal("[Server] 群聊互通已连接"), false);
        });
    }

    @Override
    public void onClientDisconnected(WebSocket conn) {
        // 向游戏内发送消息
        MessageQueue.getInstance().enqueue(server -> { // OnClose
            if (server.getCurrentPlayerCount() <= 0) return;
            server.getPlayerManager().broadcast(Text.literal("[Server] 群聊互通已断开"), false);
        });
    }

    // 消息解析结果封装
    private static class ParsedMessage {
        String content;
        boolean isReply;
        boolean toMe;
    }
}