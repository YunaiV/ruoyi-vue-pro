package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

// TODO @puhui999：单测可以后补下

/**
 * 拼团活动的 {@link TradePriceCalculator} 实现类
 *
 * @author HUIHUI
 */
@Component
@Order(TradePriceCalculator.ORDER_COMBINATION_ACTIVITY)
public class TradeCombinationActivityPriceCalculator implements TradePriceCalculator {

    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1. 判断订单类型和是否具有拼团活动编号
        if (param.getCombinationActivityId() == null) {
            return;
        }
        Assert.isTrue(param.getItems().size() == 1, "拼团时，只允许选择一个商品");
        // 2. 校验是否可以参与拼团
        TradePriceCalculateRespBO.OrderItem orderItem = result.getItems().get(0);
        CombinationValidateJoinRespDTO combinationActivity = combinationRecordApi.validateJoinCombination(
                param.getUserId(), param.getCombinationActivityId(), param.getCombinationHeadId(),
                orderItem.getSkuId(), orderItem.getCount());

        // 3.1 记录优惠明细
        Integer discountPrice = orderItem.getPayPrice() - combinationActivity.getCombinationPrice() * orderItem.getCount();
        TradePriceCalculatorHelper.addPromotion(result, orderItem,
                param.getCombinationActivityId(), combinationActivity.getName(), PromotionTypeEnum.COMBINATION_ACTIVITY.getType(),
                StrUtil.format("拼团活动：省 {} 元", TradePriceCalculatorHelper.formatPrice(discountPrice)),
                discountPrice);
        // 3.2 更新 SKU 优惠金额
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
        TradePriceCalculatorHelper.recountPayPrice(orderItem);
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

}
