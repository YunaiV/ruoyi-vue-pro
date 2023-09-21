package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 使用积分的 {@link TradePriceCalculator} 实现类
 *
 * @author owen
 */
@Component
@Order(TradePriceCalculator.ORDER_POINT_USE)
@Slf4j
public class TradePointUsePriceCalculator implements TradePriceCalculator {

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // TODO 疯狂：待实现，嘿嘿；
        if (param.getPointStatus()) {
            result.setUsePoint(10);
        } else {
            result.setUsePoint(0);
        }
    }

}
