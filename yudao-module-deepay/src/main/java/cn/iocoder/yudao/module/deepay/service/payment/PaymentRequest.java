package cn.iocoder.yudao.module.deepay.service.payment;

import java.math.BigDecimal;

/**
 * PaymentRequest — 创建支付单请求 VO。
 *
 * <h3>ID 映射规则（必须记住）</h3>
 * <pre>
 * 你的系统        Jeepay字段
 * outTradeNo  →  outTradeNo   ⭐全局唯一，绑定我方paymentId
 * amount(元)  →  amount(分) ×100
 * </pre>
 */
public class PaymentRequest {

    /** 我方唯一订单号（= deepay_order.payment_id，映射到 Jeepay.outTradeNo） */
    private String outTradeNo;

    /** 支付金额（元，插件内部转换为"分"） */
    private java.math.BigDecimal amount;

    /** 商品标题 */
    private String subject;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 扩展信息（可选，原样透传） */
    private String attach;

    /** 货币代码（订单级别，覆盖全局配置，用于 wayCode 路由） */
    private String currency = "EUR";

    // ---- 构造 / Builder ----

    public PaymentRequest() {}

    public PaymentRequest(String outTradeNo, BigDecimal amount, String subject, String notifyUrl) {
        this(outTradeNo, amount, subject, notifyUrl, "EUR");
    }

    public PaymentRequest(String outTradeNo, BigDecimal amount, String subject,
                           String notifyUrl, String currency) {
        this.outTradeNo = outTradeNo;
        this.amount     = amount;
        this.subject    = subject;
        this.notifyUrl  = notifyUrl;
        this.currency   = currency;
    }

    public String      getOutTradeNo() { return outTradeNo; }
    public void        setOutTradeNo(String v) { this.outTradeNo = v; }
    public BigDecimal  getAmount()     { return amount; }
    public void        setAmount(BigDecimal v) { this.amount = v; }
    public String      getSubject()    { return subject; }
    public void        setSubject(String v) { this.subject = v; }
    public String      getNotifyUrl()  { return notifyUrl; }
    public void        setNotifyUrl(String v) { this.notifyUrl = v; }
    public String      getCurrency()   { return currency != null ? currency : "EUR"; }
    public void        setCurrency(String v) { this.currency = v; }
    public String      getAttach()     { return attach; }
    public void        setAttach(String v) { this.attach = v; }
}
