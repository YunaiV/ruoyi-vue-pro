package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.promotion.api.seckill.SeckillActivityApi;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_SECKILL_TOTAL_LIMIT_COUNT;

// TODO huihui：单测需要补充
/**
 * 秒杀活动的 {@link TradePriceCalculator} 实现类
 *
 * @author HUIHUI
 */
@Component
@Order(TradePriceCalculator.ORDER_SECKILL_ACTIVITY)
public class TradeSeckillActivityPriceCalculator implements TradePriceCalculator {

    @Resource
    private SeckillActivityApi seckillActivityApi;

    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1. 判断订单类型和是否具有秒杀活动编号
        if (param.getSeckillActivityId() == null) {
            return;
        }
        Assert.isTrue(param.getItems().size() == 1, "秒杀时，只允许选择一个商品");
        // 2. 校验是否可以参与秒杀
        TradePriceCalculateRespBO.OrderItem orderItem = result.getItems().get(0);
        SeckillValidateJoinRespDTO seckillActivity = validateJoinSeckill(
                param.getUserId(), param.getSeckillActivityId(),
                orderItem.getSkuId(), orderItem.getCount());

        // 3.1 记录优惠明细
        Integer discountPrice = orderItem.getPayPrice() - seckillActivity.getSeckillPrice() * orderItem.getCount();
        TradePriceCalculatorHelper.addPromotion(result, orderItem,
                param.getSeckillActivityId(), seckillActivity.getName(), PromotionTypeEnum.SECKILL_ACTIVITY.getType(),
                StrUtil.format("秒杀活动：省 {} 元", TradePriceCalculatorHelper.formatPrice(discountPrice)),
                discountPrice);
        // 3.2 更新 SKU 优惠金额
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
        TradePriceCalculatorHelper.recountPayPrice(orderItem);
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    private SeckillValidateJoinRespDTO validateJoinSeckill(Long userId, Long activityId, Long skuId, Integer count) {
        // 1. 校验是否可以参与秒杀
        SeckillValidateJoinRespDTO seckillActivity = seckillActivityApi.validateJoinSeckill(activityId, skuId, count);
        // 2. 校验总限购数量，目前只有 trade 有具体下单的数据，需要交给 trade 价格计算使用
        int seckillProductCount = tradeOrderQueryService.getSeckillProductCount(userId, activityId);
        if (seckillProductCount + count > seckillActivity.getTotalLimitCount()) {
            throw exception(PRICE_CALCULATE_SECKILL_TOTAL_LIMIT_COUNT);
        }
        return seckillActivity;
    }

}
