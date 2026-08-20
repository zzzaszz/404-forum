package zzz.service.forum.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注关系表(UserFollow)实体类
 */
@Data
public class UserFollowEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;
    /** 关注者ID */
    private Long userid;
    /** 被关注者ID */
    private Long followuserid;
    /** 被关注者昵称（冗余，便于列表展示） */
    private String nickname;
    /** 创建时间 */
    private Date createdat;
}
