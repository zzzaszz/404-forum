package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UserbehaviorPojo;

/**
 * 用户行为表（推荐系统基础）(Userbehavior)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:22:30
 */
public interface UserbehaviorService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    UserbehaviorPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<UserbehaviorPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param userbehaviorPojo 实例对象
     * @return 实例对象
     */
    UserbehaviorPojo insert(UserbehaviorPojo userbehaviorPojo);

    /**
     * 修改数据
     *
     * @param userbehaviorpojo 实例对象
     * @return 实例对象
     */
    UserbehaviorPojo update(UserbehaviorPojo userbehaviorpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
