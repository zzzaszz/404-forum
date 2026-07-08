package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.TagEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.TagPojo;
import zzz.service.forum.mapper.TagMapper;
import zzz.service.forum.service.TagService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.List;

/**
 * 标签表(Tag)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:22:13
 */
@Service("tagService")
public class TagServiceImpl implements TagService {
    @Resource
    private TagMapper tagMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public TagPojo getEntity(String key) {
        return this.tagMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<TagPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<TagPojo> lst = tagMapper.getPageList(queryParam);
            PageInfo<TagPojo> pageInfo = new PageInfo<TagPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param tagPojo 实例对象
     * @return 实例对象
     */
    @Override
    public TagPojo insert(TagPojo tagPojo) {
        //初始化NULL字段
        if (tagPojo.getName() == null) tagPojo.setName("");
        if (tagPojo.getPostcount() == null) tagPojo.setPostcount(0);
        TagEntity tagEntity = new TagEntity();
        BeanUtils.copyProperties(tagPojo, tagEntity);
        //生成雪花id
        tagEntity.setId(snowflakeIdGenerator.nextId());

        this.tagMapper.insert(tagEntity);
        return this.getEntity(String.valueOf(tagEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param tagPojo 实例对象
     * @return 实例对象
     */
    @Override
    public TagPojo update(TagPojo tagPojo) {
        TagEntity tagEntity = new TagEntity();
        BeanUtils.copyProperties(tagPojo, tagEntity);
        this.tagMapper.update(tagEntity);
        return this.getEntity(String.valueOf(tagEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.tagMapper.delete(key);
    }


}
