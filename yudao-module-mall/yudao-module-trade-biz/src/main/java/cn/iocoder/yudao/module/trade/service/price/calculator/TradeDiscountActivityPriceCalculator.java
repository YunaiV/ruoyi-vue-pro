package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.api.discount.DiscountActivityApi;
import cn.iocoder.yudao.module.promotion.api.discount.dto.DiscountProductRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper.formatPrice;

/**
 * 限时折扣的 {@link TradePriceCalculator} 实现类
 *
 * 由于“会员折扣”和“限时折扣”是冲突，需要选择优惠金额多的，所以也放在这里计算
 *
 * @author 芋道源码
 */
@Component
@Order(TradePriceCalculator.ORDER_DISCOUNT_ACTIVITY)
public class TradeDiscountActivityPriceCalculator implements TradePriceCalculator {

    @Resource
    private DiscountActivityApi discountActivityApi;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 0. 只有【普通】订单，才计算该优惠
        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
            return;
        }

        // 1.1 获得 SKU 对应的限时折扣活动
        List<DiscountProductRespDTO> discountProducts = discountActivityApi.getMatchDiscountProductList(
                convertSet(result.getItems(), TradePriceCalculateRespBO.OrderItem::getSkuId));
        Map<Long, DiscountProductRespDTO> discountProductMap = convertMap(discountProducts, DiscountProductRespDTO::getSkuId);
        // 1.2 获得会员等级
        MemberLevelRespDTO level = getMemberLevel(param.getUserId());

        // 2. 计算每个 SKU 的优惠金额
        result.getItems().forEach(orderItem -> {
            if (!orderItem.getSelected()) {
                return;
            }
            // 2.1 计算限时折扣的优惠金额
            DiscountProductRespDTO discountProduct = discountProductMap.get(orderItem.getSkuId());
            Integer discountPrice = calculateActivityPrice(discountProduct, orderItem);
            // 2.2 计算 VIP 优惠金额
            Integer vipPrice = calculateVipPrice(level, orderItem);
            if (discountPrice <= 0 && vipPrice <= 0) {
                return;
            }

            // 3. 选择优惠金额多的
            if (discountPrice > vipPrice) {
                TradePriceCalculatorHelper.addPromotion(result, orderItem,
                        discountProduct.getActivityId(), discountProduct.getActivityName(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(),
                        StrUtil.format("限时折扣：省 {} 元", formatPrice(discountPrice)),
                        discountPrice);
                // 更新 SKU 优惠金额
                orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
            } else {
                assert level != null;
                TradePriceCalculatorHelper.addPromotion(result, orderItem,
                        level.getId(), level.getName(), PromotionTypeEnum.MEMBER_LEVEL.getType(),
                        String.format("会员等级折扣：省 %s 元", formatPrice(vipPrice)),
                        vipPrice);
                // 更新 SKU 的优惠金额
                orderItem.setVipPrice(vipPrice);
            }

            // 4. 分摊优惠
            TradePriceCalculatorHelper.recountPayPrice(orderItem);
            TradePriceCalculatorHelper.recountAllPrice(result);
        });
    }

    /**
     * 获得用户的等级
     *
     * @param userId 用户编号
     * @return 用户等级
     */
    public MemberLevelRespDTO getMemberLevel(Long userId) {
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        if (user == null || user.getLevelId() == null || user.getLevelId() <= 0) {
            return null;
        }
        return memberLevelApi.getMemberLevel(user.getLevelId());
    }

    /**
     * 计算优惠活动的价格
     *
     * @param discount 优惠活动
     * @param orderItem 交易项
     * @return 优惠价格
     */
    public Integer calculateActivityPrice(DiscountProductRespDTO discount,
                                           TradePriceCalculateRespBO.OrderItem orderItem) {
        if (discount == null) {
            return 0;
        }
        Integer newPrice = orderItem.getPayPrice();
        if (PromotionDiscountTypeEnum.PRICE.getType().equals(discount.getDiscountType())) { // 减价
            newPrice -= discount.getDiscountPrice() * orderItem.getCount();
        } else if (PromotionDiscountTypeEnum.PERCENT.getType().equals(discount.getDiscountType())) { // 打折
            newPrice = newPrice * discount.getDiscountPercent() / 100;
        } else {
            throw new IllegalArgumentException(String.format("优惠活动的商品(%s) 的优惠类型不正确", discount));
        }
        return orderItem.getPayPrice() - newPrice;
    }

    /**
     * 计算会员 VIP 的优惠价格
     *
     * @param level 会员等级
     * @param orderItem 交易项
     * @return 优惠价格
     */
    public Integer calculateVipPrice(MemberLevelRespDTO level,
                                      TradePriceCalculateRespBO.OrderItem orderItem) {
        if (level == null || level.getDiscountPercent() == null) {
            return 0;
        }
        Integer newPrice = MoneyUtils.calculateRatePrice(orderItem.getPayPrice(), level.getDiscountPercent().doubleValue());
        return orderItem.getPayPrice() - newPrice;
    }

}
