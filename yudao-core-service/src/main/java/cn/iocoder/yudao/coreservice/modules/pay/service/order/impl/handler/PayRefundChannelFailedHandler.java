package cn.iocoder.yudao.coreservice.modules.pay.service.order.impl.handler;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayOrderCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order.PayRefundCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.notify.PayNotifyCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayRefundAbstractChannelPostHandler;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayRefundPostReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 支付退款订单渠道返回失败的后置处理类
 * {@link PayChannelRespEnum#CALL_EXCEPTION}
 * {@link PayChannelRespEnum#CAN_NOT_RETRY_FAILURE}
 */
@Service
public class PayRefundChannelFailedHandler extends PayRefundAbstractChannelPostHandler {

    @Resource
    private PayNotifyCoreService payNotifyCoreService;

    public PayRefundChannelFailedHandler(PayOrderCoreMapper payOrderCoreMapper, PayRefundCoreMapper payRefundMapper) {
        super(payOrderCoreMapper, payRefundMapper);
    }

    @Override
    public PayChannelRespEnum[] supportHandleResp() {
        return new PayChannelRespEnum[] {PayChannelRespEnum.CALL_EXCEPTION, PayChannelRespEnum.CAN_NOT_RETRY_FAILURE};
    }


    @Override
    public void handleRefundChannelResp(PayRefundPostReqDTO req) {
        //退款失败
        //更新退款单表
        PayRefundDO updateRefundDO = new PayRefundDO();

        updateRefundDO.setId(req.getRefundId())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelErrorCode(req.getChannelErrCode())
                .setChannelErrorMsg(Optional.ofNullable(req.getChannelErrMsg())
                                          .orElse(req.getExceptionMsg()));
        updatePayRefund(updateRefundDO);
        PayOrderDO updateOrderDO = new PayOrderDO();
        //更新订单表
        updateOrderDO.setId(req.getOrderId())
                .setRefundTimes(req.getRefundedTimes() + 1);
        updatePayOrder(updateOrderDO);
        // 立刻插入退款通知记录
        // TODO 通知商户成功或者失败. 现在通知似乎没有实现， 只是回调
        payNotifyCoreService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                .type(PayNotifyTypeEnum.REFUND.getType()).dataId(req.getRefundId()).build());
    }
}
