package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.ReportPojo;

/**
 * 举报记录表(Report)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:21:59
 */
public interface ReportService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    ReportPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<ReportPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param reportPojo 实例对象
     * @return 实例对象
     */
    ReportPojo insert(ReportPojo reportPojo);

    /**
     * 修改数据
     *
     * @param reportpojo 实例对象
     * @return 实例对象
     */
    ReportPojo update(ReportPojo reportpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
