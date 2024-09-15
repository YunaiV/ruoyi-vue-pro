package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper.formatPrice;

/**
 * 会员 VIP 折扣的 {@link TradePriceCalculator} 实现类
 *
 * @author 芋道源码
 */
@Component
@Order(TradePriceCalculator.ORDER_MEMBER_LEVEL)
public class TradeMemberLevelPriceCalculator implements TradePriceCalculator {

    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private MemberUserApi memberUserApi;

    /**
     * 会员计算迁移到限时优惠计算里
     * @param param
     * @param result
     */
    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
//        // 0. 只有【普通】订单，才计算该优惠
//        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
//            return;
//        }
//        // 1. 获得用户的会员等级
//        MemberUserRespDTO user = memberUserApi.getUser(param.getUserId());
//        if (user.getLevelId() == null || user.getLevelId() <= 0) {
//            return;
//        }
//        MemberLevelRespDTO level = memberLevelApi.getMemberLevel(user.getLevelId());
//        if (level == null || level.getDiscountPercent() == null) {
//            return;
//        }
//
//        // 2. 计算每个 SKU 的优惠金额
//        result.getItems().forEach(orderItem -> {
//            // 2.1 计算优惠金额
//            Integer vipPrice = calculateVipPrice(orderItem.getPayPrice(), level.getDiscountPercent());
//            if (vipPrice <= 0) {
//                return;
//            }
//
//            // 2.2 记录优惠明细
//            if (orderItem.getSelected()) {
//                // 注意，只有在选中的情况下，才会记录到优惠明细。否则仅仅是更新 SKU 优惠金额，用于展示
//                TradePriceCalculatorHelper.addPromotion(result, orderItem,
//                        level.getId(), level.getName(), PromotionTypeEnum.MEMBER_LEVEL.getType(),
//                        String.format("会员等级折扣：省 %s 元", formatPrice(vipPrice)),
//                        vipPrice);
//            }
//
//            // 2.3 更新 SKU 的优惠金额
//            orderItem.setVipPrice(vipPrice);
//            TradePriceCalculatorHelper.recountPayPrice(orderItem);
//        });
//        TradePriceCalculatorHelper.recountAllPrice(result);
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
        Integer newPrice = price * discountPercent / 100;
        return price - newPrice;
    }

}
