package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 点赞记录表（防重复点赞）(Likerecord)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:18:02
 */
public class LikerecordPojo implements Serializable {
    private static final long serialVersionUID = 660416120199022462L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 用户ID
    @Excel(name = "用户ID")
    private Long userid;
    // 目标类型：1帖子 2评论
    @Excel(name = "目标类型：1帖子 2评论")
    private Integer targettype;
    // 目标ID
    @Excel(name = "目标ID")
    private Long targetid;
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

    // 目标类型：1帖子 2评论
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

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

