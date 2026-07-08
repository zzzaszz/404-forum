package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.FavoriterecordEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.FavoriterecordPojo;

import java.util.List;

/**
 * 收藏记录表(Favoriterecord)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:17:41
 */
@Mapper
public interface FavoriterecordMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    FavoriterecordPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<FavoriterecordPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param favoriterecordEntity 实例对象
     * @return 影响行数
     */
    int insert(FavoriterecordEntity favoriterecordEntity);


    /**
     * 修改数据
     *
     * @param favoriterecordEntity 实例对象
     * @return 影响行数
     */
    int update(FavoriterecordEntity favoriterecordEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

