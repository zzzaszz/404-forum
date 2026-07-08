package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.BoardEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.BoardPojo;
import zzz.service.forum.mapper.BoardMapper;
import zzz.service.forum.service.BoardService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 社区板块表(Board)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 16:22:10
 */
@Service("boardService")
public class BoardServiceImpl implements BoardService {
    @Resource
    private BoardMapper boardMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public BoardPojo getEntity(String key) {
        return this.boardMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<BoardPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<BoardPojo> lst = boardMapper.getPageList(queryParam);
            PageInfo<BoardPojo> pageInfo = new PageInfo<BoardPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 查询 所有Item
     *
     * @param Pid 筛选条件
     * @return 查询结果
     */
    @Override
    public List<BoardPojo> getList(String Pid) {
        try {
            List<BoardPojo> lst = boardMapper.getList(Pid);
            return lst;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }


    /**
     * 新增数据
     *
     * @param boardPojo 实例对象
     * @return 实例对象
     */
    @Override
    public BoardPojo insert(BoardPojo boardPojo) {
        //初始化item的NULL
        BoardPojo itempojo = this.clearNull(boardPojo);
        BoardEntity boardEntity = new BoardEntity();
        BeanUtils.copyProperties(itempojo, boardEntity);
        //生成雪花id
        boardEntity.setId(snowflakeIdGenerator.nextId());
        this.boardMapper.insert(boardEntity);
        return this.getEntity(String.valueOf(boardEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param boardPojo 实例对象
     * @return 实例对象
     */
    @Override
    public BoardPojo update(BoardPojo boardPojo) {
        BoardEntity boardEntity = new BoardEntity();
        BeanUtils.copyProperties(boardPojo, boardEntity);
        this.boardMapper.update(boardEntity);
        return this.getEntity(String.valueOf(boardEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.boardMapper.delete(key);
    }

    /**
     * 修改数据
     *
     * @param boardPojo 实例对象
     * @return 实例对象
     */
    @Override
    public BoardPojo clearNull(BoardPojo boardPojo) {
        //初始化NULL字段
        if (boardPojo.getName() == null) boardPojo.setName("");
        if (boardPojo.getCode() == null) boardPojo.setCode("");
        if (boardPojo.getIcon() == null) boardPojo.setIcon("");
        if (boardPojo.getDescription() == null) boardPojo.setDescription("");
        if (boardPojo.getSortorder() == null) boardPojo.setSortorder(0);
        if (boardPojo.getPostcount() == null) boardPojo.setPostcount(0);
        if (boardPojo.getFollowcount() == null) boardPojo.setFollowcount(0);
        if (boardPojo.getStatus() == null) boardPojo.setStatus(0);
        if (boardPojo.getCreatedat() == null) boardPojo.setCreatedat(new Date());
        if (boardPojo.getUpdatedat() == null) boardPojo.setUpdatedat(new Date());
        return boardPojo;
    }
}
