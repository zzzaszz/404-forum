package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 用户Token表（JWT会话管理）(Usertoken)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:23:06
 */
public class UsertokenPojo implements Serializable {
    private static final long serialVersionUID = -89506440823286354L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 用户ID
    @Excel(name = "用户ID")
    private Long userid;
    // JWT访问令牌
    @Excel(name = "JWT访问令牌")
    private String accesstoken;
    // 刷新令牌（用于续期）
    @Excel(name = "刷新令牌（用于续期）")
    private String refreshtoken;
    // AccessToken过期时间
    @Excel(name = "AccessToken过期时间")
    private Date expireat;
    // 设备ID
    @Excel(name = "设备ID")
    private String deviceid;
    // 登录IP
    @Excel(name = "登录IP")
    private String ip;
    // 状态：1有效 0失效
    @Excel(name = "状态：1有效 0失效")
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

    // 用户ID
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    // JWT访问令牌
    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

    // 刷新令牌（用于续期）
    public String getRefreshtoken() {
        return refreshtoken;
    }

    public void setRefreshtoken(String refreshtoken) {
        this.refreshtoken = refreshtoken;
    }

    // AccessToken过期时间
    public Date getExpireat() {
        return expireat;
    }

    public void setExpireat(Date expireat) {
        this.expireat = expireat;
    }

    // 设备ID
    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    // 登录IP
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    // 状态：1有效 0失效
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

