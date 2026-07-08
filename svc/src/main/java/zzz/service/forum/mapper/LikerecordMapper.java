package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.LikerecordEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.LikerecordPojo;

import java.util.List;

/**
 * 点赞记录表（防重复点赞）(Likerecord)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:18:02
 */
@Mapper
public interface LikerecordMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    LikerecordPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<LikerecordPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param likerecordEntity 实例对象
     * @return 影响行数
     */
    int insert(LikerecordEntity likerecordEntity);


    /**
     * 修改数据
     *
     * @param likerecordEntity 实例对象
     * @return 影响行数
     */
    int update(LikerecordEntity likerecordEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

