package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.seckill.SeckillActivityApi;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 秒杀订单 handler 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeSeckillHandler implements TradeOrderHandler {

    @Resource
    private SeckillActivityApi seckillActivityApi;

    @Override
    public void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {
        // 如果是秒杀订单：额外扣减秒杀的库存；
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.SECKILL.getType(), reqBO.getOrderType())) {
            return;
        }

        seckillActivityApi.updateSeckillStock(TradeOrderConvert.INSTANCE.convert(reqBO));
    }

    @Override
    public void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {

    }

    @Override
    public void rollbackStock() {

    }

}
