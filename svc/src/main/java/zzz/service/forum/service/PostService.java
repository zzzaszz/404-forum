package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.PostPojo;

/**
 * 帖子表（社区核心内容）(Post)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:19:36
 */
public interface PostService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    PostPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<PostPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param postPojo 实例对象
     * @return 实例对象
     */
    PostPojo insert(PostPojo postPojo);

    /**
     * 修改数据
     *
     * @param postpojo 实例对象
     * @return 实例对象
     */
    PostPojo update(PostPojo postpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
