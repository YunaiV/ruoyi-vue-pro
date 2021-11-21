package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayRefundMapper;

/**
 * 支付退款订单渠道返回后 , 后置处理抽象类， 处理公用的逻辑
 * @author  jason
 */
public abstract  class PayRefundAbstractChannelPostHandler implements PayRefundChannelPostHandler {

    private final PayOrderCoreMapper payOrderCoreMapper;
    private final PayRefundMapper payRefundMapper;

    public PayRefundAbstractChannelPostHandler(PayOrderCoreMapper payOrderCoreMapper,
                                               PayRefundMapper payRefundMapper){
        this.payOrderCoreMapper = payOrderCoreMapper;
        this.payRefundMapper = payRefundMapper;
    }


    /**
     * 更新退款单
     * @param refundDO  需要更新的退款单信息
     */
    protected void updatePayRefund(PayRefundDO refundDO){
        payRefundMapper.updateById(refundDO);
    }


    /**
     * 更新原始支付订单
     * @param payOrderDO 支付订单信息
     */
    protected void updatePayOrder(PayOrderDO payOrderDO){
        payOrderCoreMapper.updateById(payOrderDO);
    }
}
