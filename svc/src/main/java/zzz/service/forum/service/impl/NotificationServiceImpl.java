package zzz.service.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zzz.service.forum.domain.NotificationEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.NotificationPojo;
import zzz.service.forum.mapper.NotificationMapper;
import zzz.service.forum.service.NotificationService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 通知表(Notification)表服务实现类
 *
 * @author caoshi
 * @since 2026-05-13 17:18:19
 */
@Service("notificationService")
public class NotificationServiceImpl implements NotificationService {
    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    @Override
    public NotificationPojo getEntity(String key) {
        return this.notificationMapper.getEntity(key);
    }

    /**
     * 分页查询
     *
     * @param queryParam 筛选条件
     * @return 查询结果
     */
    @Override
    public PageInfo<NotificationPojo> getPageList(QueryParam queryParam) {
        PageHelper.startPage(queryParam.getPageNum(), queryParam.getPageSize());
        try {
            List<NotificationPojo> lst = notificationMapper.getPageList(queryParam);
            PageInfo<NotificationPojo> pageInfo = new PageInfo<NotificationPojo>(lst);
            return pageInfo;
        } catch (Exception e) {
            throw new BaseBusinessException(e.getMessage());
        }
    }

    /**
     * 新增数据
     *
     * @param notificationPojo 实例对象
     * @return 实例对象
     */
    @Override
    public NotificationPojo insert(NotificationPojo notificationPojo) {
        //初始化NULL字段
        if (notificationPojo.getType() == null) notificationPojo.setType(0);
        if (notificationPojo.getContent() == null) notificationPojo.setContent("");
        if (notificationPojo.getIsread() == null) notificationPojo.setIsread(0);
        if (notificationPojo.getCreatedat() == null) notificationPojo.setCreatedat(new Date());
        NotificationEntity notificationEntity = new NotificationEntity();
        BeanUtils.copyProperties(notificationPojo, notificationEntity);
        //生成雪花id
        notificationEntity.setId(snowflakeIdGenerator.nextId());

        this.notificationMapper.insert(notificationEntity);
        return this.getEntity(String.valueOf(notificationEntity.getId()));

    }

    /**
     * 修改数据
     *
     * @param notificationPojo 实例对象
     * @return 实例对象
     */
    @Override
    public NotificationPojo update(NotificationPojo notificationPojo) {
        NotificationEntity notificationEntity = new NotificationEntity();
        BeanUtils.copyProperties(notificationPojo, notificationEntity);
        this.notificationMapper.update(notificationEntity);
        return this.getEntity(String.valueOf(notificationEntity.getId()));
    }

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 是否成功
     */
    @Override
    public int delete(String key) {
        return this.notificationMapper.delete(key);
    }


}
