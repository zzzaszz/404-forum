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
import zzz.service.forum.domain.pojo.AnonymousidentityPojo;
import zzz.service.forum.service.AnonymousidentityService;
import zzz.service.forum.service.RedisService;
import zzz.service.forum.utils.LoginUser;
import zzz.service.forum.utils.ServletUtils;
import zzz.service.forum.utils.TokenService;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 匿名身份映射表（同帖匿名一致，不同帖匿名不同）(AnonymousIdentity)表控制层
 *
 * @author caoshi
 * @since 2026-05-13 15:13:28
 */
@RestController
@RequestMapping("anonymousidentity")
public class AnonymousidentityController {
    /**
     * 服务对象
     */
    @Resource
    private AnonymousidentityService anonymousidentityService;

    /**
     * 引用Redis服务
     */
    @Resource
    private RedisService redisService;
    @Resource
    private TokenService tokenService;

    /**
     * slf4j+logback
     */
    private final static Logger logger = LoggerFactory.getLogger(AnonymousidentityController.class);

    /**
     * 通过主键查询单条数据
     *
     * @param key 主键
     * @return 单条数据
     */
    @ApiOperation(value = " 获取匿名身份映射表（同帖匿名一致，不同帖匿名不同）详细信息", notes = "获取匿名身份映射表（同帖匿名一致，不同帖匿名不同）详细信息", produces = "application/json")
    @RequestMapping(value = "/getEntity", method = RequestMethod.GET)
    public R<AnonymousidentityPojo> getEntity(String key) {
        try {
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            return R.ok(this.anonymousidentityService.getEntity(key, loginUser.getTenantid()));
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
    public R<PageInfo<AnonymousidentityPojo>> getPageList(@RequestBody String json) {
        try {
            QueryParam queryParam = JSONArray.parseObject(json, QueryParam.class);
            if (queryParam.getOrderBy() == null || "".equals(queryParam.getOrderBy()))
                queryParam.setOrderBy("AnonymousIdentity.CreatedAt");
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            queryParam.setTenantid(loginUser.getTenantid());
            return R.ok(this.anonymousidentityService.getPageList(queryParam));
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
    @ApiOperation(value = " 新增匿名身份映射表（同帖匿名一致，不同帖匿名不同）", notes = "新增匿名身份映射表（同帖匿名一致，不同帖匿名不同）", produces = "application/json")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public R<AnonymousidentityPojo> create(@RequestBody String json) {
        try {
            AnonymousidentityPojo anonymousidentityPojo = JSONArray.parseObject(json, AnonymousidentityPojo.class);
            anonymousidentityPojo.setCreatedat(new Date());
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            return R.ok(this.anonymousidentityService.insert(anonymousidentityPojo));
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
    @ApiOperation(value = "修改匿名身份映射表（同帖匿名一致，不同帖匿名不同）", notes = "修改匿名身份映射表（同帖匿名一致，不同帖匿名不同）", produces = "application/json")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public R<AnonymousidentityPojo> update(@RequestBody String json) {
        try {
            AnonymousidentityPojo anonymousidentityPojo = JSONArray.parseObject(json, AnonymousidentityPojo.class);
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//            anonymousidentityPojo.setAssessor(""); // 审核员
//            anonymousidentityPojo.setAssessorid(""); // 审核员id
//            anonymousidentityPojo.setAssessdate(new Date()); //审核时间
            return R.ok(this.anonymousidentityService.update(anonymousidentityPojo));
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
    @ApiOperation(value = "删除匿名身份映射表（同帖匿名一致，不同帖匿名不同）", notes = "删除匿名身份映射表（同帖匿名一致，不同帖匿名不同）", produces = "application/json")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public R<Integer> delete(String key) {
        try {
            // 获得用户数据
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            return R.ok(this.anonymousidentityService.delete(key, loginUser.getTenantid()));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

}

