package zzz.service.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.RoomMessageEntity;
import zzz.service.forum.domain.User;
import zzz.service.forum.domain.UserFollowEntity;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.mapper.RoomMessageMapper;
import zzz.service.forum.mapper.UserFollowMapper;
import zzz.service.forum.mapper.UserMapper;
import zzz.service.forum.service.RedisService;
import zzz.service.forum.utils.SnowflakeIdGenerator;
import zzz.service.forum.websocket.ChatRoomManager;

import javax.annotation.Resource;
import java.util.*;

/**
 * 聊天室 REST 接口：房间列表、关注、私信。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatRoomManager roomManager;
    @Resource
    private RedisService redisService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Resource
    private RoomMessageMapper roomMessageMapper;

    private static final String FOLLOW_KEY_PREFIX = "follow:";

    @GetMapping("/rooms")
    public R<Object> rooms() {
        return R.ok(roomManager.getRoomList());
    }

    /** 聊天室历史消息 */
    @GetMapping("/history")
    public R<List<RoomMessageEntity>> history(@RequestParam Integer roomId,
                                              @RequestParam(defaultValue = "50") Integer limit) {
        if (limit == null || limit > 200) limit = 50;
        return R.ok(roomMessageMapper.selectRecent(roomId, limit));
    }

    /** 关注用户 */
    @PostMapping("/follow")
    public R<Void> follow(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) return R.fail(401, "请先登录");
        String targetUserId = String.valueOf(body.get("userId"));
        String nickname = body.get("nickname") != null ? String.valueOf(body.get("nickname")) : "";

        String key = FOLLOW_KEY_PREFIX + userId;
        List<Map<String, String>> list = getFollowList(userId);
        // 去重
        list.removeIf(f -> targetUserId.equals(f.get("userId")));
        Map<String, String> item = new HashMap<>();
        item.put("userId", targetUserId);
        item.put("nickname", nickname);
        list.add(item);
        redisService.setCacheObject(key, list);

        // MySQL 持久化（幂等：先删旧记录再插入，避免唯一键冲突）
        try {
            Long targetId = Long.valueOf(targetUserId);
            userFollowMapper.deleteByUserAndFollow(userId, targetId);
            UserFollowEntity entity = new UserFollowEntity();
            entity.setId(snowflakeIdGenerator.nextId());
            entity.setUserid(userId);
            entity.setFollowuserid(targetId);
            entity.setNickname(nickname);
            entity.setCreatedat(new Date());
            userFollowMapper.insert(entity);
        } catch (Exception e) {
            // 持久化失败不影响本次关注结果，仅记录日志
            log.warn("关注持久化失败: userId={}, targetUserId={}, error={}", userId, targetUserId, e.getMessage());
        }
        return R.ok(null);
    }

    /** 取消关注 */
    @DeleteMapping("/follow/{targetUserId}")
    public R<Void> unfollow(@PathVariable String targetUserId) {
        Long userId = UserContext.getUserId();
        if (userId == null) return R.fail(401, "请先登录");
        String key = FOLLOW_KEY_PREFIX + userId;
        List<Map<String, String>> list = getFollowList(userId);
        list.removeIf(f -> targetUserId.equals(f.get("userId")));
        redisService.setCacheObject(key, list);

        // 同步删除 MySQL 记录
        try {
            userFollowMapper.deleteByUserAndFollow(userId, Long.valueOf(targetUserId));
        } catch (Exception e) {
            log.warn("取消关注持久化失败: userId={}, targetUserId={}, error={}", userId, targetUserId, e.getMessage());
        }
        return R.ok(null);
    }

    /** 获取关注列表 */
    @GetMapping("/follow")
    public R<List<Map<String, String>>> followList() {
        Long userId = UserContext.getUserId();
        if (userId == null) return R.ok(Collections.emptyList());

        // 优先读 Redis 缓存
        List<Map<String, String>> list = getFollowList(userId);
        if (!list.isEmpty()) {
            return R.ok(list);
        }
        // 缓存为空（Redis 重启 / 首次访问），从 MySQL 加载并回填缓存
        List<UserFollowEntity> records = userFollowMapper.listByUserId(userId);
        if (records != null) {
            list = new ArrayList<>();
            for (UserFollowEntity r : records) {
                Map<String, String> m = new HashMap<>();
                m.put("userId", String.valueOf(r.getFollowuserid()));
                m.put("nickname", r.getNickname());
                list.add(m);
            }
        }
        if (!list.isEmpty()) {
            redisService.setCacheObject(FOLLOW_KEY_PREFIX + userId, list);
        }
        return R.ok(list);
    }

    /** 获取关注我的人列表（SQL 反查 UserFollow，避免全量扫描 Redis） */
    @GetMapping("/followers")
    public R<List<Map<String, String>>> followers() {
        Long userId = UserContext.getUserId();
        if (userId == null) return R.ok(Collections.emptyList());

        List<UserFollowEntity> records = userFollowMapper.listFollowers(userId);
        List<Map<String, String>> result = new ArrayList<>();
        if (records != null) {
            for (UserFollowEntity r : records) {
                Map<String, String> m = new HashMap<>();
                m.put("userId", String.valueOf(r.getUserid()));
                // UserFollow.nickname 冗余的是"被关注者昵称"，粉丝列表需显示关注者昵称，故实时查询
                m.put("nickname", nicknameOf(String.valueOf(r.getUserid())));
                result.add(m);
            }
        }
        return R.ok(result);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getFollowList(Long userId) {
        String key = FOLLOW_KEY_PREFIX + userId;
        Object cached = redisService.getCacheObject(key);
        if (cached instanceof List) return new ArrayList<>((List<Map<String, String>>) cached);
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> readFollowList(String key) {
        Object cached = redisService.getCacheObject(key);
        if (cached instanceof List) return new ArrayList<>((List<Map<String, String>>) cached);
        return new ArrayList<>();
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
}
