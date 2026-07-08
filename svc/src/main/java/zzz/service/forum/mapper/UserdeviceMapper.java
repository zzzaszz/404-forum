package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.UserdeviceEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UserdevicePojo;

import java.util.List;

/**
 * 用户设备记录表（风控与设备追踪）(Userdevice)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:22:46
 */
@Mapper
public interface UserdeviceMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    UserdevicePojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<UserdevicePojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param userdeviceEntity 实例对象
     * @return 影响行数
     */
    int insert(UserdeviceEntity userdeviceEntity);


    /**
     * 修改数据
     *
     * @param userdeviceEntity 实例对象
     * @return 影响行数
     */
    int update(UserdeviceEntity userdeviceEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

