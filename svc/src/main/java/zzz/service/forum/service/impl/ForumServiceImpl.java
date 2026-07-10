package zzz.service.forum.service.impl;

import org.springframework.stereotype.Service;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.FavoriterecordEntity;
import zzz.service.forum.domain.LikerecordEntity;
import zzz.service.forum.domain.NotificationEntity;
import zzz.service.forum.domain.ReportEntity;
import zzz.service.forum.domain.User;
import zzz.service.forum.domain.pojo.BoardPojo;
import zzz.service.forum.domain.pojo.CommentPojo;
import zzz.service.forum.domain.pojo.NotificationPojo;
import zzz.service.forum.domain.pojo.PostPojo;
import zzz.service.forum.mapper.FavoriterecordMapper;
import zzz.service.forum.mapper.ForumMapper;
import zzz.service.forum.mapper.LikerecordMapper;
import zzz.service.forum.mapper.NotificationMapper;
import zzz.service.forum.mapper.ReportMapper;
import zzz.service.forum.mapper.UserMapper;
import zzz.service.forum.service.ForumService;
import zzz.service.forum.service.RedisService;
import zzz.service.forum.utils.BaseBusinessException;
import zzz.service.forum.utils.SnowflakeIdGenerator;
import zzz.service.forum.websocket.NotifyWebSocketHandler;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    // 头像图片池（使用前端 public/images/ 下的图片，与 pickAvatar 保持一致）
    private static final String[] AVATAR_POOL = {
            "/images/user1.png", "/images/user2.png", "/images/user3.png", "/images/user4.png",
            "/images/user5.png", "/images/user6.png", "/images/user7.png", "/images/user8.png"
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
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisService redisService;

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

    /** 获取当前登录用户的昵称 */
    private String getUserNickname() {
        Long userId = UserContext.getUserId();
        if (userId == null) return "匿名用户";
        User user = userMapper.selectById(userId);
        return user != null && user.getNickname() != null ? user.getNickname() : "匿名用户";
    }

    /** 按用户 ID 稳定映射头像（与前端 pickAvatar 逻辑一致） */
    private String pickUserAvatar(Long userId) {
        if (userId == null) return AVATAR_POOL[0];
        int idx = (int) (Math.abs(userId) % AVATAR_POOL.length);
        return AVATAR_POOL[idx];
    }

    /** 获取用户主题颜色，没有则随机 */
    private String getUserColor() {
        Long userId = UserContext.getUserId();
        if (userId == null) return randomFrom(COLOR_POOL);
        User user = userMapper.selectById(userId);
        return user != null && user.getThemeColor() != null ? user.getThemeColor() : randomFrom(COLOR_POOL);
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
        Long userId = UserContext.getUserId();
        post.setUserid(userId);
        // 使用真实用户信息，不再使用匿名随机名称
        post.setAnonymousname(getUserNickname());
        post.setAnonymousavatar(pickUserAvatar(userId));
        post.setAnonymouscolor(getUserColor());
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setContenttext(content.trim());
        // Images 为 MySQL JSON 列，不设置（null）避免空字符串报错
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
    public CommentPojo createComment(String postId, String content, Long parentId, Long rootId, Long replyUserId) {
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
        comment.setReplyuserid(replyUserId == null ? 0L : replyUserId);
        comment.setAnonymousidentityid(null);
        // 使用真实用户信息，不再使用匿名随机名称
        comment.setAnonymousname(getUserNickname());
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
        // 在线用户数 = Redis 中有效的 access token 数量（近 30 分钟内登录）
        Set<String> accessKeys = redisService.scanKeysWithPrefix("login:access:");
        long online = accessKeys == null ? 0 : accessKeys.size();

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
        String me = currentUserId();

        // 拉取与当前用户相关的所有私信（收或发），按时间倒序
        List<NotificationPojo> all = forumMapper.findConversations(me);
        // 按对话对方聚合：首次出现即最新一条；累计该会话未读数
        Map<String, Map<String, Object>> convMap = new LinkedHashMap<>();
        long totalUnread = 0;
        for (NotificationPojo n : all) {
            String other = otherIdOf(n, me);
            if (other == null) continue;
            boolean toMe = n.getUserid() != null && me.equals(String.valueOf(n.getUserid()));
            boolean unread = toMe && n.getIsread() != null && n.getIsread() == 0;
            if (unread) totalUnread++;

            Map<String, Object> conv = convMap.get(other);
            if (conv == null) {
                conv = new HashMap<>();
                conv.put("userId", other);
                conv.put("lastMessage", n.getContent() == null ? "" : n.getContent());
                conv.put("time", n.getCreatedat());
                conv.put("unread", 0);
                convMap.put(other, conv);
            }
            if (unread) {
                conv.put("unread", ((Integer) conv.get("unread")) + 1);
            }
        }

        // 补齐对方昵称
        List<Map<String, Object>> list = new ArrayList<>(convMap.values());
        for (Map<String, Object> conv : list) {
            conv.put("nickname", nicknameOf((String) conv.get("userId")));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("unread", totalUnread);
        return result;
    }

    @Override
    public Map<String, Object> messageHistory(Long toUserId) {
        requireLogin();
        if (toUserId == null) {
            throw new BaseBusinessException("接收用户不能为空");
        }
        String me = currentUserId();
        String other = String.valueOf(toUserId);
        List<NotificationPojo> all = forumMapper.findPrivateMessages(me, other);

        List<Map<String, Object>> list = new ArrayList<>();
        for (NotificationPojo n : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("content", n.getContent() == null ? "" : n.getContent());
            m.put("time", n.getCreatedat());
            boolean mine = n.getSourceuserid() != null && me.equals(String.valueOf(n.getSourceuserid()));
            m.put("mine", mine);
            m.put("fromUserId", n.getSourceuserid() == null ? null : String.valueOf(n.getSourceuserid()));
            list.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("toUserId", other);
        result.put("nickname", nicknameOf(other));
        return result;
    }

    @Override
    public Map<String, Object> markRead(Long toUserId) {
        requireLogin();
        if (toUserId == null) {
            throw new BaseBusinessException("接收用户不能为空");
        }
        String me = currentUserId();
        int rows = forumMapper.markConversationRead(me, String.valueOf(toUserId));
        Map<String, Object> result = new HashMap<>();
        result.put("updated", rows);
        return result;
    }

    @Override
    public Map<String, Object> deleteTopic(String id) {
        requireLogin();
        if (id == null || id.trim().isEmpty()) {
            throw new BaseBusinessException("帖子ID不能为空");
        }
        Long owner = forumMapper.findPostOwner(id);
        if (owner == null) {
            throw new BaseBusinessException("帖子不存在或已删除");
        }
        if (!canModify(owner)) {
            throw new BaseBusinessException("无权删除该帖子");
        }
        forumMapper.softDeletePost(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    public Map<String, Object> deleteComment(String id) {
        requireLogin();
        if (id == null || id.trim().isEmpty()) {
            throw new BaseBusinessException("评论ID不能为空");
        }
        Long owner = forumMapper.findCommentOwner(id);
        if (owner == null) {
            throw new BaseBusinessException("评论不存在或已删除");
        }
        if (!canModify(owner)) {
            throw new BaseBusinessException("无权删除该评论");
        }
        forumMapper.softDeleteComment(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    /** 判断当前用户能否修改/删除目标资源：本人或管理员 */
    private boolean canModify(Long owner) {
        Long me = UserContext.getUserId();
        if (me == null) return false;
        if (me.equals(owner)) return true;
        User u = userMapper.selectById(me);
        return u != null && u.getRole() != null && u.getRole() == 1;
    }

    @Override
    public Map<String, Object> myPosts(Integer pageNum, Integer pageSize) {
        requireLogin();
        String me = currentUserId();
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (page - 1) * size;

        List<PostPojo> list = forumMapper.findTopicsByUser(me, offset, size);
        long total = forumMapper.countTopicsByUser(me);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", page);
        result.put("pageSize", size);
        return result;
    }

    @Override
    public Map<String, Object> myComments(Integer pageNum, Integer pageSize) {
        requireLogin();
        String me = currentUserId();
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (page - 1) * size;

        List<Map<String, Object>> list = forumMapper.findCommentsByUser(me, offset, size);
        long total = forumMapper.countCommentsByUser(me);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", page);
        result.put("pageSize", size);
        return result;
    }

    @Override
    public Map<String, Object> myLikes(Integer pageNum, Integer pageSize) {
        requireLogin();
        String me = currentUserId();
        // 获赞内容通常不多，单页取 100 条
        List<Map<String, Object>> list = forumMapper.findLikedByUser(me, 0, 100);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }

    @Override
    public Map<String, Object> myStats() {
        requireLogin();
        String me = currentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("posts", forumMapper.countTopicsByUser(me));
        result.put("comments", forumMapper.countCommentsByUser(me));
        result.put("likes", forumMapper.sumUserLikes(me));
        return result;
    }

    /** 计算私信对话中的"对方"用户 ID */
    private String otherIdOf(NotificationPojo n, String me) {
        if (n == null) return null;
        boolean toMe = n.getUserid() != null && me.equals(String.valueOf(n.getUserid()));
        if (toMe) {
            // 我是接收者，对方是发送者
            return n.getSourceuserid() == null ? null : String.valueOf(n.getSourceuserid());
        }
        // 我是发送者，对方是接收者
        return n.getUserid() == null ? null : String.valueOf(n.getUserid());
    }

    /** 按用户 ID 查询昵称，失败回退为"用户" */
    private String nicknameOf(String userId) {
        if (userId == null || userId.isEmpty()) return "用户";
        try {
            User u = userMapper.selectById(Long.valueOf(userId));
            return u != null && u.getNickname() != null ? u.getNickname() : "用户";
        } catch (NumberFormatException e) {
            return "用户";
        }
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

        // 通过 WebSocket 实时推送给接收方（不在线则静默，前端轮询兜底）
        Map<String, Object> push = new HashMap<>();
        push.put("type", "pm");
        push.put("id", n.getId());
        push.put("fromUserId", n.getSourceuserid());
        push.put("fromNickname", getUserNickname());
        push.put("content", n.getContent());
        push.put("createdAt", n.getCreatedat());
        NotifyWebSocketHandler.sendToUser(toUserId, push);

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
