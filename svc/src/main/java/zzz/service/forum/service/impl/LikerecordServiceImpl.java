package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.LikerecordEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.LikerecordPojo;
import zzz.service.forum.mapper.LikerecordMapper;
import zzz.service.forum.service.LikerecordService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 点赞记录表（防重复点赞）(Likerecord)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:18:02
 */
@Service("likerecordService")
public class LikerecordServiceImpl implements LikerecordService {
    @Resource
    private LikerecordMapper likerecordMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public LikerecordPojo getEntity(String key) {
        return this.likerecordMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<LikerecordPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<LikerecordPojo> lst = likerecordMapper.getPageList(queryParam);
            PageInfo<LikerecordPojo> pageInfo = new PageInfo<LikerecordPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param likerecordPojo 实例对象
     * @return 实例对象
     */
    @Override
    public LikerecordPojo insert(LikerecordPojo likerecordPojo) {
        //初始化NULL字段
        if (likerecordPojo.getTargettype() == null) likerecordPojo.setTargettype(0);
        if (likerecordPojo.getCreatedat() == null) likerecordPojo.setCreatedat(new Date());
        LikerecordEntity likerecordEntity = new LikerecordEntity();
        BeanUtils.copyProperties(likerecordPojo, likerecordEntity);
        //生成雪花id
        likerecordEntity.setId(snowflakeIdGenerator.nextId());

        this.likerecordMapper.insert(likerecordEntity);
        return this.getEntity(String.valueOf(likerecordEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param likerecordPojo 实例对象
     * @return 实例对象
     */
    @Override
    public LikerecordPojo update(LikerecordPojo likerecordPojo) {
        LikerecordEntity likerecordEntity = new LikerecordEntity();
        BeanUtils.copyProperties(likerecordPojo, likerecordEntity);
        this.likerecordMapper.update(likerecordEntity);
        return this.getEntity(String.valueOf(likerecordEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.likerecordMapper.delete(key);
    }


}
