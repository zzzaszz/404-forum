package zzz.service.forum.domain.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论表（支持楼中楼结构）(Comment)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:06:44
 */
public class CommentPojo implements Serializable {
    private static final long serialVersionUID = -38250444965847603L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 帖子ID
    @Excel(name = "帖子ID")
    private Long postid;
    // 真实用户ID
    @Excel(name = "真实用户ID")
    private Long userid;
    // 根评论ID（楼中楼结构）
    @Excel(name = "根评论ID（楼中楼结构）")
    private Long rootid;
    // 父评论ID
    @Excel(name = "父评论ID")
    private Long parentid;
    // 被回复用户ID
    @Excel(name = "被回复用户ID")
    private Long replyuserid;
    // 匿名身份ID
    @Excel(name = "匿名身份ID")
    private Long anonymousidentityid;
    // 匿名昵称
    @Excel(name = "匿名昵称")
    private String anonymousname;
    // 评论内容
    @Excel(name = "评论内容")
    private String content;
    // 点赞数
    @Excel(name = "点赞数")
    private Long likecount;
    // 回复数
    @Excel(name = "回复数")
    private Long replycount;
    // 状态：1正常 2删除 3屏蔽
    @Excel(name = "状态：1正常 2删除 3屏蔽")
    private Integer status;
    // 创建时间
    @Excel(name = "创建时间")
    private Date createdat;

    // 主键ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 帖子ID
    public Long getPostid() {
        return postid;
    }

    public void setPostid(Long postid) {
        this.postid = postid;
    }

    // 真实用户ID
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    // 根评论ID（楼中楼结构）
    public Long getRootid() {
        return rootid;
    }

    public void setRootid(Long rootid) {
        this.rootid = rootid;
    }

    // 父评论ID
    public Long getParentid() {
        return parentid;
    }

    public void setParentid(Long parentid) {
        this.parentid = parentid;
    }

    // 被回复用户ID
    public Long getReplyuserid() {
        return replyuserid;
    }

    public void setReplyuserid(Long replyuserid) {
        this.replyuserid = replyuserid;
    }

    // 匿名身份ID
    public Long getAnonymousidentityid() {
        return anonymousidentityid;
    }

    public void setAnonymousidentityid(Long anonymousidentityid) {
        this.anonymousidentityid = anonymousidentityid;
    }

    // 匿名昵称
    public String getAnonymousname() {
        return anonymousname;
    }

    public void setAnonymousname(String anonymousname) {
        this.anonymousname = anonymousname;
    }

    // 评论内容
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 点赞数
    public Long getLikecount() {
        return likecount;
    }

    public void setLikecount(Long likecount) {
        this.likecount = likecount;
    }

    // 回复数
    public Long getReplycount() {
        return replycount;
    }

    public void setReplycount(Long replycount) {
        this.replycount = replycount;
    }

    // 状态：1正常 2删除 3屏蔽
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

