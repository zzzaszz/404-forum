package zzz.service.forum.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzz.service.forum.domain.User;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.domain.pojo.UserPojo;
import zzz.service.forum.mapper.UserMapper;
import zzz.service.forum.utils.PasswordUtils;
import zzz.service.forum.utils.SnowflakeIdGenerator;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户管理控制器（后台，仅管理员）。
 * 由 {@link zzz.service.forum.interceptor.AdminInterceptor} 拦截 /user/** 校验管理员权限。
 *
 * @author caoshi
 * @since 2026-06-30
 */
@Api(tags = "用户管理（管理员）")
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,32}$");

    @Resource
    private UserMapper userMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 分页查询用户
     */
    @ApiOperation("分页查询用户")
    @RequestMapping(value = "/getPageList", method = RequestMethod.POST)
    public R<PageInfo<UserPojo>> getPageList(@RequestBody Map<String, Object> body) {
        try {
            String keyword = str(body.get("keyword"));
            Integer status = intVal(body.get("status"));
            Integer role = intVal(body.get("role"));
            int pageNum = intVal(body.get("pageNum"), 1);
            int pageSize = intVal(body.get("pageSize"), 10);
            int offset = (pageNum - 1) * pageSize;

            long total = userMapper.countPageList(keyword, status, role);
            java.util.List<UserPojo> list = userMapper.findPageList(keyword, status, role, offset, pageSize);

            // 组装 PageInfo 风格结果
            PageInfo<UserPojo> pageInfo = new PageInfo<>(list);
            pageInfo.setTotal(total);
            pageInfo.setPageNum(pageNum);
            pageInfo.setPageSize(pageSize);
            pageInfo.setPages((int) ((total + pageSize - 1) / pageSize));
            return R.ok(pageInfo);
        } catch (Exception e) {
            logger.warn("user getPageList error", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 新增用户
     */
    @ApiOperation("新增用户")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        try {
            String username = str(body.get("username"));
            String password = str(body.get("password"));
            String nickname = str(body.get("nickname"));
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return R.fail("账号和密码不能为空");
            }
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                return R.fail("账号需为 3-32 位字母、数字或下划线");
            }
            if (password.length() < 6 || password.length() > 32) {
                return R.fail("密码长度应为 6-32 位");
            }
            if (userMapper.getByUsername(username) != null) {
                return R.fail("该账号已存在");
            }

            User user = new User();
            user.setId(snowflakeIdGenerator.nextId());
            user.setDeviceId(username);
            user.setUsername(username);
            user.setPasswordHash(PasswordUtils.encode(password));
            user.setNickname(nickname == null || nickname.isEmpty() ? username : nickname);
            user.setAvatar("");
            user.setStatus(1);
            user.setRole(intVal(body.get("role"), 0));
            user.setLastLoginIp("");
            user.setDeleted(0);
            userMapper.insert(user);

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            return R.ok(data);
        } catch (Exception e) {
            logger.warn("user create error", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     */
    @ApiOperation("删除用户")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public R<Integer> delete(@RequestParam("key") String key) {
        try {
            if (key == null || key.isEmpty()) {
                return R.fail("主键不能为空");
            }
            Long id = Long.valueOf(key);
            // 不允许删除自己（管理员自身）
            Long current = zzz.service.forum.context.UserContext.getUserId();
            if (current != null && current.equals(id)) {
                return R.fail("不能删除当前登录账号");
            }
            return R.ok(userMapper.delete(id));
        } catch (Exception e) {
            logger.warn("user delete error", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @ApiOperation("重置密码")
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public R<Map<String, Object>> resetPassword(@RequestBody Map<String, Object> body) {
        try {
            Long id = longVal(body.get("id"));
            String password = str(body.get("password"));
            if (id == null) {
                return R.fail("用户ID不能为空");
            }
            if (password == null || password.length() < 6 || password.length() > 32) {
                return R.fail("密码长度应为 6-32 位");
            }
            userMapper.updatePassword(id, PasswordUtils.encode(password));
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            return R.ok(data);
        } catch (Exception e) {
            logger.warn("user resetPassword error", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 封禁 / 解封（status: 1正常 2封禁）
     */
    @ApiOperation("封禁/解封用户")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    public R<Map<String, Object>> updateStatus(@RequestBody Map<String, Object> body) {
        try {
            Long id = longVal(body.get("id"));
            Integer status = intVal(body.get("status"));
            if (id == null || status == null) {
                return R.fail("参数不完整");
            }
            if (status != 1 && status != 2) {
                return R.fail("状态值非法");
            }
            Long current = zzz.service.forum.context.UserContext.getUserId();
            if (current != null && current.equals(id)) {
                return R.fail("不能封禁当前登录账号");
            }
            userMapper.updateStatus(id, status);
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            return R.ok(data);
        } catch (Exception e) {
            logger.warn("user updateStatus error", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 更新昵称/角色
     */
    @ApiOperation("编辑用户")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public R<Map<String, Object>> update(@RequestBody Map<String, Object> body) {
        try {
            Long id = longVal(body.get("id"));
            if (id == null) {
                return R.fail("用户ID不能为空");
            }
            String nickname = str(body.get("nickname"));
            Integer role = intVal(body.get("role"));
            userMapper.updateProfile(id, nickname, role);
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            return R.ok(data);
        } catch (Exception e) {
            logger.warn("user update error", e);
            return R.fail(e.getMessage());
        }
    }

    // ============ 工具 ============
    private String str(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }

    private Integer intVal(Object v) {
        if (v == null) return null;
        try {
            return Integer.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int intVal(Object v, int def) {
        Integer r = intVal(v);
        return r == null ? def : r;
    }

    private Long longVal(Object v) {
        if (v == null) return null;
        try {
            return Long.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
