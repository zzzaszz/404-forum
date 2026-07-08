package zzz.service.forum.service.impl;

import org.springframework.stereotype.Service;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.FavoriterecordEntity;
import zzz.service.forum.domain.LikerecordEntity;
import zzz.service.forum.domain.NotificationEntity;
import zzz.service.forum.domain.ReportEntity;
import zzz.service.forum.domain.pojo.BoardPojo;
import zzz.service.forum.domain.pojo.CommentPojo;
import zzz.service.forum.domain.pojo.NotificationPojo;
import zzz.service.forum.domain.pojo.PostPojo;
import zzz.service.forum.mapper.FavoriterecordMapper;
import zzz.service.forum.mapper.ForumMapper;
import zzz.service.forum.mapper.LikerecordMapper;
import zzz.service.forum.mapper.NotificationMapper;
import zzz.service.forum.mapper.ReportMapper;
import zzz.service.forum.service.ForumService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 论坛业务聚合服务实现。
 *
 * @author caoshi
 * @since 2026-06-30
 */
@Service("forumService")
public class ForumServiceImpl implements ForumService {

    // 匿名昵称池
    private static final String[] NAME_POOL = {
            "匿名旅人", "潜水观察员", "夜猫子", "树洞精灵", "云上漫步", "匿名#A3F",
            "匿名#B72", "匿名#C91", "匿名#D45", "匿名#E28", "匿名#F16", "匿名#G33"
    };
    // 头像颜色池
    private static final String[] COLOR_POOL = {
            "#7C3AED", "#3B82F6", "#16A34A", "#EA580C", "#F59E0B", "#EF4444",
            "#0EA5E9", "#8B5CF6", "#EC4899", "#14B8A6"
    };
    // 头像 emoji 池
    private static final String[] AVATAR_POOL = {
            "🦊", "🐼", "🐱", "🐧", "🦉", "🐢", "🦄", "🐙", "🦖", "🐳"
    };
    @Resource
    private ForumMapper forumMapper;
    @Resource
    private LikerecordMapper likerecordMapper;
    @Resource
    private FavoriterecordMapper favoriterecordMapper;
    @Resource
    private ReportMapper reportMapper;
    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private String currentUserId() {
        Long uid = UserContext.getUserId();
        return uid == null ? null : String.valueOf(uid);
    }

    private void requireLogin() {
        if (currentUserId() == null) {
            throw new BaseBusinessException("请先登录");
        }
    }

    private String randomFrom(String[] pool) {
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    @Override
    public Map<String, Object> listTopics(String boardCode, String sort, String keyword,
                                          Integer pageNum, Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String orderSql = "hot".equalsIgnoreCase(sort)
                ? "Post.HotScore DESC, Post.CreatedAt DESC"
                : "Post.IsTop DESC, Post.CreatedAt DESC";
        int offset = (page - 1) * size;

        List<PostPojo> list = forumMapper.findTopicList(boardCode, keyword, orderSql, offset, size);
        long total = forumMapper.countTopicList(boardCode, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", page);
        result.put("pageSize", size);
        return result;
    }

    @Override
    public List<PostPojo> hotTopics(Integer limit) {
        int n = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        return forumMapper.findHotTopics(n);
    }

    @Override
    public PostPojo topicDetail(String id) {
        PostPojo post = forumMapper.findTopicDetail(id);
        if (post == null) {
            throw new BaseBusinessException("话题不存在或已删除");
        }
        forumMapper.incrViewCount(id);
        // 返回前手动 +1 浏览量，保证展示与库一致
        if (post.getViewcount() == null) {
            post.setViewcount(1L);
        } else {
            post.setViewcount(post.getViewcount() + 1);
        }
        return post;
    }

    @Override
    public PostPojo createTopic(String boardCode, String title, String content) {
        requireLogin();
        if (title == null || title.trim().isEmpty()) {
            throw new BaseBusinessException("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BaseBusinessException("内容不能为空");
        }

        BoardPojo board = forumMapper.findBoardByCode(boardCode);
        if (board == null) {
            throw new BaseBusinessException("板块不存在");
        }

        PostPojo post = new PostPojo();
        post.setId(snowflakeIdGenerator.nextId());
        post.setBoardid(board.getId());
        post.setUserid(UserContext.getUserId());
        post.setAnonymousname(randomFrom(NAME_POOL));
        post.setAnonymousavatar(randomFrom(AVATAR_POOL));
        post.setAnonymouscolor(randomFrom(COLOR_POOL));
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setContenttext(content.trim());
        post.setImages("");
        post.setViewcount(0L);
        post.setReplycount(0L);
        post.setLikecount(0L);
        post.setFavoritecount(0L);
        post.setSharecount(0L);
        post.setReportcount(0L);
        post.setHotscore(new java.math.BigDecimal(0));
        post.setIstop(0);
        post.setIsessence(0);
        post.setStatus(1);
        post.setIphash("");
        post.setDevicehash("");
        post.setCreatedat(new Date());
        post.setUpdatedat(new Date());
        post.setDeleted(0);

        forumMapper.insertPost(post);
        forumMapper.incrBoardPostCount(String.valueOf(board.getId()), 1);
        post.setBoardname(board.getName());
        post.setBoardcode(board.getCode());
        return post;
    }

    @Override
    public Map<String, Object> listComments(String postId, Integer pageNum, Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 50 : Math.min(pageSize, 200);
        int offset = (page - 1) * size;

        List<CommentPojo> list = forumMapper.findCommentsByPostId(postId, offset, size);
        long total = forumMapper.countComments(postId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", page);
        result.put("pageSize", size);
        return result;
    }

    @Override
    public CommentPojo createComment(String postId, String content, Long parentId, Long rootId) {
        requireLogin();
        if (content == null || content.trim().isEmpty()) {
            throw new BaseBusinessException("评论内容不能为空");
        }
        PostPojo post = forumMapper.findTopicDetail(postId);
        if (post == null) {
            throw new BaseBusinessException("帖子不存在或已删除");
        }

        CommentPojo comment = new CommentPojo();
        comment.setId(snowflakeIdGenerator.nextId());
        comment.setPostid(Long.valueOf(postId));
        comment.setUserid(UserContext.getUserId());
        comment.setRootid(rootId == null ? 0L : rootId);
        comment.setParentid(parentId == null ? 0L : parentId);
        comment.setReplyuserid(0L);
        comment.setAnonymousidentityid(null);
        comment.setAnonymousname(randomFrom(NAME_POOL));
        comment.setContent(content.trim());
        comment.setLikecount(0L);
        comment.setReplycount(0L);
        comment.setStatus(1);
        comment.setCreatedat(new Date());

        forumMapper.insertComment(comment);
        forumMapper.incrPostReplyCount(postId);
        return comment;
    }

    @Override
    public Map<String, Object> toggleLike(Integer targetType, String targetId) {
        requireLogin();
        if (targetType == null || (targetType != 1 && targetType != 2)) {
            throw new BaseBusinessException("目标类型无效");
        }
        String userId = currentUserId();
        Long existId = forumMapper.findLikeId(userId, targetType, targetId);
        boolean liked;
        if (existId != null) {
            forumMapper.deleteLike(userId, targetType, targetId);
            if (targetType == 1) {
                forumMapper.incrPostLikeCount(targetId, -1);
            } else {
                forumMapper.incrCommentLikeCount(targetId, -1);
            }
            liked = false;
        } else {
            LikerecordEntity record = new LikerecordEntity();
            record.setId(snowflakeIdGenerator.nextId());
            record.setUserid(Long.valueOf(userId));
            record.setTargettype(targetType);
            record.setTargetid(Long.valueOf(targetId));
            record.setCreatedat(new Date());
            likerecordMapper.insert(record);
            if (targetType == 1) {
                forumMapper.incrPostLikeCount(targetId, 1);
            } else {
                forumMapper.incrCommentLikeCount(targetId, 1);
            }
            liked = true;
        }

        long count = 0;
        if (targetType == 1) {
            PostPojo p = forumMapper.findTopicDetail(targetId);
            count = p == null ? 0 : (p.getLikecount() == null ? 0 : p.getLikecount());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("count", count);
        return result;
    }

    @Override
    public Map<String, Object> toggleFavorite(String postId) {
        requireLogin();
        String userId = currentUserId();
        Long existId = forumMapper.findFavoriteId(userId, postId);
        boolean favorited;
        if (existId != null) {
            forumMapper.deleteFavorite(userId, postId);
            forumMapper.incrPostFavoriteCount(postId, -1);
            favorited = false;
        } else {
            FavoriterecordEntity record = new FavoriterecordEntity();
            record.setId(snowflakeIdGenerator.nextId());
            record.setUserid(Long.valueOf(userId));
            record.setPostid(Long.valueOf(postId));
            record.setCreatedat(new Date());
            favoriterecordMapper.insert(record);
            forumMapper.incrPostFavoriteCount(postId, 1);
            favorited = true;
        }

        PostPojo p = forumMapper.findTopicDetail(postId);
        long count = p == null ? 0 : (p.getFavoritecount() == null ? 0 : p.getFavoritecount());
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        result.put("count", count);
        return result;
    }

    @Override
    public Map<String, Object> report(Integer targetType, String targetId, String reason, String description) {
        requireLogin();
        if (reason == null || reason.trim().isEmpty()) {
            throw new BaseBusinessException("举报原因不能为空");
        }
        ReportEntity report = new ReportEntity();
        report.setId(snowflakeIdGenerator.nextId());
        report.setReportuserid(UserContext.getUserId());
        report.setTargettype(targetType == null ? 1 : targetType);
        report.setTargetid(Long.valueOf(targetId));
        report.setReason(reason.trim());
        report.setDescription(description == null ? "" : description.trim());
        report.setStatus(0);
        report.setCreatedat(new Date());
        reportMapper.insert(report);

        if (report.getTargettype() == 1) {
            forumMapper.incrPostReportCount(targetId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", report.getId());
        return result;
    }

    @Override
    public List<BoardPojo> boards() {
        return forumMapper.findActiveBoards();
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> data = new HashMap<>();
        long totalTopics = forumMapper.countAllPosts();
        long totalComments = forumMapper.countAllComments();
        long totalUsers = forumMapper.countAllUsers();
        long activeTopics = forumMapper.countRecentActivePosts();
        long online = Math.max(8L, totalUsers / 10 + 8);

        data.put("onlineCount", online);
        data.put("activeTopics", activeTopics);
        data.put("totalTopics", totalTopics);
        data.put("totalComments", totalComments);
        data.put("totalUsers", totalUsers);
        return data;
    }

    @Override
    public Map<String, Object> online() {
        List<Map<String, Object>> users = forumMapper.findRecentUsers(8);
        Map<String, Object> data = new HashMap<>();
        data.put("count", Math.max(8, users.size()));
        data.put("users", users);
        return data;
    }

    @Override
    public Map<String, Object> messages(Integer pageNum, Integer pageSize) {
        requireLogin();
        String userId = currentUserId();
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (page - 1) * size;

        List<NotificationPojo> list = forumMapper.findNotifications(userId, offset, size);
        long unread = forumMapper.countUnreadNotifications(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("unread", unread);
        result.put("pageNum", page);
        result.put("pageSize", size);
        return result;
    }

    @Override
    public Map<String, Object> sendMessage(Long toUserId, String content) {
        requireLogin();
        if (toUserId == null) {
            throw new BaseBusinessException("接收用户不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BaseBusinessException("消息内容不能为空");
        }
        NotificationEntity n = new NotificationEntity();
        n.setId(snowflakeIdGenerator.nextId());
        n.setUserid(toUserId);
        n.setType(1); // 1 私信
        n.setSourceuserid(UserContext.getUserId());
        n.setTargetid(0L);
        n.setContent(content.trim());
        n.setIsread(0);
        n.setCreatedat(new Date());
        notificationMapper.insert(n);

        Map<String, Object> result = new HashMap<>();
        result.put("id", n.getId());
        return result;
    }

    @Override
    public Map<String, Object> search(String keyword, Integer pageNum, Integer pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("list", java.util.Collections.emptyList());
            empty.put("total", 0L);
            empty.put("pageNum", pageNum == null ? 1 : pageNum);
            empty.put("pageSize", pageSize == null ? 10 : pageSize);
            return empty;
        }
        return listTopics(null, "latest", keyword.trim(), pageNum, pageSize);
    }
}
