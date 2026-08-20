package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.RoomMessageEntity;

import java.util.List;

/**
 * 聊天室消息表(RoomMessage)表数据库访问层
 */
@Mapper
public interface RoomMessageMapper {

    int insert(RoomMessageEntity entity);

    /** 某房间最近 N 条消息（按时间正序） */
    List<RoomMessageEntity> selectRecent(@Param("roomId") Integer roomId, @Param("limit") int limit);
}
