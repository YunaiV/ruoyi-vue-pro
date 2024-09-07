package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_COUPON_CAN_NOT_USE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER;

/**
 * 优惠劵的 {@link TradePriceCalculator} 实现类
 *
 * @author 芋道源码
 */
@Component
@Order(TradePriceCalculator.ORDER_COUPON)
public class TradeCouponPriceCalculator implements TradePriceCalculator {

    @Resource
    private CouponApi couponApi;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 只有【普通】订单，才允许使用优惠劵
        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
            if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
                throw exception(PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER);
            }
            return;
        }

        // 1.1 加载用户的优惠劵列表
        List<CouponRespDTO> coupons = couponApi.getCouponListByUserId(param.getUserId(), CouponStatusEnum.UNUSED.getStatus());
        coupons.removeIf(coupon -> LocalDateTimeUtils.beforeNow(coupon.getValidEndTime()));
        // 1.2 计算优惠劵的使用条件
        result.setCoupons(calculateCoupons(coupons, result));

        // 2. 校验优惠劵是否可用
        if (param.getCouponId() == null) {
            return;
        }
        TradePriceCalculateRespBO.Coupon couponBO = CollUtil.findOne(result.getCoupons(), item -> item.getId().equals(param.getCouponId()));
        CouponRespDTO coupon = CollUtil.findOne(coupons, item -> item.getId().equals(param.getCouponId()));
        if (couponBO == null || coupon == null) {
            throw exception(PRICE_CALCULATE_COUPON_CAN_NOT_USE, "优惠劵不存在");
        }
        if (Boolean.FALSE.equals(couponBO.getMatch())) {
            throw exception(PRICE_CALCULATE_COUPON_CAN_NOT_USE, couponBO.getMismatchReason());
        }

        // 3.1 计算可以优惠的金额
        List<TradePriceCalculateRespBO.OrderItem> orderItems = filterMatchCouponOrderItems(result, coupon);
        Integer totalPayPrice = TradePriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        Integer couponPrice = getCouponPrice(coupon, totalPayPrice);
        // 3.2 计算分摊的优惠金额
        List<Integer> divideCouponPrices = TradePriceCalculatorHelper.dividePrice(orderItems, couponPrice);

        // 4.1 记录使用的优惠劵
        result.setCouponId(param.getCouponId());
        // 4.2 记录优惠明细
        TradePriceCalculatorHelper.addPromotion(result, orderItems,
                param.getCouponId(), couponBO.getName(), PromotionTypeEnum.COUPON.getType(),
                StrUtil.format("优惠劵：省 {} 元", TradePriceCalculatorHelper.formatPrice(couponPrice)),
                divideCouponPrices);
        // 4.3 更新 SKU 优惠金额
        for (int i = 0; i < orderItems.size(); i++) {
            TradePriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            orderItem.setCouponPrice(divideCouponPrices.get(i));
            TradePriceCalculatorHelper.recountPayPrice(orderItem);
        }
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 计算用户的优惠劵列表（可用 + 不可用）
     *
     * @param coupons 优惠劵
     * @param result 计算结果
     * @return 优惠劵列表
     */
    private List<TradePriceCalculateRespBO.Coupon> calculateCoupons(List<CouponRespDTO> coupons,
                                                                    TradePriceCalculateRespBO result) {
        return convertList(coupons, coupon -> {
            TradePriceCalculateRespBO.Coupon matchCoupon = BeanUtils.toBean(coupon, TradePriceCalculateRespBO.Coupon.class);
            // 1.1 优惠劵未到使用时间
            if (LocalDateTimeUtils.afterNow(coupon.getValidStartTime())) {
                return matchCoupon.setMatch(false).setMismatchReason("优惠劵未到使用时间");
            }
            // 1.2 优惠劵没有匹配的商品
            List<TradePriceCalculateRespBO.OrderItem> orderItems = filterMatchCouponOrderItems(result, coupon);
            if (CollUtil.isEmpty(orderItems)) {
                return matchCoupon.setMatch(false).setMismatchReason("优惠劵没有匹配的商品");
            }
            // 1.3 差 %1$,.2f 元可用优惠劵
            Integer totalPayPrice = TradePriceCalculatorHelper.calculateTotalPayPrice(orderItems);
            if (totalPayPrice < coupon.getUsePrice()) {
                return matchCoupon.setMatch(false)
                        .setMismatchReason(String.format("差 %1$,.2f 元可用优惠劵", (coupon.getUsePrice() - totalPayPrice) / 100D));
            }
            // 1.4 优惠金额超过订单金额
            Integer couponPrice = getCouponPrice(coupon, totalPayPrice);
            if (couponPrice >= totalPayPrice) {
                return matchCoupon.setMatch(false).setMismatchReason("优惠金额超过订单金额");
            }

            // 2. 满足条件
            return matchCoupon.setMatch(true);
        });
    }

    private Integer getCouponPrice(CouponRespDTO coupon, Integer totalPayPrice) {
        if (PromotionDiscountTypeEnum.PRICE.getType().equals(coupon.getDiscountType())) { // 减价
            return coupon.getDiscountPrice();
        } else if (PromotionDiscountTypeEnum.PERCENT.getType().equals(coupon.getDiscountType())) { // 打折
            int couponPrice = totalPayPrice - (totalPayPrice * coupon.getDiscountPercent() / 100);
            return coupon.getDiscountLimitPrice() == null ? couponPrice
                    : Math.min(couponPrice, coupon.getDiscountLimitPrice()); // 优惠上限
        }
        throw new IllegalArgumentException(String.format("优惠劵(%s) 的优惠类型不正确", coupon));
    }

    /**
     * 获得优惠劵可使用的订单项（商品）列表
     *
     * @param result 计算结果
     * @param coupon 优惠劵
     * @return 订单项（商品）列表
     */
    private List<TradePriceCalculateRespBO.OrderItem> filterMatchCouponOrderItems(TradePriceCalculateRespBO result,
                                                                                  CouponRespDTO coupon) {
        Predicate<TradePriceCalculateRespBO.OrderItem> matchPredicate = TradePriceCalculateRespBO.OrderItem::getSelected;
        if (PromotionProductScopeEnum.SPU.getScope().equals(coupon.getProductScope())) {
            matchPredicate = matchPredicate // 额外加如下条件
                    .and(orderItem -> coupon.getProductScopeValues().contains(orderItem.getSpuId()));
        } else if (PromotionProductScopeEnum.CATEGORY.getScope().equals(coupon.getProductScope())) {
            matchPredicate = matchPredicate // 额外加如下条件
                    .and(orderItem -> coupon.getProductScopeValues().contains(orderItem.getCategoryId()));
        }
        return filterList(result.getItems(), matchPredicate);
    }

}
