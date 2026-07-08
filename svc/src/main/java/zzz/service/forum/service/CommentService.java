package zzz.service.forum.service;


import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.CommentPojo;

/**
 * 评论表（支持楼中楼结构）(Comment)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:06:44
 */
public interface CommentService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    CommentPojo getEntity(String key, String tid);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<CommentPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param commentPojo 实例对象
     * @return 实例对象
     */
    CommentPojo insert(CommentPojo commentPojo);

    /**
     * 修改数据
     *
     * @param commentpojo 实例对象
     * @return 实例对象
     */
    CommentPojo update(CommentPojo commentpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key, String tid);
}
