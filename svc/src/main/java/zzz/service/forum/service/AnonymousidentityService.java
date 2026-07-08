package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.AnonymousidentityPojo;

/**
 * 匿名身份映射表（同帖匿名一致，不同帖匿名不同）(Anonymousidentity)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 15:13:29
 */
public interface AnonymousidentityService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    AnonymousidentityPojo getEntity(String key, String tid);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<AnonymousidentityPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param anonymousidentityPojo 实例对象
     * @return 实例对象
     */
    AnonymousidentityPojo insert(AnonymousidentityPojo anonymousidentityPojo);

    /**
     * 修改数据
     *
     * @param anonymousidentitypojo 实例对象
     * @return 实例对象
     */
    AnonymousidentityPojo update(AnonymousidentityPojo anonymousidentitypojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key, String tid);
}
