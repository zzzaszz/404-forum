package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.ReportEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.ReportPojo;

import java.util.List;

/**
 * 举报记录表(Report)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:21:59
 */
@Mapper
public interface ReportMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    ReportPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<ReportPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param reportEntity 实例对象
     * @return 影响行数
     */
    int insert(ReportEntity reportEntity);


    /**
     * 修改数据
     *
     * @param reportEntity 实例对象
     * @return 影响行数
     */
    int update(ReportEntity reportEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

