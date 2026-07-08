package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.NotificationEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.NotificationPojo;

import java.util.List;

/**
 * 通知表(Notification)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:18:19
 */
@Mapper
public interface NotificationMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    NotificationPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<NotificationPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param notificationEntity 实例对象
     * @return 影响行数
     */
    int insert(NotificationEntity notificationEntity);


    /**
     * 修改数据
     *
     * @param notificationEntity 实例对象
     * @return 影响行数
     */
    int update(NotificationEntity notificationEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

