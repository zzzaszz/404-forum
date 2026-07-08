package zzz.service.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zzz.service.forum.domain.pojo.BoardPojo;
import zzz.service.forum.domain.pojo.CommentPojo;
import zzz.service.forum.domain.pojo.NotificationPojo;
import zzz.service.forum.domain.pojo.PostPojo;

import java.util.List;
import java.util.Map;

/**
 * 论坛业务聚合查询 Mapper
 * <p>
 * 面向 C 端的话题/评论/统计等查询，区别于各实体通用 CRUD Mapper。
 *
 * @author caoshi
 * @since 2026-06-30
 */
@Mapper
public interface ForumMapper {

    /**
     * 话题列表（支持板块过滤、关键字、排序）
     *
     * @param boardCode 板块编码（可空）
     * @param keyword   关键字（可空）
     * @param orderSql  排序 SQL 片段（latest / hot）
     * @param offset    偏移
     * @param pageSize  每页数
     */
    List<PostPojo> findTopicList(@Param("boardCode") String boardCode,
                                 @Param("keyword") String keyword,
                                 @Param("orderSql") String orderSql,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    /**
     * 话题总数（与 findTopicList 同条件）
     */
    long countTopicList(@Param("boardCode") String boardCode,
                        @Param("keyword") String keyword);

    /**
     * 热门话题榜
     */
    List<PostPojo> findHotTopics(@Param("limit") int limit);

    /**
     * 帖子详情（含板块名/编码）
     */
    PostPojo findTopicDetail(@Param("id") String id);

    /**
     * 浏览量 +1
     */
    int incrViewCount(@Param("id") String id);

    /**
     * 帖子评论列表（按时间升序）
     */
    List<CommentPojo> findCommentsByPostId(@Param("postId") String postId,
                                           @Param("offset") int offset,
                                           @Param("pageSize") int pageSize);

    /**
     * 帖子评论总数
     */
    long countComments(@Param("postId") String postId);

    /**
     * 新增帖子
     */
    int insertPost(PostPojo post);

    /**
     * 新增评论
     */
    int insertComment(CommentPojo comment);

    /**
     * 帖子评论数 +1
     */
    int incrPostReplyCount(@Param("id") String id);

    /**
     * 帖子点赞数 +delta / -delta
     */
    int incrPostLikeCount(@Param("id") String id, @Param("delta") int delta);

    /**
     * 评论点赞数 +delta
     */
    int incrCommentLikeCount(@Param("id") String id, @Param("delta") int delta);

    /**
     * 帖子收藏数 +delta
     */
    int incrPostFavoriteCount(@Param("id") String id, @Param("delta") int delta);

    /**
     * 帖子举报数 +1
     */
    int incrPostReportCount(@Param("id") String id);

    /**
     * 板块帖子数 +delta
     */
    int incrBoardPostCount(@Param("boardId") String boardId, @Param("delta") int delta);

    /**
     * 启用中的板块列表（按 SortOrder 降序）
     */
    List<BoardPojo> findActiveBoards();

    /**
     * 按 Code 查板块
     */
    BoardPojo findBoardByCode(@Param("code") String code);

    /**
     * 当前用户的私信/通知列表
     */
    List<NotificationPojo> findNotifications(@Param("userId") String userId,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    /**
     * 当前用户未读通知数
     */
    long countUnreadNotifications(@Param("userId") String userId);

    /**
     * 新增通知
     */
    int insertNotification(NotificationPojo notification);

    /**
     * 统计：总帖子数
     */
    long countAllPosts();

    /**
     * 统计：总评论数
     */
    long countAllComments();

    /**
     * 统计：总用户数
     */
    long countAllUsers();

    /**
     * 统计：近 7 天活跃帖子数
     */
    long countRecentActivePosts();

    /**
     * 最近活跃用户（在线用户占位，按最新登录时间）
     */
    List<Map<String, Object>> findRecentUsers(@Param("limit") int limit);

    /**
     * 查询点赞记录主键（存在则返回 Id，不存在返回 null）
     */
    Long findLikeId(@Param("userId") String userId,
                    @Param("targetType") Integer targetType,
                    @Param("targetId") String targetId);

    /**
     * 按用户 + 目标删除点赞记录
     */
    int deleteLike(@Param("userId") String userId,
                   @Param("targetType") Integer targetType,
                   @Param("targetId") String targetId);

    /**
     * 查询收藏记录主键
     */
    Long findFavoriteId(@Param("userId") String userId, @Param("postId") String postId);

    /**
     * 按用户 + 帖子删除收藏记录
     */
    int deleteFavorite(@Param("userId") String userId, @Param("postId") String postId);
}
