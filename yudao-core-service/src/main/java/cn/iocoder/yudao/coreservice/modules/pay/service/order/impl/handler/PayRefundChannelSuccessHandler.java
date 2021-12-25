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
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundPostReqBO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
/**
 * 支付退款订单渠道返回退款成功的后置处理类
 * {@link PayChannelRespEnum#SYNC_SUCCESS}
 */
@Service
public class PayRefundChannelSuccessHandler extends PayRefundAbstractChannelPostHandler {


    @Resource
    private PayNotifyCoreService payNotifyCoreService;


    public PayRefundChannelSuccessHandler(PayOrderCoreMapper payOrderCoreMapper,
                                          PayRefundCoreMapper payRefundMapper) {
        super(payOrderCoreMapper, payRefundMapper);
    }

    @Override
    public PayChannelRespEnum[] supportHandleResp() {
        return new PayChannelRespEnum[]{PayChannelRespEnum.SYNC_SUCCESS};
    }

    @Override
    public void handleRefundChannelResp(PayRefundPostReqBO respBO) {
        //退款成功
        PayRefundDO updateRefundDO = new PayRefundDO();
        //更新退款单表
        updateRefundDO.setId(respBO.getRefundId())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelRefundNo(respBO.getChannelRefundNo())
                .setSuccessTime(new Date());
        updatePayRefund(updateRefundDO);

        PayOrderDO updateOrderDO = new PayOrderDO();
        //更新订单表
        updateOrderDO.setId(respBO.getOrderId())
                .setRefundTimes(respBO.getRefundedTimes() + 1)
                .setRefundStatus(respBO.getRefundTypeEnum().getStatus())
                .setRefundAmount(respBO.getRefundedAmount()+respBO.getRefundAmount());
         updatePayOrder(updateOrderDO);

         // 立刻插入退款通知记录
        // TODO 通知商户成功或者失败. 现在通知似乎没有实现， 只是回调
        payNotifyCoreService.createPayNotifyTask(PayNotifyTaskCreateReqDTO.builder()
                .type(PayNotifyTypeEnum.REFUND.getType()).dataId(respBO.getRefundId()).build());
    }
}
