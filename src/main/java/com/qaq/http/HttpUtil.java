package com.qaq.http;

import com.qaq.EventForwardobv11;
import com.qaq.config.ModConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// GET 方法实现
public class HttpUtil {
    public static void sendGet(String message) {
        ModConfig config = EventForwardobv11.CONFIG;
        String url = "http://" + config.obServer + ":" + config.obPort + "/send_group_msg?access_token=" + config.obToken + "&group_id=" + config.forwardGroup + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            switch (response.getStatusLine().getStatusCode()) {
                case 401:
                    EventForwardobv11.LOGGER.error("Authentication failed. The current request requires a token");
                    break;
                case 403:
                    EventForwardobv11.LOGGER.error("Authentication failed, token is invalid");
                    break;
                case 404:
                    EventForwardobv11.LOGGER.error("Request failed, invalid interface:{}", url);
            }
            // 主要是根据状态码判断错误，正常返回结果直接忽略
        } catch (Exception e) {
            EventForwardobv11.LOGGER.error("Failed to send message:{}", e.getMessage());
        }
    }
}