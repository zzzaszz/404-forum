package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 通知表(Notification)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:18:19
 */
public class NotificationPojo implements Serializable {
    private static final long serialVersionUID = 765226516673064754L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 接收用户ID
    @Excel(name = "接收用户ID")
    private Long userid;
    // 通知类型
    @Excel(name = "通知类型")
    private Integer type;
    // 触发用户ID
    @Excel(name = "触发用户ID")
    private Long sourceuserid;
    // 关联目标ID
    @Excel(name = "关联目标ID")
    private Long targetid;
    // 通知内容
    @Excel(name = "通知内容")
    private String content;
    // 是否已读
    @Excel(name = "是否已读")
    private Integer isread;
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

    // 接收用户ID
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    // 通知类型
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    // 触发用户ID
    public Long getSourceuserid() {
        return sourceuserid;
    }

    public void setSourceuserid(Long sourceuserid) {
        this.sourceuserid = sourceuserid;
    }

    // 关联目标ID
    public Long getTargetid() {
        return targetid;
    }

    public void setTargetid(Long targetid) {
        this.targetid = targetid;
    }

    // 通知内容
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 是否已读
    public Integer getIsread() {
        return isread;
    }

    public void setIsread(Integer isread) {
        this.isread = isread;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

