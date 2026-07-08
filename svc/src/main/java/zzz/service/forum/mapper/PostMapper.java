package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.PostEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.PostPojo;

import java.util.List;

/**
 * 帖子表（社区核心内容）(Post)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:19:36
 */
@Mapper
public interface PostMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    PostPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<PostPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param postEntity 实例对象
     * @return 影响行数
     */
    int insert(PostEntity postEntity);


    /**
     * 修改数据
     *
     * @param postEntity 实例对象
     * @return 影响行数
     */
    int update(PostEntity postEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

