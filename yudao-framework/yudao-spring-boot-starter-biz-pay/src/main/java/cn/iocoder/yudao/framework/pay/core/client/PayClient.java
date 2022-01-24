package cn.iocoder.yudao.framework.pay.core.client;


import cn.iocoder.yudao.framework.pay.core.client.dto.*;

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
    PayOrderNotifyRespDTO parseOrderNotify(PayNotifyDataDTO data) throws Exception;

    /**
     * 调用支付渠道，进行退款
     * @param reqDTO  统一退款请求信息
     * @return 各支付渠道的统一返回结果
     */
    PayCommonResult<PayRefundUnifiedRespDTO> unifiedRefund(PayRefundUnifiedReqDTO reqDTO);

    /**
     * 解析支付退款通知数据
     * @param notifyData  支付退款通知请求数据
     * @return 支付退款通知的Notify DTO
     */
    PayRefundNotifyDTO parseRefundNotify(PayNotifyDataDTO notifyData);

    // TODO @芋艿：后续改成非 default，避免不知道去实现
    /**
     * 验证是否渠道通知
     *
     * @param notifyData 通知数据
     * @return 默认是 true
     */
    default boolean verifyNotifyData(PayNotifyDataDTO notifyData) {
        return true;
    }

    // TODO @芋艿：后续改成非 default，避免不知道去实现
    /**
     * 判断是否为退款通知
     *
     * @param notifyData  通知数据
     * @return 默认是 false
     */
    default  boolean isRefundNotify(PayNotifyDataDTO notifyData){
        return false;
    }

}
