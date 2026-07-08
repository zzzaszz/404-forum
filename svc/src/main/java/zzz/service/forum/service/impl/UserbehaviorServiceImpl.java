package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.UserbehaviorEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UserbehaviorPojo;
import zzz.service.forum.mapper.UserbehaviorMapper;
import zzz.service.forum.service.UserbehaviorService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 用户行为表（推荐系统基础）(Userbehavior)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:22:30
 */
@Service("userbehaviorService")
public class UserbehaviorServiceImpl implements UserbehaviorService {
    @Resource
    private UserbehaviorMapper userbehaviorMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public UserbehaviorPojo getEntity(String key) {
        return this.userbehaviorMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<UserbehaviorPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<UserbehaviorPojo> lst = userbehaviorMapper.getPageList(queryParam);
            PageInfo<UserbehaviorPojo> pageInfo = new PageInfo<UserbehaviorPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param userbehaviorPojo 实例对象
     * @return 实例对象
     */
    @Override
    public UserbehaviorPojo insert(UserbehaviorPojo userbehaviorPojo) {
        //初始化NULL字段
        if (userbehaviorPojo.getBehaviortype() == null) userbehaviorPojo.setBehaviortype(0);
        if (userbehaviorPojo.getTargettype() == null) userbehaviorPojo.setTargettype(0);
        if (userbehaviorPojo.getDuration() == null) userbehaviorPojo.setDuration(0);
        if (userbehaviorPojo.getCreatedat() == null) userbehaviorPojo.setCreatedat(new Date());
        UserbehaviorEntity userbehaviorEntity = new UserbehaviorEntity();
        BeanUtils.copyProperties(userbehaviorPojo, userbehaviorEntity);
        //生成雪花id
        userbehaviorEntity.setId(snowflakeIdGenerator.nextId());

        this.userbehaviorMapper.insert(userbehaviorEntity);
        return this.getEntity(String.valueOf(userbehaviorEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param userbehaviorPojo 实例对象
     * @return 实例对象
     */
    @Override
    public UserbehaviorPojo update(UserbehaviorPojo userbehaviorPojo) {
        UserbehaviorEntity userbehaviorEntity = new UserbehaviorEntity();
        BeanUtils.copyProperties(userbehaviorPojo, userbehaviorEntity);
        this.userbehaviorMapper.update(userbehaviorEntity);
        return this.getEntity(String.valueOf(userbehaviorEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.userbehaviorMapper.delete(key);
    }


}
