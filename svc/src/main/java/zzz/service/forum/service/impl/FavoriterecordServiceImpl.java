package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.FavoriterecordEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.FavoriterecordPojo;
import zzz.service.forum.mapper.FavoriterecordMapper;
import zzz.service.forum.service.FavoriterecordService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 收藏记录表(Favoriterecord)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:17:41
 */
@Service("favoriterecordService")
public class FavoriterecordServiceImpl implements FavoriterecordService {
    @Resource
    private FavoriterecordMapper favoriterecordMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public FavoriterecordPojo getEntity(String key) {
        return this.favoriterecordMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<FavoriterecordPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<FavoriterecordPojo> lst = favoriterecordMapper.getPageList(queryParam);
            PageInfo<FavoriterecordPojo> pageInfo = new PageInfo<FavoriterecordPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param favoriterecordPojo 实例对象
     * @return 实例对象
     */
    @Override
    public FavoriterecordPojo insert(FavoriterecordPojo favoriterecordPojo) {
        //初始化NULL字段
        if (favoriterecordPojo.getCreatedat() == null) favoriterecordPojo.setCreatedat(new Date());
        FavoriterecordEntity favoriterecordEntity = new FavoriterecordEntity();
        BeanUtils.copyProperties(favoriterecordPojo, favoriterecordEntity);
        //生成雪花id
        favoriterecordEntity.setId(snowflakeIdGenerator.nextId());

        this.favoriterecordMapper.insert(favoriterecordEntity);
        return this.getEntity(String.valueOf(favoriterecordEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param favoriterecordPojo 实例对象
     * @return 实例对象
     */
    @Override
    public FavoriterecordPojo update(FavoriterecordPojo favoriterecordPojo) {
        FavoriterecordEntity favoriterecordEntity = new FavoriterecordEntity();
        BeanUtils.copyProperties(favoriterecordPojo, favoriterecordEntity);
        this.favoriterecordMapper.update(favoriterecordEntity);
        return this.getEntity(String.valueOf(favoriterecordEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.favoriterecordMapper.delete(key);
    }


}
