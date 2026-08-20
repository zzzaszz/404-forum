package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.UserFollowEntity;

import java.util.List;

/**
 * 用户关注关系表(UserFollow)表数据库访问层
 */
@Mapper
public interface UserFollowMapper {

    int insert(UserFollowEntity entity);

    int deleteByUserAndFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    /** 我关注的人 */
    List<UserFollowEntity> listByUserId(@Param("userId") Long userId);

    /** 关注我的人（按被关注者ID反查） */
    List<UserFollowEntity> listFollowers(@Param("followUserId") Long followUserId);
}
