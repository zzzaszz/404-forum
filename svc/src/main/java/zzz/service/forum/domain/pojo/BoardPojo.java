package zzz.service.forum.domain.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * 社区板块表(Board)Pojo
 *
 * @author caoshi
 * @since 2026-05-13 16:22:09
 */
public class BoardPojo implements Serializable {
    private static final long serialVersionUID = -27606501972759426L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 板块名称
    @Excel(name = "板块名称")
    private String name;
    // 板块编码（唯一标识）
    @Excel(name = "板块编码（唯一标识）")
    private String code;
    // 板块图标URL
    @Excel(name = "板块图标URL")
    private String icon;
    // 板块简介
    @Excel(name = "板块简介")
    private String description;
    // 排序值（越大越靠前）
    @Excel(name = "排序值（越大越靠前）")
    private Integer sortorder;
    // 帖子数量
    @Excel(name = "帖子数量")
    private Integer postcount;
    // 关注人数
    @Excel(name = "关注人数")
    private Integer followcount;
    // 状态：1正常 0隐藏
    @Excel(name = "状态：1正常 0隐藏")
    private Integer status;
    // 是否首页快捷显示：1显示 0不显示
    @Excel(name = "是否首页快捷显示")
    private Integer showonhome;
    // 创建时间
    @Excel(name = "创建时间")
    private Date createdat;
    // 更新时间
    @Excel(name = "更新时间")
    private Date updatedat;

    // 主键ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 板块名称
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 板块编码（唯一标识）
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // 板块图标URL
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // 板块简介
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 排序值（越大越靠前）
    public Integer getSortorder() {
        return sortorder;
    }

    public void setSortorder(Integer sortorder) {
        this.sortorder = sortorder;
    }

    // 帖子数量
    public Integer getPostcount() {
        return postcount;
    }

    public void setPostcount(Integer postcount) {
        this.postcount = postcount;
    }

    // 关注人数
    public Integer getFollowcount() {
        return followcount;
    }

    public void setFollowcount(Integer followcount) {
        this.followcount = followcount;
    }

    // 状态：1正常 0隐藏
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getShowonhome() {
        return showonhome;
    }

    public void setShowonhome(Integer showonhome) {
        this.showonhome = showonhome;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }

    // 更新时间
    public Date getUpdatedat() {
        return updatedat;
    }

    public void setUpdatedat(Date updatedat) {
        this.updatedat = updatedat;
    }


}

