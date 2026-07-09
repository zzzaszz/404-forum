package zzz.service.forum.websocket;

/**
 * WebSocket 聊天消息 DTO
 */
public class ChatMessage {

    /** 消息类型：join 加入 / leave 离开 / chat 聊天 / system 系统 */
    private String type;
    /** 发送者昵称 */
    private String nickname;
    /** 消息内容 */
    private String content;
    /** 房间号 */
    private Integer roomId;
    /** 房间在线人数 */
    private Integer onlineCount;
    /** 时间戳 */
    private Long timestamp;

    public ChatMessage() {}

    public ChatMessage(String type, String nickname, String content, Integer roomId, Integer onlineCount) {
        this.type = type;
        this.nickname = nickname;
        this.content = content;
        this.roomId = roomId;
        this.onlineCount = onlineCount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }
    public Integer getOnlineCount() { return onlineCount; }
    public void setOnlineCount(Integer onlineCount) { this.onlineCount = onlineCount; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
