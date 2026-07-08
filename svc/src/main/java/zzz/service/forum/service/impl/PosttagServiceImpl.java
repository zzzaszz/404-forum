package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.PosttagEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.PosttagPojo;
import zzz.service.forum.mapper.PosttagMapper;
import zzz.service.forum.service.PosttagService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.List;

/**
 * 帖子标签关联表(Posttag)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:19:55
 */
@Service("posttagService")
public class PosttagServiceImpl implements PosttagService {
    @Resource
    private PosttagMapper posttagMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public PosttagPojo getEntity(String key) {
        return this.posttagMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<PosttagPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<PosttagPojo> lst = posttagMapper.getPageList(queryParam);
            PageInfo<PosttagPojo> pageInfo = new PageInfo<PosttagPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param posttagPojo 实例对象
     * @return 实例对象
     */
    @Override
    public PosttagPojo insert(PosttagPojo posttagPojo) {
        //初始化NULL字段
        PosttagEntity posttagEntity = new PosttagEntity();
        BeanUtils.copyProperties(posttagPojo, posttagEntity);
        //生成雪花id
        posttagEntity.setId(snowflakeIdGenerator.nextId());

        this.posttagMapper.insert(posttagEntity);
        return this.getEntity(String.valueOf(posttagEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param posttagPojo 实例对象
     * @return 实例对象
     */
    @Override
    public PosttagPojo update(PosttagPojo posttagPojo) {
        PosttagEntity posttagEntity = new PosttagEntity();
        BeanUtils.copyProperties(posttagPojo, posttagEntity);
        this.posttagMapper.update(posttagEntity);
        return this.getEntity(String.valueOf(posttagEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.posttagMapper.delete(key);
    }


}
