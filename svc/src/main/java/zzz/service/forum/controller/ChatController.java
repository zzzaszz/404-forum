package zzz.service.forum.controller;

import org.springframework.web.bind.annotation.*;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.User;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.mapper.UserMapper;
import zzz.service.forum.service.RedisService;
import zzz.service.forum.websocket.ChatRoomManager;

import javax.annotation.Resource;
import java.util.*;

/**
 * 聊天室 REST 接口：房间列表、关注、私信。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatRoomManager roomManager;
    @Resource
    private RedisService redisService;
    @Resource
    private UserMapper userMapper;

    private static final String FOLLOW_KEY_PREFIX = "follow:";

    @GetMapping("/rooms")
    public R<Object> rooms() {
        return R.ok(roomManager.getRoomList());
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
        return R.ok(null);
    }

    /** 获取关注列表 */
    @GetMapping("/follow")
    public R<List<Map<String, String>>> followList() {
        Long userId = UserContext.getUserId();
        if (userId == null) return R.ok(Collections.emptyList());
        return R.ok(getFollowList(userId));
    }

    /** 获取关注我的人列表（反向查询所有用户的关注列表） */
    @GetMapping("/followers")
    public R<List<Map<String, String>>> followers() {
        Long userId = UserContext.getUserId();
        if (userId == null) return R.ok(Collections.emptyList());
        String me = String.valueOf(userId);

        // 遍历所有 follow:{uid} 列表，找出包含我的项，其拥有者即关注我的人
        Set<String> followerIds = new LinkedHashSet<>();
        Set<String> keys = redisService.scanKeysWithPrefix(FOLLOW_KEY_PREFIX);
        if (keys != null) {
            for (String key : keys) {
                if (!key.startsWith(FOLLOW_KEY_PREFIX)) continue;
                for (Map<String, String> item : readFollowList(key)) {
                    if (me.equals(item.get("userId"))) {
                        String followerId = key.substring(FOLLOW_KEY_PREFIX.length());
                        if (!followerId.isEmpty()) {
                            followerIds.add(followerId);
                        }
                        break;
                    }
                }
            }
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (String fid : followerIds) {
            Map<String, String> m = new HashMap<>();
            m.put("userId", fid);
            m.put("nickname", nicknameOf(fid));
            result.add(m);
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
