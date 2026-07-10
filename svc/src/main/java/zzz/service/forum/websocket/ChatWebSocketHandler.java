package zzz.service.forum.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.User;
import zzz.service.forum.mapper.UserMapper;
import zzz.service.forum.utils.JwtUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 聊天处理器：多房间文本消息。
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    @Resource
    private ChatRoomManager roomManager;
    @Resource
    private UserMapper userMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从 URL 参数获取 token 和 roomId
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;
        String token = getParam(query, "token");
        Integer roomId = getIntParam(query, "roomId", 1);

        // 解析 token 获取用户信息
        String nickname = "游客";
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            try {
                userId = JwtUtils.parseToken(token).get("userId", Long.class);
                User user = userMapper.selectById(userId);
                if (user != null && user.getNickname() != null) {
                    nickname = user.getNickname();
                }
            } catch (Exception e) {
                nickname = "游客" + session.getId().substring(0, 4);
            }
        } else {
            nickname = "游客" + session.getId().substring(0, 4);
        }

        // 加入房间
        String err = roomManager.joinRoom(roomId, session.getId(), session);
        if (err != null) {
            sendMessage(session, new ChatMessage("system", "系统", err, roomId, roomManager.getRoomOnlineCount(roomId)));
            try { session.close(); } catch (IOException ignored) {}
            return;
        }

        // 保存用户信息
        session.getAttributes().put("nickname", nickname);
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("roomId", roomId);
        roomManager.setUserInfo(session.getId(), userId, nickname);

        int online = roomManager.getRoomOnlineCount(roomId);
        String roomName = roomManager.getRoomName(roomId);
        log.info("用户 {} 加入房间 {}，当前在线：{}", nickname, roomId, online);

        // 广播加入消息
        broadcast(roomId, new ChatMessage("join", "系统", nickname + " 加入了" + roomName, roomId, online));

        // 发送房间列表更新
        sendRoomList();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JSONObject json = JSON.parseObject(message.getPayload());
            String type = json.getString("type");

            // 应用层心跳：前端发 ping，后端回 pong（无需加入房间即可响应）
            if ("ping".equals(type)) {
                ChatMessage pong = new ChatMessage();
                pong.setType("pong");
                sendMessage(session, pong);
                return;
            }

            Integer roomId = (Integer) session.getAttributes().get("roomId");
            String nickname = (String) session.getAttributes().get("nickname");

            if (roomId == null) return;

            if ("chat".equals(type)) {
                String content = json.getString("content");
                if (content == null || content.trim().isEmpty()) return;
                if (content.length() > 500) content = content.substring(0, 500);

                int online = roomManager.getRoomOnlineCount(roomId);
                broadcast(roomId, new ChatMessage("chat", nickname, content, roomId, online));
            } else if ("switch".equals(type)) {
                // 切换房间
                Integer newRoomId = json.getInteger("newRoomId");
                if (newRoomId == null || newRoomId.equals(roomId)) return;

                String err = roomManager.joinRoom(newRoomId, session.getId(), session);
                if (err != null) {
                    sendMessage(session, new ChatMessage("system", "系统", err, newRoomId, roomManager.getRoomOnlineCount(newRoomId)));
                    return;
                }

                // 离开旧房间广播
                int oldOnline = roomManager.getRoomOnlineCount(roomId);
                broadcast(roomId, new ChatMessage("leave", "系统", nickname + " 离开了" + roomManager.getRoomName(roomId), roomId, oldOnline));

                // 更新 session
                session.getAttributes().put("roomId", newRoomId);
                int newOnline = roomManager.getRoomOnlineCount(newRoomId);

                // 进入新房间广播
                broadcast(newRoomId, new ChatMessage("join", "系统", nickname + " 加入了" + roomManager.getRoomName(newRoomId), newRoomId, newOnline));

                // 发送切换成功
                sendMessage(session, new ChatMessage("system", "系统", "已切换到" + roomManager.getRoomName(newRoomId), newRoomId, newOnline));
                sendRoomList();
            } else if ("roomList".equals(type)) {
                sendRoomList();
            }
        } catch (Exception e) {
            log.warn("处理消息异常: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String nickname = (String) session.getAttributes().get("nickname");
        Integer roomId = (Integer) session.getAttributes().get("roomId");

        roomManager.removeUser(session.getId());

        if (roomId != null) {
            int online = roomManager.getRoomOnlineCount(roomId);
            broadcast(roomId, new ChatMessage("leave", "系统",
                    (nickname != null ? nickname : "某人") + " 离开了" + roomManager.getRoomName(roomId), roomId, online));
            sendRoomList();
        }
        log.info("用户 {} 断开连接", nickname != null ? nickname : session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输异常: {}", exception.getMessage());
        try { session.close(); } catch (IOException ignored) {}
    }

    /** 广播消息到房间所有用户 */
    private void broadcast(int roomId, ChatMessage msg) {
        String json = JSON.toJSONString(msg);
        for (WebSocketSession s : roomManager.getRoomSessions(roomId)) {
            if (s.isOpen()) {
                try {
                    synchronized (s) {
                        s.sendMessage(new TextMessage(json));
                    }
                } catch (IOException e) {
                    log.warn("发送消息失败: {}", e.getMessage());
                }
            }
        }
    }

    /** 发送消息给单个用户 */
    private void sendMessage(WebSocketSession session, ChatMessage msg) {
        if (session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(JSON.toJSONString(msg)));
                }
            } catch (IOException e) {
                log.warn("发送消息失败: {}", e.getMessage());
            }
        }
    }

    /** 推送房间列表（含在线用户）给所有在线用户 */
    private void sendRoomList() {
        // 房间列表含在线用户
        List<Map<String, Object>> roomListData = roomManager.getRoomList();
        for (Map<String, Object> room : roomListData) {
            int rid = (Integer) room.get("roomId");
            room.put("users", roomManager.getRoomUsers(rid));
        }

        ChatMessage listMsg = new ChatMessage();
        listMsg.setType("roomList");
        listMsg.setContent(JSON.toJSONString(roomListData));

        String json = JSON.toJSONString(listMsg);
        for (Integer roomId : roomManager.getRooms().keySet()) {
            for (WebSocketSession s : roomManager.getRoomSessions(roomId)) {
                if (s.isOpen()) {
                    try {
                        synchronized (s) {
                            s.sendMessage(new TextMessage(json));
                        }
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    private String getParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    private Integer getIntParam(String query, String key, int def) {
        String v = getParam(query, key);
        try { return v != null ? Integer.parseInt(v) : def; }
        catch (NumberFormatException e) { return def; }
    }
}
