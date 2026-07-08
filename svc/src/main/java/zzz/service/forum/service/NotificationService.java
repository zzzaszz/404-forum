package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.NotificationPojo;

/**
 * 通知表(Notification)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:18:19
 */
public interface NotificationService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    NotificationPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<NotificationPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param notificationPojo 实例对象
     * @return 实例对象
     */
    NotificationPojo insert(NotificationPojo notificationPojo);

    /**
     * 修改数据
     *
     * @param notificationpojo 实例对象
     * @return 实例对象
     */
    NotificationPojo update(NotificationPojo notificationpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
