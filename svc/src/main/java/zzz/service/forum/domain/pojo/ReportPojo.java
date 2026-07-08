package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 举报记录表(Report)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:21:59
 */
public class ReportPojo implements Serializable {
    private static final long serialVersionUID = -24394376985004074L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 举报人ID
    @Excel(name = "举报人ID")
    private Long reportuserid;
    // 目标类型
    @Excel(name = "目标类型")
    private Integer targettype;
    // 目标ID
    @Excel(name = "目标ID")
    private Long targetid;
    // 举报原因
    @Excel(name = "举报原因")
    private String reason;
    // 补充说明
    @Excel(name = "补充说明")
    private String description;
    // 处理状态：0未处理 1已处理
    @Excel(name = "处理状态：0未处理 1已处理")
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

    // 举报人ID
    public Long getReportuserid() {
        return reportuserid;
    }

    public void setReportuserid(Long reportuserid) {
        this.reportuserid = reportuserid;
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

    // 举报原因
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // 补充说明
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 处理状态：0未处理 1已处理
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

