package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.ContentauditEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.ContentauditPojo;
import zzz.service.forum.mapper.ContentauditMapper;
import zzz.service.forum.service.ContentauditService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 内容审核表(Contentaudit)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:17:22
 */
@Service("contentauditService")
public class ContentauditServiceImpl implements ContentauditService {
    @Resource
    private ContentauditMapper contentauditMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public ContentauditPojo getEntity(String key) {
        return this.contentauditMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<ContentauditPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<ContentauditPojo> lst = contentauditMapper.getPageList(queryParam);
            PageInfo<ContentauditPojo> pageInfo = new PageInfo<ContentauditPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param contentauditPojo 实例对象
     * @return 实例对象
     */
    @Override
    public ContentauditPojo insert(ContentauditPojo contentauditPojo) {
        //初始化NULL字段
        if (contentauditPojo.getTargettype() == null) contentauditPojo.setTargettype(0);
        if (contentauditPojo.getAudittype() == null) contentauditPojo.setAudittype(0);
        if (contentauditPojo.getRisklevel() == null) contentauditPojo.setRisklevel(0);
        if (contentauditPojo.getResult() == null) contentauditPojo.setResult(0);
        if (contentauditPojo.getHitwords() == null) contentauditPojo.setHitwords("");
        if (contentauditPojo.getRemark() == null) contentauditPojo.setRemark("");
        if (contentauditPojo.getCreatedat() == null) contentauditPojo.setCreatedat(new Date());
        ContentauditEntity contentauditEntity = new ContentauditEntity();
        BeanUtils.copyProperties(contentauditPojo, contentauditEntity);
        //生成雪花id
        contentauditEntity.setId(snowflakeIdGenerator.nextId());

        this.contentauditMapper.insert(contentauditEntity);
        return this.getEntity(String.valueOf(contentauditEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param contentauditPojo 实例对象
     * @return 实例对象
     */
    @Override
    public ContentauditPojo update(ContentauditPojo contentauditPojo) {
        ContentauditEntity contentauditEntity = new ContentauditEntity();
        BeanUtils.copyProperties(contentauditPojo, contentauditEntity);
        this.contentauditMapper.update(contentauditEntity);
        return this.getEntity(String.valueOf(contentauditEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.contentauditMapper.delete(key);
    }


}
