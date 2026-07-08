package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 收藏记录表(Favoriterecord)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:17:40
 */
public class FavoriterecordPojo implements Serializable {
    private static final long serialVersionUID = 474929760740571605L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 用户ID
    @Excel(name = "用户ID")
    private Long userid;
    // 帖子ID
    @Excel(name = "帖子ID")
    private Long postid;
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

    // 帖子ID
    public Long getPostid() {
        return postid;
    }

    public void setPostid(Long postid) {
        this.postid = postid;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

