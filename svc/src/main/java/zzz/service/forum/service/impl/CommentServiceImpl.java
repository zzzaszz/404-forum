package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.CommentEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.CommentPojo;
import zzz.service.forum.mapper.CommentMapper;
import zzz.service.forum.service.CommentService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 评论表（支持楼中楼结构）(Comment)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:06:45
 */
@Service("commentService")
public class CommentServiceImpl implements CommentService {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public CommentPojo getEntity(String key, String tid) {
        return this.commentMapper.getEntity(key, tid);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<CommentPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<CommentPojo> lst = commentMapper.getPageList(queryParam);
            PageInfo<CommentPojo> pageInfo = new PageInfo<CommentPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param commentPojo 实例对象
     * @return 实例对象
     */
    @Override
    public CommentPojo insert(CommentPojo commentPojo) {
        //初始化NULL字段
        if (commentPojo.getAnonymousname() == null) commentPojo.setAnonymousname("");
        if (commentPojo.getContent() == null) commentPojo.setContent("");
        if (commentPojo.getStatus() == null) commentPojo.setStatus(0);
        if (commentPojo.getCreatedat() == null) commentPojo.setCreatedat(new Date());
        CommentEntity commentEntity = new CommentEntity();
        BeanUtils.copyProperties(commentPojo, commentEntity);
        //生成雪花id
        commentEntity.setId(snowflakeIdGenerator.nextId());
        this.commentMapper.insert(commentEntity);
        return this.getEntity(String.valueOf(commentEntity.getId()), "commentEntity.getTenantid()");

    }

    /**
     * 修改数据
     *
     * @param commentPojo 实例对象
     * @return 实例对象
     */
    @Override
    public CommentPojo update(CommentPojo commentPojo) {
        CommentEntity commentEntity = new CommentEntity();
        BeanUtils.copyProperties(commentPojo, commentEntity);
        this.commentMapper.update(commentEntity);
        return this.getEntity(String.valueOf(commentEntity.getId()), "commentEntity.getTenantid()");
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key, String tid) {
        return this.commentMapper.delete(key, tid);
    }


}
