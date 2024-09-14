package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.promotion.api.reward.RewardActivityApi;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.REWARD_ACTIVITY_TYPE_NOT_EXISTS;
import static cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper.formatPrice;

// TODO @puhui999：相关的单测，建议改一改

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
        List<RewardActivityMatchRespDTO> rewardActivities = rewardActivityApi.getRewardActivityBySpuIdsAndStatusAndDateTimeLt(
                convertSet(result.getItems(), TradePriceCalculateRespBO.OrderItem::getSpuId), CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(rewardActivities)) {
            return;
        }
        // 处理最新的满减送活动
        if(!rewardActivities.isEmpty()){
            calculate(param, result, rewardActivities.get(0));
        }
    }

    private void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result,
                           RewardActivityMatchRespDTO rewardActivity) {
        // 1.1 获得满减送的订单项（商品）列表
        List<TradePriceCalculateRespBO.OrderItem> orderItems = filterMatchActivityOrderItems(result, rewardActivity);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        // 1.2 获得最大匹配的满减送活动的规则
        RewardActivityMatchRespDTO.Rule rule = getMaxMatchRewardActivityRule(rewardActivity, orderItems);
        if (rule == null) {
            TradePriceCalculatorHelper.addNotMatchPromotion(result, orderItems,
                    rewardActivity.getId(), rewardActivity.getName(), PromotionTypeEnum.REWARD_ACTIVITY.getType(),
                    getRewardActivityNotMeetTip(rewardActivity, orderItems));
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

        // 4.1 记录赠送的积分
        if (rule.getPoint() != null && rule.getPoint() > 0) {
            List<Integer> dividePoints = TradePriceCalculatorHelper.dividePrice(orderItems, rule.getPoint());
            for (int i = 0; i < orderItems.size(); i++) {
                // 商品可能赠送了积分，所以这里要加上
                TradePriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
                orderItem.setGivePoint(orderItem.getGivePoint() + dividePoints.get(i));
            }
        }
        // 4.2 记录订单是否包邮
        if (Boolean.TRUE.equals(rule.getFreeDelivery())) {
            // 只要满足一个活动包邮那么这单就包邮
            result.setFreeDelivery(true);
        }
        // 4.3 记录赠送的优惠券
        if (CollUtil.isNotEmpty(rule.getGiveCouponTemplateCounts())) {
            for (Map.Entry<Long, Integer> entry : rule.getGiveCouponTemplateCounts().entrySet()) {
                result.getGiveCouponTemplateCounts().put(entry.getKey(),
                        result.getGiveCouponTemplateCounts().getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
    }

    /**
     * 获得满减送的订单项（商品）列表
     *
     * @param result 计算结果
     * @param rewardActivity 满减送活动
     * @return 订单项（商品）列表
     */
    private List<TradePriceCalculateRespBO.OrderItem> filterMatchActivityOrderItems(TradePriceCalculateRespBO result,
                                                                                    RewardActivityMatchRespDTO rewardActivity) {
        Integer productScope = rewardActivity.getProductScope();
        if(PromotionProductScopeEnum.isAll(productScope)){
            return result.getItems();
        }else if (PromotionProductScopeEnum.isSpu(productScope)) {
            return filterList(result.getItems(),
                    orderItem -> CollUtil.contains(rewardActivity.getProductScopeValues(), orderItem.getSpuId()));
        }else if (PromotionProductScopeEnum.isCategory(productScope)) {
            return filterList(result.getItems(),
                    orderItem -> CollUtil.contains(rewardActivity.getProductScopeValues(), orderItem.getCategoryId()));
        }else{
            throw exception(REWARD_ACTIVITY_TYPE_NOT_EXISTS);
        }
    }

    /**
     * 获得最大匹配的满减送活动的规则
     *
     * @param rewardActivity 满减送活动
     * @param orderItems     商品项
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
     * 获得满减送活动不匹配时的提示
     *
     * @param rewardActivity 满减送活动
     * @return 提示
     */
    private String getRewardActivityNotMeetTip(RewardActivityMatchRespDTO rewardActivity,
                                               List<TradePriceCalculateRespBO.OrderItem> orderItems) {
        // 1. 计算数量和价格
        Integer count = TradePriceCalculatorHelper.calculateTotalCount(orderItems);
        Integer price = TradePriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        assert count != null && price != null;

        // 2. 构建不满足时的提示信息：按最低档规则算
        String meetTip = "满减送：购满 {} {}，可以减 {} 元";
        List<RewardActivityMatchRespDTO.Rule> rules = new ArrayList<>(rewardActivity.getRules());
        rules.sort(Comparator.comparing(RewardActivityMatchRespDTO.Rule::getLimit)); // 按优惠门槛升序
        RewardActivityMatchRespDTO.Rule rule = rules.get(0);
        if (PromotionConditionTypeEnum.PRICE.getType().equals(rewardActivity.getConditionType())) {
            return StrUtil.format(meetTip, rule.getLimit(), "元", MoneyUtils.fenToYuanStr(rule.getDiscountPrice()));
        }
        if (PromotionConditionTypeEnum.COUNT.getType().equals(rewardActivity.getConditionType())) {
            return StrUtil.format(meetTip, rule.getLimit(), "件", MoneyUtils.fenToYuanStr(rule.getDiscountPrice()));
        }
        return StrUtil.EMPTY;
    }

}
