package cn.iocoder.yudao.module.pay.enums;

/**
 * Pay 字典类型的枚举类
 *
 * @author 芋道源码
 */
public interface DictTypeConstants {

    /**
     * 支付-订单-订单状态
     */
    String PAY_ORDER_STATUS = "pay_order_status";

    /**
     * 支付-订单-订单回调商户状态
     */
    String PAY_ORDER_NOTIFY_STATUS = "pay_order_notify_status";

    /**
     * 支付-订单-订单退款状态
     */
    String PAY_ORDER_REFUND_STATUS = "pay_order_refund_status";

    /**
     * 支付-退款订单-退款状态
     */
    String PAY_REFUND_ORDER_STATUS = "pay_refund_order_status";

    /**
     * 支付-退款订单-退款类别
     */
    String PAY_REFUND_ORDER_TYPE = "pay_refund_order_type";

}
