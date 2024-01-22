package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * 商品 SKU 库存的 {@link TradeOrderHandler} 实现类
 *
 * @author 芋道源码
 */
@Component
public class TradeProductSkuOrderHandler implements TradeOrderHandler {

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convertNegative(orderItems));
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convert(orderItems));
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convert(singletonList(orderItem)));
    }

}
