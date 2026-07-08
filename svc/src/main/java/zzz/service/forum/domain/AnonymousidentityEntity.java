package zzz.service.forum.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 匿名身份映射表（同帖匿名一致，不同帖匿名不同）(Anonymousidentity)实体类
 *
 * @author caoshi
 * @since 2026-05-13 15:13:29
 */
public class AnonymousidentityEntity implements Serializable {
    private static final long serialVersionUID = -39216397316216647L;
    // 主键ID
    private Long id;
    // 真实用户ID
    private Long userid;
    // 帖子ID（匿名作用域）
    private Long postid;
    // 匿名昵称（如：吃瓜猫）
    private String anonymousname;
    // 匿名头像
    private String anonymousavatar;
    // 匿名颜色（UI标识）
    private String anonymouscolor;
    // 创建时间
    private Date createdat;

    // 主键ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 真实用户ID
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    // 帖子ID（匿名作用域）
    public Long getPostid() {
        return postid;
    }

    public void setPostid(Long postid) {
        this.postid = postid;
    }

    // 匿名昵称（如：吃瓜猫）
    public String getAnonymousname() {
        return anonymousname;
    }

    public void setAnonymousname(String anonymousname) {
        this.anonymousname = anonymousname;
    }

    // 匿名头像
    public String getAnonymousavatar() {
        return anonymousavatar;
    }

    public void setAnonymousavatar(String anonymousavatar) {
        this.anonymousavatar = anonymousavatar;
    }

    // 匿名颜色（UI标识）
    public String getAnonymouscolor() {
        return anonymouscolor;
    }

    public void setAnonymouscolor(String anonymouscolor) {
        this.anonymouscolor = anonymouscolor;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

