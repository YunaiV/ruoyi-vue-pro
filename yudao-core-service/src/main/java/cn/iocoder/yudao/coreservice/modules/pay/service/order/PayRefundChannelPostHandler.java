package cn.iocoder.yudao.coreservice.modules.pay.service.order;

import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundPostReqBO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;

/**
 * 支付退款订单 ，渠道返回后 后置处理
 *
 * @author jason
 */
public interface PayRefundChannelPostHandler {

    /**
     * 支持的渠道返回值
     *
     * @return 支持的渠道返回值数组
     */
    PayChannelRespEnum[] supportHandleResp();

    /**
     * 根据渠道返回，处理支付退款单
     *
     * @param respBO
     */
    void handleRefundChannelResp(PayRefundPostReqBO respBO);
}
