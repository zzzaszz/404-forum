package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.AnonymousidentityEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.AnonymousidentityPojo;
import zzz.service.forum.mapper.AnonymousidentityMapper;
import zzz.service.forum.service.AnonymousidentityService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 匿名身份映射表（同帖匿名一致，不同帖匿名不同）(Anonymousidentity)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 15:13:29
 */
@Service("anonymousidentityService")
public class AnonymousidentityServiceImpl implements AnonymousidentityService {
    @Resource
    private AnonymousidentityMapper anonymousidentityMapper;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public AnonymousidentityPojo getEntity(String key, String tid) {
        return this.anonymousidentityMapper.getEntity(key, tid);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<AnonymousidentityPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<AnonymousidentityPojo> lst = anonymousidentityMapper.getPageList(queryParam);
            PageInfo<AnonymousidentityPojo> pageInfo = new PageInfo<AnonymousidentityPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param anonymousidentityPojo 实例对象
     * @return 实例对象
     */
    @Override
    public AnonymousidentityPojo insert(AnonymousidentityPojo anonymousidentityPojo) {
        //初始化NULL字段
        if (anonymousidentityPojo.getAnonymousname() == null) anonymousidentityPojo.setAnonymousname("");
        if (anonymousidentityPojo.getAnonymousavatar() == null) anonymousidentityPojo.setAnonymousavatar("");
        if (anonymousidentityPojo.getAnonymouscolor() == null) anonymousidentityPojo.setAnonymouscolor("");
        if (anonymousidentityPojo.getCreatedat() == null) anonymousidentityPojo.setCreatedat(new Date());
        AnonymousidentityEntity anonymousidentityEntity = new AnonymousidentityEntity();
        BeanUtils.copyProperties(anonymousidentityPojo, anonymousidentityEntity);
        //生成雪花id
        anonymousidentityEntity.setId(snowflakeIdGenerator.nextId());
        this.anonymousidentityMapper.insert(anonymousidentityEntity);
        return this.getEntity(String.valueOf(anonymousidentityEntity.getId()), "anonymousidentityEntity.getTenantid()");

    }

    /**
     * 修改数据
     *
     * @param anonymousidentityPojo 实例对象
     * @return 实例对象
     */
    @Override
    public AnonymousidentityPojo update(AnonymousidentityPojo anonymousidentityPojo) {
        AnonymousidentityEntity anonymousidentityEntity = new AnonymousidentityEntity();
        BeanUtils.copyProperties(anonymousidentityPojo, anonymousidentityEntity);
        this.anonymousidentityMapper.update(anonymousidentityEntity);
        return this.getEntity(String.valueOf(anonymousidentityEntity.getId()), "anonymousidentityEntity.getTenantid()");
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key, String tid) {
        return this.anonymousidentityMapper.delete(key, tid);
    }


}
