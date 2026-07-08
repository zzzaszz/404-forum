package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.CommentEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.CommentPojo;

import java.util.List;

/**
 * 评论表（支持楼中楼结构）(Comment)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:06:44
 */
@Mapper
public interface CommentMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    CommentPojo getEntity(@Param("key") String key, @Param("tid") String tid);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<CommentPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param commentEntity 实例对象
     * @return 影响行数
     */
    int insert(CommentEntity commentEntity);


    /**
     * 修改数据
     *
     * @param commentEntity 实例对象
     * @return 影响行数
     */
    int update(CommentEntity commentEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key, @Param("tid") String tid);

}

