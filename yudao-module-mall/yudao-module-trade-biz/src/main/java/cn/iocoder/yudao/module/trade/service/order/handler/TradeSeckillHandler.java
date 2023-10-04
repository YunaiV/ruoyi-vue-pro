package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.seckill.SeckillActivityApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.SECKILL.getType(), order.getType())) {
            return;
        }
        // 扣减秒杀活动的库存
        seckillActivityApi.updateSeckillStock(order.getSeckillActivityId(),
                orderItems.get(0).getSkuId(), orderItems.get(0).getCount());
    }

}
