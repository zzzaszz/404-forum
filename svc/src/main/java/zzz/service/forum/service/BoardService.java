package zzz.service.forum.service;

import com.github.pagehelper.PageInfo;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.BoardPojo;

import java.util.List;

/**
 * 社区板块表(Board)表服务接口
 *
 * @author caoshi
 * @since 2026-05-13 16:22:10
 */
public interface BoardService {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    BoardPojo getEntity(String key);

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    PageInfo<BoardPojo> getPageList(QueryParam queryParam);

    /**
     * 查询 所有Item
     *
     * @param Pid 筛选条件
     * @return 查询结果
     */
    List<BoardPojo> getList(String Pid);

    /**
     * 新增数据
     *
     * @param boardPojo 实例对象
     * @return 实例对象
     */
    BoardPojo insert(BoardPojo boardPojo);

    /**
     * 修改数据
     *
     * @param boardpojo 实例对象
     * @return 实例对象
     */
    BoardPojo update(BoardPojo boardpojo);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    int delete(String key);

    /**
     * 修改数据
     *
     * @param boardpojo 实例对象
     * @return 实例对象
     */
    BoardPojo clearNull(BoardPojo boardpojo);
}
