package cn.iocoder.yudao.module.promotion.controller.app.activity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 营销活动") // 用于提供跨多个活动的 HTTP 接口
@RestController
@RequestMapping("/promotion/activity")
@Validated
public class AppActivityController {

    @Resource
    private CombinationActivityService combinationActivityService;
    @Resource
    private SeckillActivityService seckillActivityService;
    @Resource
    private BargainActivityService bargainActivityService;

    @GetMapping("/list-by-spu-id")
    @Operation(summary = "获得单个商品，近期参与的每个活动") // 每种活动，只返回一个
    @Parameter(name = "spuId", description = "商品编号", required = true)
    public CommonResult<List<AppActivityRespVO>> getActivityListBySpuId(@RequestParam("spuId") Long spuId) {
        return success(getAppActivityRespVOList(spuId));
    }

    @GetMapping("/list-by-spu-ids")
    @Operation(summary = "获得多个商品，近期参与的每个活动") // 每种活动，只返回一个；key 为 SPU 编号
    @Parameter(name = "spuIds", description = "商品编号数组", required = true)
    public CommonResult<Map<Long, List<AppActivityRespVO>>> getActivityListBySpuIds(@RequestParam("spuIds") List<Long> spuIds) {

        if (CollUtil.isEmpty(spuIds)) {
            return success(MapUtil.empty());
        }

        Map<Long, List<AppActivityRespVO>> map = new HashMap<>(spuIds.size());
        spuIds.forEach(spuId -> {
            map.put(spuId, getAppActivityRespVOList(spuId));
        });
        return success(map);
    }

    private List<AppActivityRespVO> getAppActivityRespVOList(Long spuId) {
        List<AppActivityRespVO> respList = new ArrayList<>();
        CombinationActivityDO combination = combinationActivityService.getCombinationActivityBySpuId(spuId);
        if (combination != null) {
            respList.add(new AppActivityRespVO()
                    .setId(combination.getId())
                    .setType(PromotionTypeEnum.COMBINATION_ACTIVITY.getType())
                    .setName(combination.getName())
                    .setStartTime(combination.getStartTime())
                    .setEndTime(combination.getEndTime()));
        }
        SeckillActivityDO seckill = seckillActivityService.getSeckillActivityBySpuId(spuId);
        if (seckill != null) {
            respList.add(new AppActivityRespVO()
                    .setId(seckill.getId())
                    .setType(PromotionTypeEnum.SECKILL_ACTIVITY.getType())
                    .setName(seckill.getName())
                    .setStartTime(seckill.getStartTime())
                    .setEndTime(seckill.getEndTime()));
        }
        BargainActivityDO bargain = bargainActivityService.getBargainActivityBySpuId(spuId);
        if (bargain != null) {
            respList.add(new AppActivityRespVO()
                    .setId(bargain.getId())
                    .setType(PromotionTypeEnum.BARGAIN_ACTIVITY.getType())
                    .setName(bargain.getName())
                    .setStartTime(bargain.getStartTime())
                    .setEndTime(bargain.getEndTime()));
        }
        return respList;
    }

}
