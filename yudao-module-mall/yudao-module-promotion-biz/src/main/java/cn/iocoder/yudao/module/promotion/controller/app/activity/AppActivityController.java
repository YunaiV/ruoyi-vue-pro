package cn.iocoder.yudao.module.promotion.controller.app.activity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import cn.iocoder.yudao.module.promotion.service.discount.DiscountActivityService;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

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
    @Resource
    private DiscountActivityService discountActivityService;
    @Resource
    private RewardActivityService rewardActivityService;

    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/list-by-spu-id")
    @Operation(summary = "获得单个商品，近期参与的每个活动")
    @Parameter(name = "spuId", description = "商品编号", required = true)
    public CommonResult<List<AppActivityRespVO>> getActivityListBySpuId(@RequestParam("spuId") Long spuId) {
        // 每种活动，只返回一个
        return success(getAppActivityList(Collections.singletonList(spuId)));
    }

    @GetMapping("/list-by-spu-ids")
    @Operation(summary = "获得多个商品，近期参与的每个活动")
    @Parameter(name = "spuIds", description = "商品编号数组", required = true)
    public CommonResult<Map<Long, List<AppActivityRespVO>>> getActivityListBySpuIds(@RequestParam("spuIds") List<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return success(MapUtil.empty());
        }
        // 每种活动，只返回一个；key 为 SPU 编号
        return success(convertMultiMap(getAppActivityList(spuIds), AppActivityRespVO::getSpuId));
    }

    private List<AppActivityRespVO> getAppActivityList(Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return new ArrayList<>();
        }
        // 获取开启的且开始的且没有结束的活动
        List<AppActivityRespVO> activityList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        // 1. 拼团活动
        getCombinationActivities(spuIds, now, activityList);
        // 2. 秒杀活动
        getSeckillActivities(spuIds, now, activityList);
        // 3. 砍价活动
        getBargainActivities(spuIds, now, activityList);
        // 4. 限时折扣活动
        getDiscountActivities(spuIds, now, activityList);
        // 5. 满减送活动
        getRewardActivityList(spuIds, now, activityList);
        return activityList;
    }

    private void getCombinationActivities(Collection<Long> spuIds, LocalDateTime now, List<AppActivityRespVO> activityList) {
        List<CombinationActivityDO> combinationActivities = combinationActivityService.getCombinationActivityBySpuIdsAndStatusAndDateTimeLt(
                spuIds, CommonStatusEnum.ENABLE.getStatus(), now);
        if (CollUtil.isEmpty(combinationActivities)) {
            return;
        }

        combinationActivities.forEach(item -> {
            activityList.add(new AppActivityRespVO(item.getId(), PromotionTypeEnum.COMBINATION_ACTIVITY.getType(),
                    item.getName(), item.getSpuId(), item.getStartTime(), item.getEndTime()));
        });
    }

    private void getSeckillActivities(Collection<Long> spuIds, LocalDateTime now, List<AppActivityRespVO> activityList) {
        List<SeckillActivityDO> seckillActivities = seckillActivityService.getSeckillActivityBySpuIdsAndStatusAndDateTimeLt(
                spuIds, CommonStatusEnum.ENABLE.getStatus(), now);
        if (CollUtil.isEmpty(seckillActivities)) {
            return;
        }

        seckillActivities.forEach(item -> {
            activityList.add(new AppActivityRespVO(item.getId(), PromotionTypeEnum.SECKILL_ACTIVITY.getType(),
                    item.getName(), item.getSpuId(), item.getStartTime(), item.getEndTime()));
        });
    }

    private void getBargainActivities(Collection<Long> spuIds, LocalDateTime now, List<AppActivityRespVO> activityList) {
        List<BargainActivityDO> bargainActivities = bargainActivityService.getBargainActivityBySpuIdsAndStatusAndDateTimeLt(
                spuIds, CommonStatusEnum.ENABLE.getStatus(), now);
        if (CollUtil.isNotEmpty(bargainActivities)) {
            return;
        }

        bargainActivities.forEach(item -> {
            activityList.add(new AppActivityRespVO(item.getId(), PromotionTypeEnum.BARGAIN_ACTIVITY.getType(),
                    item.getName(), item.getSpuId(), item.getStartTime(), item.getEndTime()));
        });
    }

    private void getDiscountActivities(Collection<Long> spuIds, LocalDateTime now, List<AppActivityRespVO> activityList) {
        List<DiscountActivityDO> discountActivities = discountActivityService.getDiscountActivityBySpuIdsAndStatusAndDateTimeLt(
                spuIds, CommonStatusEnum.ENABLE.getStatus(), now);
        if (CollUtil.isEmpty(discountActivities)) {
            return;
        }

        List<DiscountProductDO> products = discountActivityService.getDiscountProductsByActivityId(
                convertSet(discountActivities, DiscountActivityDO::getId));
        Map<Long, Long> productMap = convertMap(products, DiscountProductDO::getActivityId, DiscountProductDO::getSpuId);
        discountActivities.forEach(item -> activityList.add(new AppActivityRespVO(item.getId(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(),
                item.getName(), productMap.get(item.getId()), item.getStartTime(), item.getEndTime())));
    }

    private void getRewardActivityList(Collection<Long> spuIds, LocalDateTime now, List<AppActivityRespVO> activityList) {
        List<RewardActivityDO> rewardActivities = rewardActivityService.getRewardActivityBySpuIdsAndStatusAndDateTimeLt(
                spuIds, CommonStatusEnum.ENABLE.getStatus(), now);
        if (CollUtil.isEmpty(rewardActivities)) {
            return;
        }

        Map<Long, Optional<RewardActivityDO>> spuIdAndActivityMap = spuIds.stream().collect(Collectors.toMap(spuId -> spuId, spuId -> rewardActivities.stream()
                .filter(activity -> PromotionProductScopeEnum.isAll(activity.getProductScope())
                                || PromotionProductScopeEnum.isSpu(activity.getProductScope()) // 商品范围
                                    && CollUtil.contains(activity.getProductScopeValues(), spuId)
                                || PromotionProductScopeEnum.isCategory(activity.getProductScope()) // 分类范围
                                    && CollUtil.contains(activity.getProductScopeValues(), productSpuApi.getSpu(spuId).getCategoryId()))
                .max(Comparator.comparing(RewardActivityDO::getCreateTime))));
        for (Long supId : spuIdAndActivityMap.keySet()) {
            spuIdAndActivityMap.get(supId).ifPresent(rewardActivity -> activityList.add(
                    new AppActivityRespVO(rewardActivity.getId(), PromotionTypeEnum.REWARD_ACTIVITY.getType(),
                            rewardActivity.getName(), supId, rewardActivity.getStartTime(), rewardActivity.getEndTime())));
        }
    }

}
