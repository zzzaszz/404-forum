package zzz.service.forum.domain.pojo;

import java.util.Date;
import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 用户设备记录表（风控与设备追踪）(Userdevice)实体类
 *
 * @author caoshi
 * @since 2026-05-13 17:22:46
 */
public class UserdevicePojo implements Serializable {
    private static final long serialVersionUID = 372639489117877845L;
    // 主键ID
    @Excel(name = "主键ID")
    private Long id;
    // 用户ID（关联User表）
    @Excel(name = "用户ID（关联User表）")
    private Long userid;
    // 设备唯一ID
    @Excel(name = "设备唯一ID")
    private String deviceid;
    // 设备指纹（风控识别）
    @Excel(name = "设备指纹（风控识别）")
    private String devicefingerprint;
    // 操作系统类型
    @Excel(name = "操作系统类型")
    private String os;
    // 浏览器信息
    @Excel(name = "浏览器信息")
    private String browser;
    // 登录IP地址
    @Excel(name = "登录IP地址")
    private String ip;
    // UA信息
    @Excel(name = "UA信息")
    private String useragent;
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

    // 用户ID（关联User表）
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    // 设备唯一ID
    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    // 设备指纹（风控识别）
    public String getDevicefingerprint() {
        return devicefingerprint;
    }

    public void setDevicefingerprint(String devicefingerprint) {
        this.devicefingerprint = devicefingerprint;
    }

    // 操作系统类型
    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    // 浏览器信息
    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    // 登录IP地址
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    // UA信息
    public String getUseragent() {
        return useragent;
    }

    public void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    // 创建时间
    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }


}

