package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainActivityApi;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainRecordApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 砍价订单的 {@link TradeOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeBargainOrderHandler implements TradeOrderHandler {

    @Resource
    private BargainActivityApi bargainActivityApi;
    @Resource
    private BargainRecordApi bargainRecordApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (!TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "砍价时，只允许选择一个商品");

        // 扣减砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(order.getBargainActivityId(),
                -orderItems.get(0).getCount());
    }

    @Override
    public void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (!TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "砍价时，只允许选择一个商品");

        // 记录砍价记录对应的订单编号
        bargainRecordApi.updateBargainRecordOrderId(order.getBargainRecordId(), order.getId());
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (!TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "砍价时，只允许选择一个商品");

        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        afterCancelOrderItem(order, orderItems.get(0));
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        if (!TradeOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 恢复（增加）砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(order.getBargainActivityId(), orderItem.getCount());
    }

}
