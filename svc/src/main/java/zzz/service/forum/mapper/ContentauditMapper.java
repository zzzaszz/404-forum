package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.ContentauditEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.ContentauditPojo;

import java.util.List;

/**
 * 内容审核表(Contentaudit)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:17:22
 */
@Mapper
public interface ContentauditMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    ContentauditPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<ContentauditPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param contentauditEntity 实例对象
     * @return 影响行数
     */
    int insert(ContentauditEntity contentauditEntity);


    /**
     * 修改数据
     *
     * @param contentauditEntity 实例对象
     * @return 影响行数
     */
    int update(ContentauditEntity contentauditEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

