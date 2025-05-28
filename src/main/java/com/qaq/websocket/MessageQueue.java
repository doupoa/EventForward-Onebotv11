package com.qaq.websocket;

import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class MessageQueue {
    //单例模式
    private static final MessageQueue INSTANCE = new MessageQueue();
    //线程安全的无界队列
    private static final ConcurrentLinkedQueue<Consumer<MinecraftServer>> QUEUE = new ConcurrentLinkedQueue<>();

    public static MessageQueue getInstance() {
        return INSTANCE;
    }

    // 生产者方法 可在任意线程调用
    public void enqueue(Consumer<MinecraftServer> consumer) {
        QUEUE.offer(consumer);
    }

    // 消费者方法 必须在主线程调用
    public void processMessages(MinecraftServer server) {
        int maxProcessPerTick = 100; //每tick最多处理100条消息
        for (int i = 0; i < maxProcessPerTick && !QUEUE.isEmpty(); i++) {
            Consumer<MinecraftServer> consumer = QUEUE.poll();
            if (consumer != null) consumer.accept(server);
        }
    }
}