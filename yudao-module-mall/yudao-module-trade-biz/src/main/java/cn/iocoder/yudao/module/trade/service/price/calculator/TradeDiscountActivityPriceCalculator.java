package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper.formatPrice;

/**
 * 限时折扣的 {@link TradePriceCalculator} 实现类
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

        boolean discount;
        boolean vip;

        //----------------------------------限时折扣计算-----------------------------------------
        // 获得 SKU 对应的限时折扣活动
        List<DiscountProductRespDTO> discountProducts = discountActivityApi.getMatchDiscountProductList(
                convertSet(result.getItems(), TradePriceCalculateRespBO.OrderItem::getSkuId));
        if (CollUtil.isEmpty(discountProducts)) {
            discount = false;
        }else {
            discount = true;
        }
        Map<Long, DiscountProductRespDTO> discountProductMap = convertMap(discountProducts, DiscountProductRespDTO::getSkuId);



        //----------------------------------会员计算-----------------------------------------
        MemberLevelRespDTO level;
        // 获得用户的会员等级
        MemberUserRespDTO user = memberUserApi.getUser(param.getUserId());
        if (user.getLevelId() != null && user.getLevelId() > 0) {
            level = memberLevelApi.getMemberLevel(user.getLevelId());
            if (level != null && level.getDiscountPercent() != null) {
                vip = true;
            }else {
                vip = false;
            }
        }else {
            level = null;
            vip = false;
        }


        // 2. 计算每个 SKU 的优惠金额
        result.getItems().forEach(orderItem -> {

            //----------------------------------限时折扣计算-----------------------------------------
            DiscountProductRespDTO discountProduct = null;
            Integer newDiscountPrice = 0;
            if (discount){
                // 2.1  计算限时折扣优惠信息
                discountProduct = discountProductMap.get(orderItem.getSkuId());
                if (discountProduct != null) {
                    // 2.2 计算优惠金额
                    Integer newPayPrice = calculatePayPrice(discountProduct, orderItem);
                    newDiscountPrice = orderItem.getPayPrice() - newPayPrice;
                }
            }


            //----------------------------------会员计算-----------------------------------------
            Integer vipPrice = 0;
            if (vip){
                // 2.3 计算会员优惠金额
                vipPrice = calculateVipPrice(orderItem.getPayPrice(), level.getDiscountPercent());
            }


            // 2.4 记录优惠明细
            // 注意，只有在选中的情况下，才会记录到优惠明细。否则仅仅是更新 SKU 优惠金额，用于展示
            if (orderItem.getSelected()) {
                if (discount && vip){
                    if(newDiscountPrice > vipPrice){
                        TradePriceCalculatorHelper.addPromotion(result, orderItem,
                                discountProduct.getActivityId(), discountProduct.getActivityName(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(),
                                StrUtil.format("限时折扣：省 {} 元", formatPrice(newDiscountPrice)),
                                newDiscountPrice);
                        // 2.5 更新 SKU 优惠金额
                        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + newDiscountPrice);
                    }else{
                        TradePriceCalculatorHelper.addPromotion(result, orderItem,
                                level.getId(), level.getName(), PromotionTypeEnum.MEMBER_LEVEL.getType(),
                                String.format("会员等级折扣：省 %s 元", formatPrice(vipPrice)),
                                vipPrice);
                        // 2.5 更新 SKU 的优惠金额
                        orderItem.setVipPrice(vipPrice);
                    }
                }else if (discount){
                    TradePriceCalculatorHelper.addPromotion(result, orderItem,
                            discountProduct.getActivityId(), discountProduct.getActivityName(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(),
                            StrUtil.format("限时折扣：省 {} 元", formatPrice(newDiscountPrice)),
                            newDiscountPrice);
                    // 2.5 更新 SKU 优惠金额
                    orderItem.setDiscountPrice(orderItem.getDiscountPrice() + newDiscountPrice);
                }else if (vip){
                    TradePriceCalculatorHelper.addPromotion(result, orderItem,
                            level.getId(), level.getName(), PromotionTypeEnum.MEMBER_LEVEL.getType(),
                            String.format("会员等级折扣：省 %s 元", formatPrice(vipPrice)),
                            vipPrice);
                    // 2.5 更新 SKU 的优惠金额
                    orderItem.setVipPrice(vipPrice);
                }
            }

            TradePriceCalculatorHelper.recountPayPrice(orderItem);
        });
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    private Integer calculatePayPrice(DiscountProductRespDTO discountProduct,
                                      TradePriceCalculateRespBO.OrderItem orderItem) {
        Integer price = orderItem.getPayPrice();
        if (PromotionDiscountTypeEnum.PRICE.getType().equals(discountProduct.getDiscountType())) { // 减价
            price -= discountProduct.getDiscountPrice() * orderItem.getCount();
        } else if (PromotionDiscountTypeEnum.PERCENT.getType().equals(discountProduct.getDiscountType())) { // 打折
            price = price * discountProduct.getDiscountPercent() / 100;
        } else {
            throw new IllegalArgumentException(String.format("优惠活动的商品(%s) 的优惠类型不正确", discountProduct));
        }
        return price;
    }

    /**
     * 计算会员 VIP 优惠价格
     *
     * @param price 原价
     * @param discountPercent 折扣
     * @return 优惠价格
     */
    public Integer calculateVipPrice(Integer price, Integer discountPercent) {
        if (discountPercent == null) {
            return 0;
        }
        BigDecimal divide = new BigDecimal(price).multiply(new BigDecimal(discountPercent)).divide(new BigDecimal(100));
        Integer newPrice =  divide.intValue();
        return price - newPrice;
    }

}
