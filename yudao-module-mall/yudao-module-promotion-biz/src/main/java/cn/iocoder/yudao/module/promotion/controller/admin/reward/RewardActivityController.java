package cn.iocoder.yudao.module.promotion.controller.admin.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.reward.RewardActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 满减送活动")
@RestController
@RequestMapping("/promotion/reward-activity")
@Validated
public class RewardActivityController {

    @Resource
    private RewardActivityService rewardActivityService;

    @PostMapping("/create")
    @ApiOperation("创建满减送活动")
    @PreAuthorize("@ss.hasPermission('promotion:reward-activity:create')")
    public CommonResult<Long> createRewardActivity(@Valid @RequestBody RewardActivityCreateReqVO createReqVO) {
        return success(rewardActivityService.createRewardActivity(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新满减送活动")
    @PreAuthorize("@ss.hasPermission('promotion:reward-activity:update')")
    public CommonResult<Boolean> updateRewardActivity(@Valid @RequestBody RewardActivityUpdateReqVO updateReqVO) {
        rewardActivityService.updateRewardActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @ApiOperation("关闭满减送活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:reward-activity:close')")
    public CommonResult<Boolean> closeRewardActivity(@RequestParam("id") Long id) {
        rewardActivityService.closeRewardActivity(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除满减送活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:reward-activity:delete')")
    public CommonResult<Boolean> deleteRewardActivity(@RequestParam("id") Long id) {
        rewardActivityService.deleteRewardActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得满减送活动")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('promotion:reward-activity:query')")
    public CommonResult<RewardActivityRespVO> getRewardActivity(@RequestParam("id") Long id) {
        RewardActivityDO rewardActivity = rewardActivityService.getRewardActivity(id);
        return success(RewardActivityConvert.INSTANCE.convert(rewardActivity));
    }

    @GetMapping("/page")
    @ApiOperation("获得满减送活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:reward-activity:query')")
    public CommonResult<PageResult<RewardActivityRespVO>> getRewardActivityPage(@Valid RewardActivityPageReqVO pageVO) {
        PageResult<RewardActivityDO> pageResult = rewardActivityService.getRewardActivityPage(pageVO);
        return success(RewardActivityConvert.INSTANCE.convertPage(pageResult));
    }

}
