package zzz.service.forum.websocket;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import zzz.service.forum.service.RedisService;
import zzz.service.forum.utils.JwtUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户通知 WebSocket：按 userId 维护连接，推送私信等实时通知。
 * <p>
 * 与聊天室 /ws/chat 不同，这里不区分房间，仅按登录用户建立专属通道，
 * 后端在产生私信等事件时通过 {@link #sendToUser} 主动推送给接收方。
 */
@Component
public class NotifyWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotifyWebSocketHandler.class);
    private static final String ACCESS_TOKEN_KEY_PREFIX = "login:access:";

    /** userId -> session（同一用户仅保留最新连接） */
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Resource
    private RedisService redisService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;
        String token = getParam(query, "token");

        Long userId = null;
        if (token != null && !token.isEmpty()) {
            try {
                if (JwtUtils.isAccessToken(token)) {
                    Long uid = JwtUtils.parseToken(token).get("userId", Long.class);
                    // 与 AuthAspect 一致：Redis 中 access token 必须存在且一致
                    String stored = redisService.getCacheObject(ACCESS_TOKEN_KEY_PREFIX + uid);
                    if (stored != null && stored.equals(token)) {
                        userId = uid;
                    }
                }
            } catch (Exception ignored) {
                userId = null;
            }
        }

        if (userId == null) {
            try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
            return;
        }

        session.getAttributes().put("userId", userId);
        // 覆盖旧连接
        WebSocketSession old = userSessions.put(userId, session);
        if (old != null && old.isOpen() && old != session) {
            try { old.close(); } catch (IOException ignored) {}
        }
        log.info("用户 {} 已连接通知 WS，当前在线通知：{}", userId, userSessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            // 仅当当前 session 仍是该用户的最新连接才移除
            if (userSessions.get(userId) == session) {
                userSessions.remove(userId);
            }
            log.info("用户 {} 断开通知 WS", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("通知 WS 传输异常: {}", exception.getMessage());
        try { session.close(); } catch (IOException ignored) {}
    }

    /**
     * 向指定用户推送消息。在线则发送，返回是否送达。
     */
    public static boolean sendToUser(Long userId, Object message) {
        if (userId == null) return false;
        WebSocketSession session = userSessions.get(userId);
        if (session == null || !session.isOpen()) return false;
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(JSON.toJSONString(message)));
            }
            return true;
        } catch (IOException e) {
            log.warn("推送通知失败: {}", e.getMessage());
            return false;
        }
    }

    /** 当前在线通知连接数 */
    public static int onlineCount() {
        return userSessions.size();
    }

    private String getParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }
}
