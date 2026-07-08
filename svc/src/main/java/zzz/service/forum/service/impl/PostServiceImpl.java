package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.PostEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.PostPojo;
import zzz.service.forum.mapper.PostMapper;
import zzz.service.forum.service.PostService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 帖子表（社区核心内容）(Post)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:19:37
 */
@Service("postService")
public class PostServiceImpl implements PostService {
    @Resource
    private PostMapper postMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public PostPojo getEntity(String key) {
        return this.postMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<PostPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<PostPojo> lst = postMapper.getPageList(queryParam);
            PageInfo<PostPojo> pageInfo = new PageInfo<PostPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param postPojo 实例对象
     * @return 实例对象
     */
    @Override
    public PostPojo insert(PostPojo postPojo) {
        //初始化NULL字段
        if (postPojo.getAnonymousname() == null) postPojo.setAnonymousname("");
        if (postPojo.getAnonymousavatar() == null) postPojo.setAnonymousavatar("");
        if (postPojo.getAnonymouscolor() == null) postPojo.setAnonymouscolor("");
        if (postPojo.getTitle() == null) postPojo.setTitle("");
        if (postPojo.getContent() == null) postPojo.setContent("");
        if (postPojo.getContenttext() == null) postPojo.setContenttext("");
        if (postPojo.getImages() == null) postPojo.setImages("");
        if (postPojo.getIstop() == null) postPojo.setIstop(0);
        if (postPojo.getIsessence() == null) postPojo.setIsessence(0);
        if (postPojo.getStatus() == null) postPojo.setStatus(0);
        if (postPojo.getIphash() == null) postPojo.setIphash("");
        if (postPojo.getDevicehash() == null) postPojo.setDevicehash("");
        if (postPojo.getCreatedat() == null) postPojo.setCreatedat(new Date());
        if (postPojo.getUpdatedat() == null) postPojo.setUpdatedat(new Date());
        if (postPojo.getDeleted() == null) postPojo.setDeleted(0);
        PostEntity postEntity = new PostEntity();
        BeanUtils.copyProperties(postPojo, postEntity);
        //生成雪花id
        postEntity.setId(snowflakeIdGenerator.nextId());

        this.postMapper.insert(postEntity);
        return this.getEntity(String.valueOf(postEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param postPojo 实例对象
     * @return 实例对象
     */
    @Override
    public PostPojo update(PostPojo postPojo) {
        PostEntity postEntity = new PostEntity();
        BeanUtils.copyProperties(postPojo, postEntity);
        this.postMapper.update(postEntity);
        return this.getEntity(String.valueOf(postEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.postMapper.delete(key);
    }


}
