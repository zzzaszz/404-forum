package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.PosttagPojo;

/**
 * 帖子标签关联表(Posttag)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:19:55
 */
public interface PosttagService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    PosttagPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<PosttagPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param posttagPojo 实例对象
     * @return 实例对象
     */
    PosttagPojo insert(PosttagPojo posttagPojo);

    /**
     * 修改数据
     *
     * @param posttagpojo 实例对象
     * @return 实例对象
     */
    PosttagPojo update(PosttagPojo posttagpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
