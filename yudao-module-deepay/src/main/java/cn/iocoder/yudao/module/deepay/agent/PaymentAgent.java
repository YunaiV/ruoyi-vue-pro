package cn.iocoder.yudao.module.deepay.agent;

/**
 * PaymentAgent — 生成支付 ID（mock Jeepay / Swan）。
 */
public class PaymentAgent implements Agent {

    @Override
    public Context run(Context ctx) {
        String code = ctx.chainCode != null ? ctx.chainCode : "UNKNOWN";
        ctx.paymentId = "PAY-" + code + "-" + System.currentTimeMillis();
        ctx.paid      = false;
        ctx.iban      = "DEEPAY-" + code;
        return ctx;
    }

}

