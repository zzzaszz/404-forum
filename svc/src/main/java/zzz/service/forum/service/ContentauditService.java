package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.ContentauditPojo;

/**
 * 内容审核表(Contentaudit)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:17:22
 */
public interface ContentauditService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    ContentauditPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<ContentauditPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param contentauditPojo 实例对象
     * @return 实例对象
     */
    ContentauditPojo insert(ContentauditPojo contentauditPojo);

    /**
     * 修改数据
     *
     * @param contentauditpojo 实例对象
     * @return 实例对象
     */
    ContentauditPojo update(ContentauditPojo contentauditpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
