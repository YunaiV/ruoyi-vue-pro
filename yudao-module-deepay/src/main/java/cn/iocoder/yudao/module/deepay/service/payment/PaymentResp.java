package cn.iocoder.yudao.module.deepay.service.payment;

/**
 * PaymentResp — 创建支付单响应 VO。
 *
 * <p>统一封装各渠道返回，业务层只看此类，不接触第三方字段。</p>
 */
public class PaymentResp {

    /** 我方订单号（= outTradeNo，原样回传） */
    private String outTradeNo;

    /**
     * 支付渠道订单号（Jeepay payOrderId / Stripe paymentIntentId 等）。
     * 存入 deepay_order.payment_id，支付回调用此做幂等查询。
     */
    private String paymentId;

    /** 支付跳转链接（二维码URL / H5链接 / 收银台URL）。展示给用户扫码/跳转。 */
    private String payUrl;

    /** 是否成功创建 */
    private boolean success;

    /** 失败原因（success=false 时有值） */
    private String errorMsg;

    // ---- 工厂方法 ----

    public static PaymentResp ok(String outTradeNo, String paymentId, String payUrl) {
        PaymentResp r = new PaymentResp();
        r.outTradeNo = outTradeNo;
        r.paymentId  = paymentId;
        r.payUrl     = payUrl;
        r.success    = true;
        return r;
    }

    public static PaymentResp fail(String outTradeNo, String reason) {
        PaymentResp r = new PaymentResp();
        r.outTradeNo = outTradeNo;
        r.success    = false;
        r.errorMsg   = reason;
        return r;
    }

    // ---- getters ----

    public String  getOutTradeNo() { return outTradeNo; }
    public String  getPaymentId()  { return paymentId; }
    public String  getPayUrl()     { return payUrl; }
    public boolean isSuccess()     { return success; }
    public String  getErrorMsg()   { return errorMsg; }
}
