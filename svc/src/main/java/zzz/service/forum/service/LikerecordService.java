package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.LikerecordPojo;

/**
 * 点赞记录表（防重复点赞）(Likerecord)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:18:02
 */
public interface LikerecordService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    LikerecordPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<LikerecordPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param likerecordPojo 实例对象
     * @return 实例对象
     */
    LikerecordPojo insert(LikerecordPojo likerecordPojo);

    /**
     * 修改数据
     *
     * @param likerecordpojo 实例对象
     * @return 实例对象
     */
    LikerecordPojo update(LikerecordPojo likerecordpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
