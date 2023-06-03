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
    /**
     * TODO @芋艿 快递运费的计算在满减之前。 例如有满多少包邮
     */
    int ORDER_DELIVERY = 15;
    int ORDER_REWARD_ACTIVITY = 20;
    int ORDER_COUPON = 30;

    void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result);

}
