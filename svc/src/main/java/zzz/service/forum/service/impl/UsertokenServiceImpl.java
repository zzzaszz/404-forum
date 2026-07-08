package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.UsertokenEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UsertokenPojo;
import zzz.service.forum.mapper.UsertokenMapper;
import zzz.service.forum.service.UsertokenService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 用户Token表（JWT会话管理）(Usertoken)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:23:06
 */
@Service("usertokenService")
public class UsertokenServiceImpl implements UsertokenService {
    @Resource
    private UsertokenMapper usertokenMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public UsertokenPojo getEntity(String key) {
        return this.usertokenMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<UsertokenPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<UsertokenPojo> lst = usertokenMapper.getPageList(queryParam);
            PageInfo<UsertokenPojo> pageInfo = new PageInfo<UsertokenPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param usertokenPojo 实例对象
     * @return 实例对象
     */
    @Override
    public UsertokenPojo insert(UsertokenPojo usertokenPojo) {
        //初始化NULL字段
        if (usertokenPojo.getAccesstoken() == null) usertokenPojo.setAccesstoken("");
        if (usertokenPojo.getRefreshtoken() == null) usertokenPojo.setRefreshtoken("");
        if (usertokenPojo.getExpireat() == null) usertokenPojo.setExpireat(new Date());
        if (usertokenPojo.getDeviceid() == null) usertokenPojo.setDeviceid("");
        if (usertokenPojo.getIp() == null) usertokenPojo.setIp("");
        if (usertokenPojo.getStatus() == null) usertokenPojo.setStatus(0);
        if (usertokenPojo.getCreatedat() == null) usertokenPojo.setCreatedat(new Date());
        UsertokenEntity usertokenEntity = new UsertokenEntity();
        BeanUtils.copyProperties(usertokenPojo, usertokenEntity);
        //生成雪花id
        usertokenEntity.setId(snowflakeIdGenerator.nextId());

        this.usertokenMapper.insert(usertokenEntity);
        return this.getEntity(String.valueOf(usertokenEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param usertokenPojo 实例对象
     * @return 实例对象
     */
    @Override
    public UsertokenPojo update(UsertokenPojo usertokenPojo) {
        UsertokenEntity usertokenEntity = new UsertokenEntity();
        BeanUtils.copyProperties(usertokenPojo, usertokenEntity);
        this.usertokenMapper.update(usertokenEntity);
        return this.getEntity(String.valueOf(usertokenEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.usertokenMapper.delete(key);
    }


}
