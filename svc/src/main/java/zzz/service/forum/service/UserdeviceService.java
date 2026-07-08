package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UserdevicePojo;

/**
 * 用户设备记录表（风控与设备追踪）(Userdevice)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:22:46
 */
public interface UserdeviceService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    UserdevicePojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<UserdevicePojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param userdevicePojo 实例对象
     * @return 实例对象
     */
    UserdevicePojo insert(UserdevicePojo userdevicePojo);

    /**
     * 修改数据
     *
     * @param userdevicepojo 实例对象
     * @return 实例对象
     */
    UserdevicePojo update(UserdevicePojo userdevicepojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
