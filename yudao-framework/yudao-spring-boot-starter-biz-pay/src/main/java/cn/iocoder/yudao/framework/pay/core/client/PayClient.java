package cn.iocoder.yudao.framework.pay.core.client;

import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayNotifyReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayRefundNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedRespDTO;

/**
 * 支付客户端，用于对接各支付渠道的 SDK，实现发起支付、退款等功能
 *
 * @author 芋道源码
 */
public interface PayClient {

    /**
     * 获得渠道编号
     *
     * @return 渠道编号
     */
    Long getId();

    /**
     * 调用支付渠道，统一下单
     *
     * @param reqDTO 下单信息
     * @return 各支付渠道的返回结果
     */
    PayOrderUnifiedRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO);

    /**
     * 调用支付渠道，进行退款
     * @param reqDTO  统一退款请求信息
     * @return 各支付渠道的统一返回结果
     */
    PayRefundUnifiedRespDTO unifiedRefund(PayRefundUnifiedReqDTO reqDTO);

    /**
     * 解析回调数据
     *
     * @param rawNotify 通知内容
     * @return 回调对象
     *         1. {@link PayRefundNotifyRespDTO} 退款通知
     *         2. {@link PayOrderNotifyRespDTO} 支付通知
     */
    default Object parseNotify(PayNotifyReqDTO rawNotify) {
        throw new UnsupportedOperationException("未实现 parseNotify 方法！");
    }

}
