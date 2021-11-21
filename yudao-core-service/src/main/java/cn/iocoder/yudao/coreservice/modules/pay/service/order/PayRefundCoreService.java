package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundReqBO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundRespBO;

/**
 * 退款单 Core Service
 *
 * @author jason
 */
public interface PayRefundCoreService {


    /**
     * 提交退款申请
     * @param reqDTO 退款申请信息
     * @return 退款申请返回信息
     */
    PayRefundRespBO refund(PayRefundReqBO reqDTO);
}
