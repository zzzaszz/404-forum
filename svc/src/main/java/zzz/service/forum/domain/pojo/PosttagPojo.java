package zzz.service.forum.domain.pojo;

import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 帖子标签关联表(Posttag)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:19:54
 */
public class PosttagPojo implements Serializable {
    private static final long serialVersionUID = 789524152532238218L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 帖子ID
    @Excel(name = "帖子ID")
    private Long postid;
    // 标签ID
    @Excel(name = "标签ID")
    private Long tagid;

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

    // 标签ID
    public Long getTagid() {
        return tagid;
    }

    public void setTagid(Long tagid) {
        this.tagid = tagid;
    }


}

