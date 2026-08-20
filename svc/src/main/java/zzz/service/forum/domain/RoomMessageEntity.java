package zzz.service.forum.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天室消息表(RoomMessage)实体类
 */
@Data
public class RoomMessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;
    /** 房间号 */
    private Integer roomid;
    /** 发送用户ID（游客为空） */
    private Long userid;
    /** 发送者昵称 */
    private String nickname;
    /** 消息内容 */
    private String content;
    /** 创建时间 */
    private Date createtime;
}
