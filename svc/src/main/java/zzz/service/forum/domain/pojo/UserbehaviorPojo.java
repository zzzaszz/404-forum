package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 用户行为表（推荐系统基础）(Userbehavior)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:22:30
 */
public class UserbehaviorPojo implements Serializable {
    private static final long serialVersionUID = 636092237620648739L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 用户ID
    @Excel(name = "用户ID")
    private Long userid;
    // 行为类型：1浏览 2点赞 3收藏 4评论
    @Excel(name = "行为类型：1浏览 2点赞 3收藏 4评论")
    private Integer behaviortype;
    // 目标类型
    @Excel(name = "目标类型")
    private Integer targettype;
    // 目标ID
    @Excel(name = "目标ID")
    private Long targetid;
    // 停留时长（秒）
    @Excel(name = "停留时长（秒）")
    private Integer duration;
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

    // 用户ID
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    // 行为类型：1浏览 2点赞 3收藏 4评论
    public Integer getBehaviortype() {
        return behaviortype;
    }

    public void setBehaviortype(Integer behaviortype) {
        this.behaviortype = behaviortype;
    }

    // 目标类型
    public Integer getTargettype() {
        return targettype;
    }

    public void setTargettype(Integer targettype) {
        this.targettype = targettype;
    }

    // 目标ID
    public Long getTargetid() {
        return targetid;
    }

    public void setTargetid(Long targetid) {
        this.targetid = targetid;
    }

    // 停留时长（秒）
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

