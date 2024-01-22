package cn.iocoder.yudao.module.promotion.controller.app.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.promotion.controller.app.reward.vo.AppRewardActivityRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 满减送活动")
@RestController
@RequestMapping("/promotion/reward-activity")
@Validated
public class AppRewardActivityController {

    @Resource
    private RewardActivityService rewardActivityService;

    @GetMapping("/get")
    @Operation(summary = "获得满减送活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppRewardActivityRespVO> getRewardActivity(@RequestParam("id") Long id) {
        RewardActivityDO rewardActivity = rewardActivityService.getRewardActivity(id);
        return success(BeanUtils.toBean(rewardActivity, AppRewardActivityRespVO.class));
    }

}
