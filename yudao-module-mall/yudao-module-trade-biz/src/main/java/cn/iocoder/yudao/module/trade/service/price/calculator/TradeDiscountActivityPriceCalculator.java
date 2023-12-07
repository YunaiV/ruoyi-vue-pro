package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 0. 只有【普通】订单，才计算该优惠
        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.NORMAL.getType())) {
            return;
        }
        // 获得 SKU 对应的限时折扣活动
        List<DiscountProductRespDTO> discountProducts = discountActivityApi.getMatchDiscountProductList(
                convertSet(result.getItems(), TradePriceCalculateRespBO.OrderItem::getSkuId));
        if (CollUtil.isEmpty(discountProducts)) {
            return;
        }
        Map<Long, DiscountProductRespDTO> discountProductMap = convertMap(discountProducts, DiscountProductRespDTO::getSkuId);

        // 处理每个 SKU 的限时折扣
        result.getItems().forEach(orderItem -> {
            // 1. 获取该 SKU 的优惠信息
            DiscountProductRespDTO discountProduct = discountProductMap.get(orderItem.getSkuId());
            if (discountProduct == null) {
                return;
            }
            // 2. 计算优惠金额
            Integer newPayPrice = calculatePayPrice(discountProduct, orderItem);
            Integer newDiscountPrice = orderItem.getPayPrice() - newPayPrice;

            // 3.1 记录优惠明细
            if (orderItem.getSelected()) {
                // 注意，只有在选中的情况下，才会记录到优惠明细。否则仅仅是更新 SKU 优惠金额，用于展示
                TradePriceCalculatorHelper.addPromotion(result, orderItem,
                        discountProduct.getActivityId(), discountProduct.getActivityName(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType(),
                        StrUtil.format("限时折扣：省 {} 元", formatPrice(newDiscountPrice)),
                        newDiscountPrice);
            }
            // 3.2 更新 SKU 优惠金额
            orderItem.setDiscountPrice(orderItem.getDiscountPrice() + newDiscountPrice);
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

}
