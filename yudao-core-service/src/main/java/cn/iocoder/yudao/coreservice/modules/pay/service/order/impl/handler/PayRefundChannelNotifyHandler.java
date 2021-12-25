package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl.handler;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayRefundCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundAbstractChannelPostHandler;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundPostReqBO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import org.springframework.stereotype.Service;

/**
 * 支付退款订单渠道返回通知 {@link PayChannelRespEnum#PROCESSING_NOTIFY}，后置处理类
 * 支付宝退款单好像没有回调， 微信会触发回调
 */
@Service
public class PayRefundChannelNotifyHandler extends PayRefundAbstractChannelPostHandler {

    public PayRefundChannelNotifyHandler(PayOrderCoreMapper payOrderCoreMapper,
                                         PayRefundCoreMapper payRefundMapper) {
        super(payOrderCoreMapper, payRefundMapper);
    }

    @Override
    public PayChannelRespEnum[] supportHandleResp() {
        return new PayChannelRespEnum[] {PayChannelRespEnum.PROCESSING_NOTIFY};
    }

    @Override
    public void handleRefundChannelResp(PayRefundPostReqBO respBO) {
        PayRefundDO updateRefundDO = new PayRefundDO();
        //更新退款单表
        updateRefundDO.setId(respBO.getRefundId())
                .setStatus(PayRefundStatusEnum.PROCESSING_NOTIFY.getStatus());
        updatePayRefund(updateRefundDO);

        PayOrderDO updateOrderDO = new PayOrderDO();
        //更新订单表
        updateOrderDO.setId(respBO.getOrderId())
                .setRefundTimes(respBO.getRefundedTimes() + 1);
        updatePayOrder(updateOrderDO);

    }
}
