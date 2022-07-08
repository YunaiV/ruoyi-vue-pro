package cn.iocoder.yudao.module.market.controller.admin.activity;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;
import javax.validation.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.module.market.controller.admin.activity.vo.*;
import cn.iocoder.yudao.module.market.dal.dataobject.activity.ActivityDO;
import cn.iocoder.yudao.module.market.convert.activity.ActivityConvert;
import cn.iocoder.yudao.module.market.service.activity.ActivityService;

@Api(tags = "管理后台 - 促销活动")
@RestController
@RequestMapping("/market/activity")
@Validated
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @PostMapping("/create")
    @ApiOperation("创建促销活动")
    @PreAuthorize("@ss.hasPermission('market:activity:create')")
    public CommonResult<Long> createActivity(@Valid @RequestBody ActivityCreateReqVO createReqVO) {
        return success(activityService.createActivity(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新促销活动")
    @PreAuthorize("@ss.hasPermission('market:activity:update')")
    public CommonResult<Boolean> updateActivity(@Valid @RequestBody ActivityUpdateReqVO updateReqVO) {
        activityService.updateActivity(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除促销活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('market:activity:delete')")
    public CommonResult<Boolean> deleteActivity(@RequestParam("id") Long id) {
        activityService.deleteActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得促销活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('market:activity:query')")
    public CommonResult<ActivityRespVO> getActivity(@RequestParam("id") Long id) {
        ActivityDO activity = activityService.getActivity(id);
        return success(ActivityConvert.INSTANCE.convert(activity));
    }

    @GetMapping("/list")
    @ApiOperation("获得促销活动列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('market:activity:query')")
    public CommonResult<List<ActivityRespVO>> getActivityList(@RequestParam("ids") Collection<Long> ids) {
        List<ActivityDO> list = activityService.getActivityList(ids);
        return success(ActivityConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得促销活动分页")
    @PreAuthorize("@ss.hasPermission('market:activity:query')")
    public CommonResult<PageResult<ActivityRespVO>> getActivityPage(@Valid ActivityPageReqVO pageVO) {
        PageResult<ActivityDO> pageResult = activityService.getActivityPage(pageVO);
        return success(ActivityConvert.INSTANCE.convertPage(pageResult));
    }

}
