package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.promotion.api.reward.RewardActivityApi;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper.formatPrice;

/**
 * 满减送活动的 {@link TradePriceCalculator} 实现类
 *
 * @author 芋道源码
 */
@Component
@Order(TradePriceCalculator.ORDER_REWARD_ACTIVITY)
public class TradeRewardActivityPriceCalculator implements TradePriceCalculator {

    @Resource
    private RewardActivityApi rewardActivityApi;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 0. 只有【普通】订单，才计算该优惠
        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
            return;
        }
        // 获得 SKU 对应的满减送活动
        List<RewardActivityMatchRespDTO> rewardActivities = rewardActivityApi.getMatchRewardActivityList(
                convertSet(result.getItems(), TradePriceCalculateRespBO.OrderItem::getSpuId));
        if (CollUtil.isEmpty(rewardActivities)) {
            return;
        }

        // 处理每个满减送活动
        rewardActivities.forEach(rewardActivity -> calculate(param, result, rewardActivity));
    }

    private void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result,
                           RewardActivityMatchRespDTO rewardActivity) {
        // 1.1 获得满减送的订单项（商品）列表
        List<TradePriceCalculateRespBO.OrderItem> orderItems = filterMatchCouponOrderItems(result, rewardActivity);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        // 1.2 获得最大匹配的满减送活动的规则
        RewardActivityMatchRespDTO.Rule rule = getMaxMatchRewardActivityRule(rewardActivity, orderItems);
        if (rule == null) {
            TradePriceCalculatorHelper.addNotMatchPromotion(result, orderItems,
                    rewardActivity.getId(), rewardActivity.getName(), PromotionTypeEnum.REWARD_ACTIVITY.getType(),
                    getRewardActivityNotMeetTip(rewardActivity));
            return;
        }

        // 2.1 计算可以优惠的金额
        Integer newDiscountPrice = rule.getDiscountPrice();
        // 2.2 计算分摊的优惠金额
        List<Integer> divideDiscountPrices = TradePriceCalculatorHelper.dividePrice(orderItems, newDiscountPrice);

        // 3.1 记录使用的优惠劵
        result.setCouponId(param.getCouponId());
        // 3.2 记录优惠明细
        TradePriceCalculatorHelper.addPromotion(result, orderItems,
                rewardActivity.getId(), rewardActivity.getName(), PromotionTypeEnum.REWARD_ACTIVITY.getType(),
                StrUtil.format("满减送：省 {} 元", formatPrice(rule.getDiscountPrice())),
                divideDiscountPrices);
        // 3.3 更新 SKU 优惠金额
        for (int i = 0; i < orderItems.size(); i++) {
            TradePriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            orderItem.setDiscountPrice(orderItem.getDiscountPrice() + divideDiscountPrices.get(i));
            TradePriceCalculatorHelper.recountPayPrice(orderItem);
        }
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 获得满减送的订单项（商品）列表
     *
     * @param result 计算结果
     * @param rewardActivity 满减送活动
     * @return 订单项（商品）列表
     */
    private List<TradePriceCalculateRespBO.OrderItem> filterMatchCouponOrderItems(TradePriceCalculateRespBO result,
                                                                                  RewardActivityMatchRespDTO rewardActivity) {
        return filterList(result.getItems(),
                orderItem -> CollUtil.contains(rewardActivity.getSpuIds(), orderItem.getSpuId()));
    }

    /**
     * 获得最大匹配的满减送活动的规则
     *
     * @param rewardActivity 满减送活动
     * @param orderItems 商品项
     * @return 匹配的活动规则
     */
    private RewardActivityMatchRespDTO.Rule getMaxMatchRewardActivityRule(RewardActivityMatchRespDTO rewardActivity,
                                                                          List<TradePriceCalculateRespBO.OrderItem> orderItems) {
        // 1. 计算数量和价格
        Integer count = TradePriceCalculatorHelper.calculateTotalCount(orderItems);
        Integer price = TradePriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        assert count != null && price != null;

        // 2. 倒序找一个最大优惠的规则
        for (int i = rewardActivity.getRules().size() - 1; i >= 0; i--) {
            RewardActivityMatchRespDTO.Rule rule = rewardActivity.getRules().get(i);
            if (PromotionConditionTypeEnum.PRICE.getType().equals(rewardActivity.getConditionType())
                    && price >= rule.getLimit()) {
                return rule;
            }
            if (PromotionConditionTypeEnum.COUNT.getType().equals(rewardActivity.getConditionType())
                    && count >= rule.getLimit()) {
                return rule;
            }
        }
        return null;
    }

    /**
     * 获得满减送活动部匹配时的提示
     *
     * @param rewardActivity 满减送活动
     * @return 提示
     */
    private String getRewardActivityNotMeetTip(RewardActivityMatchRespDTO rewardActivity) {
        // TODO 芋艿：后面再想想；应该找第一个规则，算下还差多少即可。
        return "TODO";
    }

}
