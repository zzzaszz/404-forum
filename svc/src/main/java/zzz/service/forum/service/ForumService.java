package zzz.service.forum.service;

import zzz.service.forum.domain.pojo.BoardPojo;
import zzz.service.forum.domain.pojo.CommentPojo;
import zzz.service.forum.domain.pojo.PostPojo;

import java.util.List;
import java.util.Map;

/**
 * 论坛业务聚合服务：面向 C 端的话题 / 评论 / 互动 / 统计 / 私信 / 搜索。
 *
 * @author caoshi
 * @since 2026-06-30
 */
public interface ForumService {

    /**
     * 话题列表（最新 / 按板块 / 关键字）
     */
    Map<String, Object> listTopics(String boardCode, String sort, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 热门话题榜
     */
    List<PostPojo> hotTopics(Integer limit);

    /**
     * 话题详情（自增浏览量）
     */
    PostPojo topicDetail(String id);

    /**
     * 发布话题
     */
    PostPojo createTopic(String boardCode, String title, String content);

    /**
     * 帖子评论列表
     */
    Map<String, Object> listComments(String postId, Integer pageNum, Integer pageSize);

    /**
     * 发表评论
     */
    CommentPojo createComment(String postId, String content, Long parentId, Long rootId);

    /**
     * 点赞切换（targetType: 1帖子 2评论）
     */
    Map<String, Object> toggleLike(Integer targetType, String targetId);

    /**
     * 收藏切换
     */
    Map<String, Object> toggleFavorite(String postId);

    /**
     * 举报
     */
    Map<String, Object> report(Integer targetType, String targetId, String reason, String description);

    /**
     * 启用板块列表
     */
    List<BoardPojo> boards();

    /**
     * 统计
     */
    Map<String, Object> stats();

    /**
     * 在线用户
     */
    Map<String, Object> online();

    /**
     * 我的私信 / 通知
     */
    Map<String, Object> messages(Integer pageNum, Integer pageSize);

    /**
     * 发送私信
     */
    Map<String, Object> sendMessage(Long toUserId, String content);

    /**
     * 搜索话题
     */
    Map<String, Object> search(String keyword, Integer pageNum, Integer pageSize);
}
