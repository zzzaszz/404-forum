package zzz.service.forum.controller;

import org.springframework.web.bind.annotation.*;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.dto.R;
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

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getFollowList(Long userId) {
        String key = FOLLOW_KEY_PREFIX + userId;
        Object cached = redisService.getCacheObject(key);
        if (cached instanceof List) return new ArrayList<>((List<Map<String, String>>) cached);
        return new ArrayList<>();
    }
}
