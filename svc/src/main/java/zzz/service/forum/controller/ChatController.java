package zzz.service.forum.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.websocket.ChatRoomManager;

import javax.annotation.Resource;

/**
 * 聊天室 REST 接口：房间列表等。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatRoomManager roomManager;

    @GetMapping("/rooms")
    public R<Object> rooms() {
        return R.ok(roomManager.getRoomList());
    }
}
