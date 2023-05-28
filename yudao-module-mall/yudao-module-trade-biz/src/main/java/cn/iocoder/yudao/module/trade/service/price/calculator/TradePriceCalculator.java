package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;

/**
 * 价格计算的计算器接口
 *
 * @author 芋道源码
 */
public interface TradePriceCalculator {

    int ORDER_DISCOUNT_ACTIVITY = 10;
    int ORDER_REWARD_ACTIVITY = 20;
    int ORDER_COUPON = 30;

    void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result);

}
