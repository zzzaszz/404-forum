package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.TagPojo;

/**
 * 标签表(Tag)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:22:13
 */
public interface TagService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    TagPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<TagPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param tagPojo 实例对象
     * @return 实例对象
     */
    TagPojo insert(TagPojo tagPojo);

    /**
     * 修改数据
     *
     * @param tagpojo 实例对象
     * @return 实例对象
     */
    TagPojo update(TagPojo tagpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
