package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.UserdeviceEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UserdevicePojo;
import zzz.service.forum.mapper.UserdeviceMapper;
import zzz.service.forum.service.UserdeviceService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 用户设备记录表（风控与设备追踪）(Userdevice)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:22:46
 */
@Service("userdeviceService")
public class UserdeviceServiceImpl implements UserdeviceService {
    @Resource
    private UserdeviceMapper userdeviceMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public UserdevicePojo getEntity(String key) {
        return this.userdeviceMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<UserdevicePojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<UserdevicePojo> lst = userdeviceMapper.getPageList(queryParam);
            PageInfo<UserdevicePojo> pageInfo = new PageInfo<UserdevicePojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param userdevicePojo 实例对象
     * @return 实例对象
     */
    @Override
    public UserdevicePojo insert(UserdevicePojo userdevicePojo) {
        //初始化NULL字段
        if (userdevicePojo.getDeviceid() == null) userdevicePojo.setDeviceid("");
        if (userdevicePojo.getDevicefingerprint() == null) userdevicePojo.setDevicefingerprint("");
        if (userdevicePojo.getOs() == null) userdevicePojo.setOs("");
        if (userdevicePojo.getBrowser() == null) userdevicePojo.setBrowser("");
        if (userdevicePojo.getIp() == null) userdevicePojo.setIp("");
        if (userdevicePojo.getUseragent() == null) userdevicePojo.setUseragent("");
        if (userdevicePojo.getCreatedat() == null) userdevicePojo.setCreatedat(new Date());
        UserdeviceEntity userdeviceEntity = new UserdeviceEntity();
        BeanUtils.copyProperties(userdevicePojo, userdeviceEntity);
        //生成雪花id
        userdeviceEntity.setId(snowflakeIdGenerator.nextId());

        this.userdeviceMapper.insert(userdeviceEntity);
        return this.getEntity(String.valueOf(userdeviceEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param userdevicePojo 实例对象
     * @return 实例对象
     */
    @Override
    public UserdevicePojo update(UserdevicePojo userdevicePojo) {
        UserdeviceEntity userdeviceEntity = new UserdeviceEntity();
        BeanUtils.copyProperties(userdevicePojo, userdeviceEntity);
        this.userdeviceMapper.update(userdeviceEntity);
        return this.getEntity(String.valueOf(userdeviceEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.userdeviceMapper.delete(key);
    }


}
