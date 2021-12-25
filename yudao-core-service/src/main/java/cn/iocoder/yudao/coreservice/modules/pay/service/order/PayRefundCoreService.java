package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundReqBO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundRespBO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;

/**
 * 退款单 Core Service
 *
 * @author jason
 */
public interface PayRefundCoreService {

    // TODO @jason：方法名改成，submitRefundOrder，发起退款订单。这样和发起支付单，保持一致
    /**
     * 提交退款申请
     *
     * @param reqDTO 退款申请信息
     * @return 退款申请返回信息
     */
    PayRefundRespBO refund(PayRefundReqBO reqDTO);

    /**
     * 渠道的退款通知
     *
     * @param channelId  渠道编号
     * @param notifyData  通知数据
     * @throws Exception 退款通知异常
     */
    void notifyPayRefund(Long channelId, PayNotifyDataDTO notifyData) throws Exception;

}
