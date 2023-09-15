package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;

/**
 * 订单活动特殊逻辑处理器 handler 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderHandler {

    /**
     * 订单创建前
     *
     * @param reqBO 请求
     */
    void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO);

    /**
     * 订单创建后
     *
     * @param reqBO 请求
     */
    void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO);

    /**
     * 回滚活动相关库存
     */
    void rollbackStock();

}
