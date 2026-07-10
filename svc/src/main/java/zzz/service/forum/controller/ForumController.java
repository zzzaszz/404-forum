package zzz.service.forum.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.service.ForumService;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 论坛业务聚合控制器（C 端）。
 * <p>
 * 提供话题、评论、互动、元数据、私信、搜索等接口，区别于各实体通用 CRUD。
 *
 * @author caoshi
 * @since 2026-06-30
 */
@Api(tags = "论坛业务")
@RestController
@RequestMapping("/forum")
public class ForumController {

    private static final Logger logger = LoggerFactory.getLogger(ForumController.class);

    @Resource
    private ForumService forumService;

    // ==================== 话题 ====================

    @ApiOperation("话题列表（最新/按板块/搜索）")
    @GetMapping("/topics")
    public R<Map<String, Object>> topics(@RequestParam(required = false) String boardCode,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer pageNum,
                                         @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.listTopics(boardCode, sort, keyword, pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("topics error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("热门话题榜")
    @GetMapping("/hot")
    public R<Object> hot(@RequestParam(required = false) Integer limit) {
        try {
            return R.ok(forumService.hotTopics(limit));
        } catch (Exception e) {
            logger.warn("hot error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("话题详情")
    @GetMapping("/topic/{id}")
    public R<Object> topicDetail(@PathVariable("id") String id) {
        try {
            return R.ok(forumService.topicDetail(id));
        } catch (Exception e) {
            logger.warn("topicDetail error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("发布话题")
    @PostMapping("/topic/create")
    public R<Object> createTopic(@RequestBody Map<String, Object> body) {
        try {
            String boardCode = body.get("boardCode") == null ? null : String.valueOf(body.get("boardCode"));
            String title = body.get("title") == null ? null : String.valueOf(body.get("title"));
            String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
            return R.ok(forumService.createTopic(boardCode, title, content));
        } catch (Exception e) {
            logger.warn("createTopic error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 评论 ====================

    @ApiOperation("帖子评论列表")
    @GetMapping("/topic/{id}/comments")
    public R<Map<String, Object>> comments(@PathVariable("id") String id,
                                           @RequestParam(required = false) Integer pageNum,
                                           @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.listComments(id, pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("comments error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("发表评论")
    @PostMapping("/comment/create")
    public R<Object> createComment(@RequestBody Map<String, Object> body) {
        try {
            String postId = body.get("postId") == null ? null : String.valueOf(body.get("postId"));
            String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
            Long parentId = parseLong(body.get("parentId"));
            Long rootId = parseLong(body.get("rootId"));
            Long replyUserId = parseLong(body.get("replyUserId"));
            return R.ok(forumService.createComment(postId, content, parentId, rootId, replyUserId));
        } catch (Exception e) {
            logger.warn("createComment error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("删除帖子（本人或管理员）")
    @PostMapping("/topic/delete")
    public R<Map<String, Object>> deleteTopic(@RequestBody Map<String, Object> body) {
        try {
            String id = body.get("id") == null ? null : String.valueOf(body.get("id"));
            return R.ok(forumService.deleteTopic(id));
        } catch (Exception e) {
            logger.warn("deleteTopic error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("删除评论（本人或管理员）")
    @PostMapping("/comment/delete")
    public R<Map<String, Object>> deleteComment(@RequestBody Map<String, Object> body) {
        try {
            String id = body.get("id") == null ? null : String.valueOf(body.get("id"));
            return R.ok(forumService.deleteComment(id));
        } catch (Exception e) {
            logger.warn("deleteComment error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 互动 ====================

    @ApiOperation("点赞切换（targetType:1帖子 2评论）")
    @PostMapping("/like")
    public R<Map<String, Object>> like(@RequestBody Map<String, Object> body) {
        try {
            Integer targetType = parseInteger(body.get("targetType"));
            String targetId = body.get("targetId") == null ? null : String.valueOf(body.get("targetId"));
            return R.ok(forumService.toggleLike(targetType, targetId));
        } catch (Exception e) {
            logger.warn("like error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("收藏切换")
    @PostMapping("/favorite")
    public R<Map<String, Object>> favorite(@RequestBody Map<String, Object> body) {
        try {
            String postId = body.get("postId") == null ? null : String.valueOf(body.get("postId"));
            return R.ok(forumService.toggleFavorite(postId));
        } catch (Exception e) {
            logger.warn("favorite error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("举报")
    @PostMapping("/report")
    public R<Map<String, Object>> report(@RequestBody Map<String, Object> body) {
        try {
            Integer targetType = parseInteger(body.get("targetType"));
            String targetId = body.get("targetId") == null ? null : String.valueOf(body.get("targetId"));
            String reason = body.get("reason") == null ? null : String.valueOf(body.get("reason"));
            String description = body.get("description") == null ? null : String.valueOf(body.get("description"));
            return R.ok(forumService.report(targetType, targetId, reason, description));
        } catch (Exception e) {
            logger.warn("report error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 元数据 / 统计 ====================

    @ApiOperation("启用板块列表")
    @GetMapping("/boards")
    public R<Object> boards() {
        try {
            return R.ok(forumService.boards());
        } catch (Exception e) {
            logger.warn("boards error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("统计")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        try {
            return R.ok(forumService.stats());
        } catch (Exception e) {
            logger.warn("stats error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("在线用户")
    @GetMapping("/online")
    public R<Map<String, Object>> online() {
        try {
            return R.ok(forumService.online());
        } catch (Exception e) {
            logger.warn("online error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 私信 / 通知 ====================

    @ApiOperation("我的私信列表")
    @GetMapping("/messages")
    public R<Map<String, Object>> messages(@RequestParam(required = false) Integer pageNum,
                                           @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.messages(pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("messages error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("与某用户的私信历史")
    @GetMapping("/messages/history")
    public R<Map<String, Object>> messageHistory(@RequestParam("toUserId") String toUserId) {
        try {
            return R.ok(forumService.messageHistory(parseLong(toUserId)));
        } catch (Exception e) {
            logger.warn("messageHistory error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("标记与某用户的会话为已读")
    @PostMapping("/messages/read")
    public R<Map<String, Object>> markRead(@RequestBody Map<String, Object> body) {
        try {
            Long toUserId = parseLong(body.get("toUserId"));
            return R.ok(forumService.markRead(toUserId));
        } catch (Exception e) {
            logger.warn("markRead error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("发送私信")
    @PostMapping("/message/send")
    public R<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> body) {
        try {
            Long toUserId = parseLong(body.get("toUserId"));
            String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
            return R.ok(forumService.sendMessage(toUserId, content));
        } catch (Exception e) {
            logger.warn("sendMessage error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 搜索 ====================

    @ApiOperation("搜索话题")
    @GetMapping("/search")
    public R<Map<String, Object>> search(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer pageNum,
                                         @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.search(keyword, pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("search error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 我的内容 ====================

    @ApiOperation("我的发帖")
    @GetMapping("/my/posts")
    public R<Map<String, Object>> myPosts(@RequestParam(required = false) Integer pageNum,
                                          @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.myPosts(pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("myPosts error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("我的评论")
    @GetMapping("/my/comments")
    public R<Map<String, Object>> myComments(@RequestParam(required = false) Integer pageNum,
                                             @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.myComments(pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("myComments error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("我的获赞")
    @GetMapping("/my/likes")
    public R<Map<String, Object>> myLikes(@RequestParam(required = false) Integer pageNum,
                                          @RequestParam(required = false) Integer pageSize) {
        try {
            return R.ok(forumService.myLikes(pageNum, pageSize));
        } catch (Exception e) {
            logger.warn("myLikes error", e);
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("我的统计")
    @GetMapping("/my/stats")
    public R<Map<String, Object>> myStats() {
        try {
            return R.ok(forumService.myStats());
        } catch (Exception e) {
            logger.warn("myStats error", e);
            return R.fail(e.getMessage());
        }
    }

    // ==================== 工具 ====================

    private Long parseLong(Object v) {
        if (v == null) return null;
        try {
            return Long.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(Object v) {
        if (v == null) return null;
        try {
            return Integer.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
