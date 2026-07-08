package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.UsertokenEntity;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.pojo.UsertokenPojo;

import java.util.List;

/**
 * 用户Token表（JWT会话管理）(Usertoken)表数据库访问层
 *
 * @author caoshi
 * @since 2026-05-13 17:23:06
 */
@Mapper
public interface UsertokenMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param key 主键
     * @return 实例对象
     */
    UsertokenPojo getEntity(@Param("key") String key);

    /**
     * 查询指定行数据
     *
     * @param queryParam 查询条件
     * @return 对象列表
     */
    List<UsertokenPojo> getPageList(QueryParam queryParam);


    /**
     * 新增数据
     *
     * @param usertokenEntity 实例对象
     * @return 影响行数
     */
    int insert(UsertokenEntity usertokenEntity);


    /**
     * 修改数据
     *
     * @param usertokenEntity 实例对象
     * @return 影响行数
     */
    int update(UsertokenEntity usertokenEntity);

    /**
     * 通过主键删除数据
     *
     * @param key 主键
     * @return 影响行数
     */
    int delete(@Param("key") String key);

}

