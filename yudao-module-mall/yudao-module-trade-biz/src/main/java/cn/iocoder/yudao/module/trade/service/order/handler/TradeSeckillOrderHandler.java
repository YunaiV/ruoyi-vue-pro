package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.promotion.api.seckill.SeckillActivityApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 秒杀订单的 {@link TradeOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeSeckillOrderHandler implements TradeOrderHandler {

    @Resource
    private SeckillActivityApi seckillActivityApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (!TradeOrderTypeEnum.isSeckill(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "秒杀时，只允许选择一个商品");

        // 扣减秒杀活动的库存
        seckillActivityApi.updateSeckillStockDecr(order.getSeckillActivityId(),
                orderItems.get(0).getSkuId(), orderItems.get(0).getCount());
    }

    @Override
    public void cancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (TradeOrderTypeEnum.isSeckill(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "秒杀时，只允许选择一个商品");

        // 恢复秒杀活动的库存
        seckillActivityApi.updateSeckillStockIncr(order.getSeckillActivityId(),
                orderItems.get(0).getSkuId(), orderItems.get(0).getCount());
    }

}
