package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.BoardEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.BoardPojo;

import java.util.List;

/**
 * 社区板块表(Board)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 16:22:10
 */
@Mapper
public interface BoardMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    BoardPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<BoardPojo> getPageList(QueryParam queryParam);

    /**
     * 查询 所有Item
     *
     * @param Pid 筛选条件
     * @return 查询结果
     */
    List<BoardPojo> getList(@Param("Pid") String Pid);


    /**
     * 新增数据
     *
     * @param boardEntity 实例对象
     * @return 影响行数
     */
    int insert(BoardEntity boardEntity);


    /**
     * 修改数据
     *
     * @param boardEntity 实例对象
     * @return 影响行数
     */
    int update(BoardEntity boardEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

