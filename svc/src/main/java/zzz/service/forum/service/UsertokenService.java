package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UsertokenPojo;

/**
 * 用户Token表（JWT会话管理）(Usertoken)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:23:06
 */
public interface UsertokenService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    UsertokenPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<UsertokenPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param usertokenPojo 实例对象
     * @return 实例对象
     */
    UsertokenPojo insert(UsertokenPojo usertokenPojo);

    /**
     * 修改数据
     *
     * @param usertokenpojo 实例对象
     * @return 实例对象
     */
    UsertokenPojo update(UsertokenPojo usertokenpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
