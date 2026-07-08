package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 内容审核表(Contentaudit)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:17:22
 */
public class ContentauditPojo implements Serializable {
    private static final long serialVersionUID = -21464604980569939L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 目标类型
    @Excel(name = "目标类型")
    private Integer targettype;
    // 目标ID
    @Excel(name = "目标ID")
    private Long targetid;
    // 审核类型
    @Excel(name = "审核类型")
    private Integer audittype;
    // 风险等级
    @Excel(name = "风险等级")
    private Integer risklevel;
    // 审核结果：0待审 1通过 2拒绝
    @Excel(name = "审核结果：0待审 1通过 2拒绝")
    private Integer result;
    // 命中敏感词
    @Excel(name = "命中敏感词")
    private String hitwords;
    // 备注
    @Excel(name = "备注")
    private String remark;
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

    // 审核类型
    public Integer getAudittype() {
        return audittype;
    }

    public void setAudittype(Integer audittype) {
        this.audittype = audittype;
    }

    // 风险等级
    public Integer getRisklevel() {
        return risklevel;
    }

    public void setRisklevel(Integer risklevel) {
        this.risklevel = risklevel;
    }

    // 审核结果：0待审 1通过 2拒绝
    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    // 命中敏感词
    public String getHitwords() {
        return hitwords;
    }

    public void setHitwords(String hitwords) {
        this.hitwords = hitwords;
    }

    // 备注
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

