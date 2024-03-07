package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponValidReqDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Predicate;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_NO_MATCH_MIN_PRICE;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_NO_MATCH_SPU;
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
        // 1.1 校验优惠劵
        if (param.getCouponId() == null) {
            return;
        }
        CouponRespDTO coupon = couponApi.validateCoupon(new CouponValidReqDTO()
                .setId(param.getCouponId()).setUserId(param.getUserId()));
        Assert.notNull(coupon, "校验通过的优惠劵({})，不能为空", param.getCouponId());
        // 1.2 只有【普通】订单，才允许使用优惠劵
        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
            throw exception(PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER);
        }

        // 2.1 获得匹配的商品 SKU 数组
        List<TradePriceCalculateRespBO.OrderItem> orderItems = filterMatchCouponOrderItems(result, coupon);
        if (CollUtil.isEmpty(orderItems)) {
            throw exception(COUPON_NO_MATCH_SPU);
        }
        // 2.2 计算是否满足优惠劵的使用金额
        Integer totalPayPrice = TradePriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        if (totalPayPrice < coupon.getUsePrice()) {
            throw exception(COUPON_NO_MATCH_MIN_PRICE);
        }

        // 3.1 计算可以优惠的金额
        Integer couponPrice = getCouponPrice(coupon, totalPayPrice);
        Assert.isTrue(couponPrice < totalPayPrice,
                "优惠劵({}) 的优惠金额({})，不能大于订单总金额({})", coupon.getId(), couponPrice, totalPayPrice);
        // 3.2 计算分摊的优惠金额
        List<Integer> divideCouponPrices = TradePriceCalculatorHelper.dividePrice(orderItems, couponPrice);

        // 4.1 记录使用的优惠劵
        result.setCouponId(param.getCouponId());
        // 4.2 记录优惠明细
        TradePriceCalculatorHelper.addPromotion(result, orderItems,
                param.getCouponId(), coupon.getName(), PromotionTypeEnum.COUPON.getType(),
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

    private Integer getCouponPrice(CouponRespDTO coupon, Integer totalPayPrice) {
        if (PromotionDiscountTypeEnum.PRICE.getType().equals(coupon.getDiscountType())) { // 减价
            return coupon.getDiscountPrice();
        } else if (PromotionDiscountTypeEnum.PERCENT.getType().equals(coupon.getDiscountType())) { // 打折
            int couponPrice = totalPayPrice * coupon.getDiscountPercent() / 100;
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
