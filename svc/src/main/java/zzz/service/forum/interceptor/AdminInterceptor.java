package zzz.service.forum.interceptor;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import zzz.service.forum.context.UserContext;
import zzz.service.forum.domain.User;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.mapper.UserMapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 管理员权限拦截器：校验当前登录用户是否为管理员（Role=1）。
 * <p>
 * 用于保护用户管理、板块管理等后台写接口。非管理员或未登录返回 403。
 *
 * @author caoshi
 * @since 2026-06-30
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = UserContext.getUserId();
        // 未登录
        if (userId == null) {
            writeForbidden(response, "请先登录");
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            writeForbidden(response, "用户不存在");
            return false;
        }
        // 账号封禁
        if (user.getStatus() != null && user.getStatus() == 2) {
            writeForbidden(response, "账号已被封禁");
            return false;
        }
        // 非管理员
        if (user.getRole() == null || user.getRole() != 1) {
            writeForbidden(response, "无权限，仅管理员可访问");
            return false;
        }
        return true;
    }

    private void writeForbidden(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> result = R.fail(403, msg);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSONObject.toJSONString(result));
        }
    }
}
