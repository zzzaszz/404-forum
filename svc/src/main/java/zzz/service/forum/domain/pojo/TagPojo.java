package zzz.service.forum.domain.pojo;

import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 标签表(Tag)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:22:12
 */
public class TagPojo implements Serializable {
    private static final long serialVersionUID = 884383756646226181L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 标签名称
    @Excel(name = "标签名称")
    private String name;
    // 关联帖子数量
    @Excel(name = "关联帖子数量")
    private Integer postcount;

    // 主键ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 标签名称
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 关联帖子数量
    public Integer getPostcount() {
        return postcount;
    }

    public void setPostcount(Integer postcount) {
        this.postcount = postcount;
    }


}

