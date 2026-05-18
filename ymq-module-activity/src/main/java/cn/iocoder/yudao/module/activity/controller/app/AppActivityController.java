package cn.iocoder.yudao.module.activity.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.activity.controller.app.vo.AppActivityCreateReqVO;
import cn.iocoder.yudao.module.activity.controller.app.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.activity.dal.dataobject.ActivityDO;
import cn.iocoder.yudao.module.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Sprint 1 第 1 周接龙基础接口。
 *
 * 当前阶段 `userId` 通过 @RequestParam 传，D5 微信登录链路接通后改成从 SecurityContext 注入。
 */
@Tag(name = "用户 App - 活动")
@RestController
@RequestMapping("/activity")
@PermitAll
public class AppActivityController {

    @Resource
    private ActivityService activityService;

    @PostMapping("/create")
    @Operation(summary = "创建活动")
    public CommonResult<Long> create(@RequestBody @Valid AppActivityCreateReqVO reqVO) {
        return success(activityService.createActivity(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "活动详情（含报名列表）")
    @Parameter(name = "id", description = "活动 id", required = true)
    public CommonResult<AppActivityRespVO> get(@RequestParam("id") Long id) {
        return success(activityService.getActivityDetail(id));
    }

    @PostMapping("/signup")
    @Operation(summary = "接龙报名")
    public CommonResult<Integer> signup(@RequestParam("activityId") Long activityId,
                                        @RequestParam("userId") Long userId) {
        return success(activityService.signup(activityId, userId));
    }

    @PostMapping("/cancel-signup")
    @Operation(summary = "取消报名")
    public CommonResult<Boolean> cancelSignup(@RequestParam("activityId") Long activityId,
                                              @RequestParam("userId") Long userId) {
        activityService.cancelSignup(activityId, userId);
        return success(true);
    }

    @GetMapping("/list-mine")
    @Operation(summary = "我创建的活动")
    public CommonResult<PageResult<AppActivityRespVO>> listMine(@RequestParam("userId") Long userId,
                                                                 PageParam pageParam) {
        PageResult<ActivityDO> page = activityService.pageMyCreated(userId, pageParam);
        return success(BeanUtils.toBean(page, AppActivityRespVO.class));
    }

    @GetMapping("/list-joined")
    @Operation(summary = "我参与的活动")
    public CommonResult<PageResult<AppActivityRespVO>> listJoined(@RequestParam("userId") Long userId,
                                                                   PageParam pageParam) {
        return success(activityService.pageMyJoined(userId, pageParam));
    }

}
