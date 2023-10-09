package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.promotion.api.bargain.BargainActivityApi;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainRecordApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 砍价订单 handler 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeBargainHandler implements TradeOrderHandler {

    @Resource
    private BargainActivityApi bargainActivityApi;
    @Resource
    private BargainRecordApi bargainRecordApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }

        // 扣减砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(order.getBargainActivityId(),
                -orderItems.get(0).getCount());
    }

    @Override
    public void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }

        // 记录砍价记录对应的订单编号
        bargainRecordApi.updateBargainRecordOrderId(order.getBargainRecordId(), order.getId());
    }

    @Override
    public void cancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // TODO 芋艿：取消订单时，需要增加库存
    }

}
