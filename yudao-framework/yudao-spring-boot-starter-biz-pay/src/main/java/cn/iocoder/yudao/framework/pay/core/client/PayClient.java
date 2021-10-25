package cn.iocoder.yudao.framework.pay.core.client;

import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;

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
    PayCommonResult<?> unifiedOrder(PayOrderUnifiedReqDTO reqDTO);

    /**
     * 解析支付单的通知结果
     *
     * @param data 通知结果
     * @return 解析结果
     * @throws Exception 解析失败，抛出异常
     */
    PayOrderNotifyRespDTO parseOrderNotify(String data) throws Exception;

}
