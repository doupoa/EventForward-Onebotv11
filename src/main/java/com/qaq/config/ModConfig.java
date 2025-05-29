package com.qaq.config;

import java.util.List;

public class ModConfig { // 遵循Onebotv11协议发送事件
    public String obServer = "127.0.0.1"; // Onebotv11服务器地址
    public int obPort = 3000; // http端口
    public String obToken = ""; // 鉴权token
    public String forwardMethod = "http"; //请求方法 可选 http 和 ws
    public String forwardGroup = ""; // 要转发的群聊
    public List<String> adminUsers = List.of("");  // 管理员列表
    public List<String> commandPrefix = List.of("!"); // 指令触发前缀
    public List<String> messagePrefix = List.of("~","～"); // 消息触发前缀
}