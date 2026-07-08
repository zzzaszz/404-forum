package zzz.service.forum.controller;

import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.User;
import zzz.service.forum.domain.dto.Result;
import zzz.service.forum.mapper.UserMapper;

import javax.annotation.Resource;

public abstract class BaseController {

    private static final int UNAUTHORIZED = 401;

    @Resource
    private UserMapper userMapper;

    /**
     * 获取当前登录用户信息。
     * 业务接口应在入口处调用此方法。
     * 当 token 过期或用户不存在时，返回 code=401, message="userinfo is null"。
     * 前端收到 code 401 后会触发 token 刷新逻辑。
     */
    protected Result<User> getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(UNAUTHORIZED, "userinfo is null");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(UNAUTHORIZED, "userinfo is null");
        }

        return Result.success(user);
    }
}
