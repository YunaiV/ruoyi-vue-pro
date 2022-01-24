package cn.iocoder.yudao.framework.pay.core.enums;

/**
 * 退款通知, 统一的渠道退款状态
 *
 * @author  jason
 */
public enum PayNotifyRefundStatusEnum {
    /**
     * 支付宝 中 全额退款 trade_status=TRADE_CLOSED， 部分退款 trade_status=TRADE_SUCCESS
     * 退款成功
     */
    SUCCESS,

    /**
     * 支付宝退款通知没有这个状态
     * 退款异常
     */
    ABNORMAL;
}
