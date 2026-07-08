package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.UserbehaviorEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UserbehaviorPojo;

import java.util.List;

/**
 * 用户行为表（推荐系统基础）(Userbehavior)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:22:30
 */
@Mapper
public interface UserbehaviorMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    UserbehaviorPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<UserbehaviorPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param userbehaviorEntity 实例对象
     * @return 影响行数
     */
    int insert(UserbehaviorEntity userbehaviorEntity);


    /**
     * 修改数据
     *
     * @param userbehaviorEntity 实例对象
     * @return 影响行数
     */
    int update(UserbehaviorEntity userbehaviorEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

