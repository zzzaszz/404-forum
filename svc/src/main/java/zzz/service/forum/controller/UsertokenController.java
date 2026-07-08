package zzz.service.forum.controller;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import zzz.service.forum.domain.dto.QueryParam;
import zzz.service.forum.domain.dto.R;
import zzz.service.forum.domain.pojo.UsertokenPojo;
import zzz.service.forum.service.RedisService;
import zzz.service.forum.service.UsertokenService;
import zzz.service.forum.utils.LoginUser;
import zzz.service.forum.utils.ServletUtils;
import zzz.service.forum.utils.TokenService;

import javax.annotation.Resource;

/**
 * 用户Token表（JWT会话管理）(UserToken)表控制层
 *
 * @author caoshi
 * @since 2026-05-13 17:23:06
 */
@RestController
@RequestMapping("usertoken")
public class UsertokenController {
    /**
     * 服务对象
     */
    @Resource
    private UsertokenService usertokenService;

    /**
     * 引用Redis服务
     */
    @Resource
    private RedisService redisService;
    /**
     * 引用Token服务
     */
    @Resource
    private TokenService tokenService;

    /**
     * slf4j+logback
     */
    private final static Logger logger = LoggerFactory.getLogger(UsertokenController.class);

    /**
     * 通过主键查询单条数据
     *
     * @param key 主键
     * @return 单条数据
     */
    @ApiOperation(value = " 获取用户Token表（JWT会话管理）详细信息", notes = "获取用户Token表（JWT会话管理）详细信息", produces = "application/json")
    @RequestMapping(value = "/getEntity", method = RequestMethod.GET)

    public R<UsertokenPojo> getEntity(String key) {
        try {
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            return R.ok(this.usertokenService.getEntity(key));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }


    /**
     * 分页查询
     *
     * @param json 筛选条件
     * @return 查询结果
     */
    @ApiOperation(value = "按条件分页查询", notes = "按条件分页查询", produces = "application/json")
    @RequestMapping(value = "/getPageList", method = RequestMethod.POST)

    public R<PageInfo<UsertokenPojo>> getPageList(@RequestBody String json) {
        try {
            QueryParam queryParam = JSONArray.parseObject(json, QueryParam.class);
            if (queryParam.getOrderBy() == null || "".equals(queryParam.getOrderBy()))
                queryParam.setOrderBy("UserToken.CreatedAt");
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            return R.ok(this.usertokenService.getPageList(queryParam));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }


    /**
     * 新增数据
     *
     * @param json 实体
     * @return 新增结果
     */
    @ApiOperation(value = " 新增用户Token表（JWT会话管理）", notes = "新增用户Token表（JWT会话管理）", produces = "application/json")
    @RequestMapping(value = "/create", method = RequestMethod.POST)

    public R<UsertokenPojo> create(@RequestBody String json) {
        try {
            UsertokenPojo usertokenPojo = JSONArray.parseObject(json, UsertokenPojo.class);
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());


            return R.ok(this.usertokenService.insert(usertokenPojo));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 编辑数据
     *
     * @param json 实体
     * @return 编辑结果
     */
    @ApiOperation(value = "修改用户Token表（JWT会话管理）", notes = "修改用户Token表（JWT会话管理）", produces = "application/json")
    @RequestMapping(value = "/update", method = RequestMethod.POST)

    public R<UsertokenPojo> update(@RequestBody String json) {
        try {
            UsertokenPojo usertokenPojo = JSONArray.parseObject(json, UsertokenPojo.class);
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());


//            usertokenPojo.setAssessor(""); // 审核员
//            usertokenPojo.setAssessorid(""); // 审核员id
//            usertokenPojo.setAssessdate(new Date()); //审核时间
            return R.ok(this.usertokenService.update(usertokenPojo));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 删除数据
     *
     * @param key 主键
     * @return 删除是否成功
     */
    @ApiOperation(value = "删除用户Token表（JWT会话管理）", notes = "删除用户Token表（JWT会话管理）", produces = "application/json")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)

    public R<Integer> delete(String key) {
        try {
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            return R.ok(this.usertokenService.delete(key));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}

