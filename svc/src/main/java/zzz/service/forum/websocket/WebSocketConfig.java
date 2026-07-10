package zzz.service.forum.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * WebSocket 配置：注册聊天处理器到 /ws/chat 路径。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private ChatWebSocketHandler chatHandler;

    @Resource
    private NotifyWebSocketHandler notifyHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*");
        // 用户通知通道：私信等实时推送
        registry.addHandler(notifyHandler, "/ws/notify")
                .setAllowedOrigins("*");
    }
}
