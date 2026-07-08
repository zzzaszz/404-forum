package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.AnonymousidentityEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.AnonymousidentityPojo;

import java.util.List;

/**
 * 匿名身份映射表（同帖匿名一致，不同帖匿名不同）(Anonymousidentity)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 15:13:29
 */
@Mapper
public interface AnonymousidentityMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    AnonymousidentityPojo getEntity(@Param("key") String key, @Param("tid") String tid);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<AnonymousidentityPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param anonymousidentityEntity 实例对象
     * @return 影响行数
     */
    int insert(AnonymousidentityEntity anonymousidentityEntity);


    /**
     * 修改数据
     *
     * @param anonymousidentityEntity 实例对象
     * @return 影响行数
     */
    int update(AnonymousidentityEntity anonymousidentityEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key, @Param("tid") String tid);

}

