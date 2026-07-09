package zzz.service.forum.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室管理器：多房间，每间最多 200 人。
 */
@Component
public class ChatRoomManager {

    /** 每个房间最大人数 */
    private static final int MAX_PER_ROOM = 200;

    /** 预创建房间：id → 名称 */
    private static final String[][] DEFAULT_ROOMS = {
            {"1", "闲聊大厅"},
            {"2", "技术交流"},
            {"3", "游戏娱乐"},
            {"4", "生活分享"},
            {"5", "求助问答"}
    };

    /** roomId → { sessionId → session } */
    private final Map<Integer, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    /** sessionId → roomId */
    private final Map<String, Integer> sessionRoom = new ConcurrentHashMap<>();

    /** roomId → 房间名称 */
    private final Map<Integer, String> roomNames = new ConcurrentHashMap<>();

    /** sessionId → 用户信息 {userId, nickname} */
    private final Map<String, Map<String, Object>> sessionUsers = new ConcurrentHashMap<>();

    public ChatRoomManager() {
        for (String[] room : DEFAULT_ROOMS) {
            int id = Integer.parseInt(room[0]);
            rooms.put(id, new ConcurrentHashMap<>());
            roomNames.put(id, room[1]);
        }
    }

    /** 记录用户信息 */
    public void setUserInfo(String sessionId, Long userId, String nickname) {
        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId != null ? userId.toString() : sessionId);
        info.put("nickname", nickname);
        sessionUsers.put(sessionId, info);
    }

    /** 移除用户信息 */
    public void removeUser(String sessionId) {
        sessionUsers.remove(sessionId);
        leaveCurrentRoom(sessionId);
    }

    /** 获取房间名称 */
    public String getRoomName(int roomId) {
        return roomNames.getOrDefault(roomId, "房间" + roomId);
    }

    /** 加入房间，返回加入结果消息 */
    public synchronized String joinRoom(int roomId, String sessionId, WebSocketSession session) {
        rooms.putIfAbsent(roomId, new ConcurrentHashMap<>());
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room.size() >= MAX_PER_ROOM) {
            return "房间" + roomId + "已满（" + MAX_PER_ROOM + "人），请选择其他房间";
        }
        // 先从旧房间移除
        leaveCurrentRoom(sessionId);
        room.put(sessionId, session);
        sessionRoom.put(sessionId, roomId);
        return null; // 成功
    }

    /** 离开当前房间 */
    public void leaveCurrentRoom(String sessionId) {
        Integer oldRoomId = sessionRoom.remove(sessionId);
        if (oldRoomId != null) {
            Map<String, WebSocketSession> oldRoom = rooms.get(oldRoomId);
            if (oldRoom != null) {
                oldRoom.remove(sessionId);
            }
        }
    }

    /** 移除会话 */
    public void removeSession(String sessionId) {
        leaveCurrentRoom(sessionId);
    }

    /** 获取房间所有会话 */
    public Collection<WebSocketSession> getRoomSessions(int roomId) {
        Map<String, WebSocketSession> room = rooms.get(roomId);
        return room != null ? room.values() : Collections.emptyList();
    }

    /** 获取用户所在房间 */
    public Integer getUserRoom(String sessionId) {
        return sessionRoom.get(sessionId);
    }

    /** 获取房间在线人数 */
    public int getRoomOnlineCount(int roomId) {
        Map<String, WebSocketSession> room = rooms.get(roomId);
        return room != null ? room.size() : 0;
    }

    /** 获取所有房间信息 */
    public List<Map<String, Object>> getRoomList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, WebSocketSession>> entry : rooms.entrySet()) {
            Map<String, Object> info = new HashMap<>();
            info.put("roomId", entry.getKey());
            info.put("name", getRoomName(entry.getKey()));
            info.put("online", entry.getValue().size());
            info.put("max", MAX_PER_ROOM);
            list.add(info);
        }
        list.sort(Comparator.comparingInt(m -> (Integer) m.get("roomId")));
        return list;
    }

    /** 创建新房间 */
    public synchronized int createRoom() {
        int newId = rooms.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        rooms.put(newId, new ConcurrentHashMap<>());
        return newId;
    }

    public Map<Integer, Map<String, WebSocketSession>> getRooms() {
        return rooms;
    }

    /** 获取房间在线用户列表 */
    public List<Map<String, Object>> getRoomUsers(int roomId) {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room == null) return users;
        for (String sid : room.keySet()) {
            Map<String, Object> info = sessionUsers.get(sid);
            if (info != null) {
                Map<String, Object> u = new HashMap<>();
                u.put("userId", info.get("userId"));
                u.put("nickname", info.get("nickname"));
                users.add(u);
            }
        }
        return users;
    }
}
