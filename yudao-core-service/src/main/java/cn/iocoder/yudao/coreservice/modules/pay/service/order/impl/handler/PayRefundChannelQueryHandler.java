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

import java.util.Objects;

/**
 * 支付退款订单渠道返回需调用查询接口的后置处理类
 * {@link PayChannelRespEnum#PROCESSING_QUERY} //TODO 芋道源码 是不是微信有这样的情况
 * {@link PayChannelRespEnum#READ_TIME_OUT_EXCEPTION}
 */
@Service
public class PayRefundChannelQueryHandler extends PayRefundAbstractChannelPostHandler {


    public PayRefundChannelQueryHandler(PayOrderCoreMapper payOrderCoreMapper,
                                        PayRefundCoreMapper payRefundMapper) {
        super(payOrderCoreMapper, payRefundMapper);
    }

    @Override
    public PayChannelRespEnum[] supportHandleResp() {
        return new PayChannelRespEnum[]{PayChannelRespEnum.PROCESSING_QUERY, PayChannelRespEnum.READ_TIME_OUT_EXCEPTION};
    }

    @Override
    public void handleRefundChannelResp(PayRefundPostReqBO respBO) {
        final PayChannelRespEnum respEnum = respBO.getRespEnum();
        PayRefundStatusEnum refundStatus =
                Objects.equals(PayChannelRespEnum.PROCESSING_QUERY, respEnum) ? PayRefundStatusEnum.PROCESSING_QUERY
                        : PayRefundStatusEnum.UNKNOWN_QUERY;
        //更新退款单表
        PayRefundDO updateRefundDO = new PayRefundDO();
        updateRefundDO.setId(respBO.getRefundId())
                .setStatus(refundStatus.getStatus())
                .setChannelErrorCode(respBO.getChannelErrCode())
                .setChannelErrorMsg(respBO.getChannelErrMsg());
        updatePayRefund(updateRefundDO);

        PayOrderDO updateOrderDO = new PayOrderDO();
        //更新订单表
        updateOrderDO.setId(respBO.getOrderId())
                .setRefundTimes(respBO.getRefundedTimes() + 1);
        updatePayOrder(updateOrderDO);

        //TODO 发起查询任务
    }
}
