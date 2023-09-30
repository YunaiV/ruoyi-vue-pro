package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterPayOrderReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;

/**
 * 订单活动特殊逻辑处理器 handler 默认抽象实现类
 *
 * @author HUIHUI
 */
public abstract class TradeOrderDefaultHandler implements TradeOrderHandler {

    @Override
    public void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {

    }

    @Override
    public void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {

    }

    @Override
    public void afterPayOrder(TradeAfterPayOrderReqBO reqBO) {

    }

    @Override
    public void cancelOrder() {

    }

}
