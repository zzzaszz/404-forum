package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.FavoriterecordPojo;

/**
 * 收藏记录表(Favoriterecord)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 17:17:41
 */
public interface FavoriterecordService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    FavoriterecordPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<FavoriterecordPojo> getPageList(QueryParam queryParam);

    /**
     * 新增数据
     *
     * @param favoriterecordPojo 实例对象
     * @return 实例对象
     */
    FavoriterecordPojo insert(FavoriterecordPojo favoriterecordPojo);

    /**
     * 修改数据
     *
     * @param favoriterecordpojo 实例对象
     * @return 实例对象
     */
    FavoriterecordPojo update(FavoriterecordPojo favoriterecordpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);
}
