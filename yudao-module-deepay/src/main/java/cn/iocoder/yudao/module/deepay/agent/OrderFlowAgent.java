package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.service.JeepayProperties;
import cn.iocoder.yudao.module.deepay.service.PaymentServiceV2;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderFlowAgent — Phase 10 升级版订单流（替代 FinanceAgent 进入主链路）。
 *
 * <h3>与 FinanceAgent 的区别</h3>
 * <ul>
 *   <li>通过 {@link PaymentServiceV2} → {@link cn.iocoder.yudao.module.deepay.service.payment.PaymentPluginManager}
 *       创建支付单，完全解耦 Jeepay</li>
 *   <li>防重复下单：同一 chainCode + userId 已有 PENDING 订单时直接复用</li>
 *   <li>切换 Jeepay → Stripe：只改 {@code deepay.payment.provider}，此类无需改动</li>
 * </ul>
 */
@Component
public class OrderFlowAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(OrderFlowAgent.class);

    @Resource private DeepayOrderMapper  deepayOrderMapper;
    @Resource private PaymentServiceV2   paymentService;
    @Resource private JeepayProperties   jeepayProperties;

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[OrderFlowAgent] shouldProduce=false，跳过");
            return ctx;
        }

        String     code   = ctx.chainCode != null ? ctx.chainCode : "UNKNOWN";
        BigDecimal amount = ctx.price     != null ? ctx.price     : BigDecimal.ZERO;

        // ── 防重复下单（同 chainCode + userId）──────────────────────
        if (ctx.userId != null) {
            DeepayOrderDO existing = deepayOrderMapper.selectByChainCodeAndUserId(code, ctx.userId);
            if (existing != null && "PENDING".equals(existing.getStatus())) {
                log.info("[OrderFlowAgent] 复用已有 PENDING 订单 orderId={} paymentId={}",
                        existing.getId(), existing.getPaymentId());
                ctx.orderId   = String.valueOf(existing.getId());
                ctx.paymentId = existing.getPaymentId();
                ctx.paid      = false;
                return ctx;
            }
        }

        // ── 通过支付中台创建支付单（不知道 Jeepay 存在）────────────
        String subject   = StringUtils.hasText(ctx.title) ? ctx.title : ("Deepay-" + code);
        // outTradeNo = 我方唯一标识（= paymentId，映射到 Jeepay outTradeNo）
        String outTradeNo = "PAY-" + code + "-" + System.currentTimeMillis();

        PaymentResp resp = paymentService.create(
                outTradeNo, amount, subject, jeepayProperties.getNotifyUrl());

        // ── 落库订单 ─────────────────────────────────────────────────
        DeepayOrderDO order = new DeepayOrderDO();
        order.setPaymentId(outTradeNo);          // 我方ID = outTradeNo
        order.setChainCode(code);
        order.setUserId(ctx.userId);
        order.setAmount(amount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        deepayOrderMapper.insert(order);

        ctx.orderId   = String.valueOf(order.getId());
        ctx.paymentId = outTradeNo;
        ctx.paid      = false;
        ctx.iban      = "DEEPAY-" + code;        // 向后兼容
        // 支付跳转链接（前端展示给用户）
        if (resp.isSuccess() && StringUtils.hasText(resp.getPayUrl())) {
            log.info("[OrderFlowAgent] 支付链接={}", resp.getPayUrl());
        }

        log.info("[OrderFlowAgent] 订单已创建 orderId={} paymentId={} amount={} payUrl={}",
                ctx.orderId, outTradeNo, amount,
                resp.isSuccess() ? resp.getPayUrl() : "FAILED:" + resp.getErrorMsg());
        return ctx;
    }
}

