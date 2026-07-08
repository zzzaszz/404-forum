package zzz.service.forum.domain.pojo;

import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 帖子表（社区核心内容）(Post)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:19:36
 */
public class PostPojo implements Serializable {
    private static final long serialVersionUID = 487426773746599645L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 板块ID
    @Excel(name = "板块ID")
    private Long boardid;
    // 真实用户ID
    @Excel(name = "真实用户ID")
    private Long userid;
    // 匿名身份ID
    @Excel(name = "匿名身份ID")
    private Long anonymousidentityid;
    // 匿名昵称
    @Excel(name = "匿名昵称")
    private String anonymousname;
    // 匿名头像
    @Excel(name = "匿名头像")
    private String anonymousavatar;
    // 匿名标识颜色
    @Excel(name = "匿名标识颜色")
    private String anonymouscolor;
    // 帖子标题
    @Excel(name = "帖子标题")
    private String title;
    // 帖子内容（富文本）
    @Excel(name = "帖子内容（富文本）")
    private String content;
    // 纯文本内容（用于搜索）
    @Excel(name = "纯文本内容（用于搜索）")
    private String contenttext;
    // 图片列表（JSON数组）
    @Excel(name = "图片列表（JSON数组）")
    private String images;
    // 浏览量
    @Excel(name = "浏览量")
    private Long viewcount;
    // 评论数
    @Excel(name = "评论数")
    private Long replycount;
    // 点赞数
    @Excel(name = "点赞数")
    private Long likecount;
    // 收藏数
    @Excel(name = "收藏数")
    private Long favoritecount;
    // 分享数
    @Excel(name = "分享数")
    private Long sharecount;
    // 举报数
    @Excel(name = "举报数")
    private Long reportcount;
    // 热度分（用于推荐排序）
    @Excel(name = "热度分（用于推荐排序）")
    private BigDecimal hotscore;
    // 是否置顶
    @Excel(name = "是否置顶")
    private Integer istop;
    // 是否精华
    @Excel(name = "是否精华")
    private Integer isessence;
    // 状态：1正常 2审核中 3删除
    @Excel(name = "状态：1正常 2审核中 3删除")
    private Integer status;
    // IP哈希（风控）
    @Excel(name = "IP哈希（风控）")
    private String iphash;
    // 设备哈希（风控）
    @Excel(name = "设备哈希（风控）")
    private String devicehash;
    // 创建时间
    @Excel(name = "创建时间")
    private Date createdat;
    // 更新时间
    @Excel(name = "更新时间")
    private Date updatedat;
    // 逻辑删除
    @Excel(name = "逻辑删除")
    private Integer deleted;
    // 板块名称（联表查询，非表字段）
    private String boardname;
    // 板块编码（联表查询，非表字段）
    private String boardcode;

    // 主键ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 板块ID
    public Long getBoardid() {
        return boardid;
    }

    public void setBoardid(Long boardid) {
        this.boardid = boardid;
    }

    // 真实用户ID
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
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

    // 匿名头像
    public String getAnonymousavatar() {
        return anonymousavatar;
    }

    public void setAnonymousavatar(String anonymousavatar) {
        this.anonymousavatar = anonymousavatar;
    }

    // 匿名标识颜色
    public String getAnonymouscolor() {
        return anonymouscolor;
    }

    public void setAnonymouscolor(String anonymouscolor) {
        this.anonymouscolor = anonymouscolor;
    }

    // 帖子标题
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // 帖子内容（富文本）
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 纯文本内容（用于搜索）
    public String getContenttext() {
        return contenttext;
    }

    public void setContenttext(String contenttext) {
        this.contenttext = contenttext;
    }

    // 图片列表（JSON数组）
    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    // 浏览量
    public Long getViewcount() {
        return viewcount;
    }

    public void setViewcount(Long viewcount) {
        this.viewcount = viewcount;
    }

    // 评论数
    public Long getReplycount() {
        return replycount;
    }

    public void setReplycount(Long replycount) {
        this.replycount = replycount;
    }

    // 点赞数
    public Long getLikecount() {
        return likecount;
    }

    public void setLikecount(Long likecount) {
        this.likecount = likecount;
    }

    // 收藏数
    public Long getFavoritecount() {
        return favoritecount;
    }

    public void setFavoritecount(Long favoritecount) {
        this.favoritecount = favoritecount;
    }

    // 分享数
    public Long getSharecount() {
        return sharecount;
    }

    public void setSharecount(Long sharecount) {
        this.sharecount = sharecount;
    }

    // 举报数
    public Long getReportcount() {
        return reportcount;
    }

    public void setReportcount(Long reportcount) {
        this.reportcount = reportcount;
    }

    // 热度分（用于推荐排序）
    public BigDecimal getHotscore() {
        return hotscore;
    }

    public void setHotscore(BigDecimal hotscore) {
        this.hotscore = hotscore;
    }

    // 是否置顶
    public Integer getIstop() {
        return istop;
    }

    public void setIstop(Integer istop) {
        this.istop = istop;
    }

    // 是否精华
    public Integer getIsessence() {
        return isessence;
    }

    public void setIsessence(Integer isessence) {
        this.isessence = isessence;
    }

    // 状态：1正常 2审核中 3删除
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    // IP哈希（风控）
    public String getIphash() {
        return iphash;
    }

    public void setIphash(String iphash) {
        this.iphash = iphash;
    }

    // 设备哈希（风控）
    public String getDevicehash() {
        return devicehash;
    }

    public void setDevicehash(String devicehash) {
        this.devicehash = devicehash;
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

    // 逻辑删除
    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    // 板块名称
    public String getBoardname() {
        return boardname;
    }

    public void setBoardname(String boardname) {
        this.boardname = boardname;
    }

    // 板块编码
    public String getBoardcode() {
        return boardcode;
    }

    public void setBoardcode(String boardcode) {
        this.boardcode = boardcode;
    }


}

