package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayPaymentLogDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayPaymentLogMapper;
import cn.iocoder.yudao.module.deepay.service.PaymentServiceV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * PaymentReconcileAgent — Phase 10 对账 + 防错。
 *
 * <p>在支付回调到达后、更新订单前调用。三道防线：</p>
 * <ol>
 *   <li><b>签名验证</b>：通过 {@link PaymentService#verifyCallback} 验证来源合法性</li>
 *   <li><b>金额校验</b>：回调金额 == 订单金额（允许 ±1分误差）</li>
 *   <li><b>幂等控制</b>：paymentId 已处理过，直接跳过</li>
 * </ol>
 *
 * <h3>使用方式</h3>
 * <pre>
 * PaymentReconcileAgent.ReconcileResult result =
 *     reconcileAgent.reconcile(params, order, rawBody);
 * if (!result.pass) return error(result.reason);
 * </pre>
 */
@Component
public class PaymentReconcileAgent {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileAgent.class);

    /** 允许的金额误差（分），Jeepay 可能有1分精度差 */
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.01");

    @Resource private PaymentServiceV2    paymentService;
    @Resource private DeepayOrderMapper    orderMapper;
    @Resource private DeepayPaymentLogMapper paymentLogMapper;

    /**
     * 执行对账校验。
     *
     * @param callbackParams 回调原始参数（含 sign）
     * @param order          已查到的订单（null → 订单不存在）
     * @param rawBody        回调原始报文（用于落日志）
     * @return {@link ReconcileResult}
     */
    public ReconcileResult reconcile(Map<String, String> callbackParams,
                                     DeepayOrderDO order,
                                     String rawBody) {

        String paymentId = callbackParams.getOrDefault("paymentId",
                callbackParams.getOrDefault("payOrderId", ""));

        // ── 1. 签名验证 ───────────────────────────────────────────
        if (!paymentService.verifyCallback(callbackParams)) {
            logCallback(paymentId, order, "SIGN_FAIL", rawBody);
            return ReconcileResult.fail("签名验证失败");
        }

        // ── 2. 订单存在性 ──────────────────────────────────────────
        if (order == null) {
            logCallback(paymentId, null, "ORDER_NOT_FOUND", rawBody);
            return ReconcileResult.fail("订单不存在: " + paymentId);
        }

        // ── 3. 幂等控制（PAID → 已处理）────────────────────────────
        if ("PAID".equals(order.getStatus())) {
            log.info("[PaymentReconcile] 幂等：paymentId={} 已处理", paymentId);
            return ReconcileResult.idempotent();
        }

        // ── 4. 金额校验 ────────────────────────────────────────────
        BigDecimal paidAmount = paymentService.extractPaidAmount(callbackParams);
        if (order.getAmount() != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = order.getAmount().subtract(paidAmount).abs();
            if (diff.compareTo(AMOUNT_TOLERANCE) > 0) {
                String reason = String.format("金额不符：期望=%.2f 实收=%.2f",
                        order.getAmount(), paidAmount);
                logCallback(paymentId, order, "AMOUNT_MISMATCH", rawBody);
                return ReconcileResult.fail(reason);
            }
        }

        // ── 5. 库存保护 ────────────────────────────────────────────
        // （由 InventoryAgent.onPaid 内部原子 SQL 处理，此处仅记录）

        logCallback(paymentId, order, "PASS", rawBody);
        return ReconcileResult.pass(paidAmount);
    }

    // ----------------------------------------------------------------

    private void logCallback(String paymentId, DeepayOrderDO order,
                              String status, String rawBody) {
        try {
            DeepayPaymentLogDO rec = new DeepayPaymentLogDO();
            rec.setPaymentId(StringUtils.hasText(paymentId) ? paymentId : "UNKNOWN");
            rec.setOrderId(order != null ? String.valueOf(order.getId()) : null);
            rec.setStatus(status);
            rec.setAmount(order != null ? order.getAmount() : null);
            rec.setCallbackRaw(rawBody != null && rawBody.length() > 2000
                    ? rawBody.substring(0, 2000) : rawBody);
            rec.setCreatedAt(LocalDateTime.now());
            paymentLogMapper.insert(rec);
        } catch (Exception e) {
            log.warn("[PaymentReconcile] 落库日志失败（不影响主流程）paymentId={}", paymentId, e);
        }
    }

    // ====================================================================
    // Result VO
    // ====================================================================

    public static class ReconcileResult {
        public final boolean   pass;
        public final boolean   idempotent;
        public final String    reason;
        public final BigDecimal paidAmount;

        private ReconcileResult(boolean pass, boolean idempotent, String reason, BigDecimal paid) {
            this.pass       = pass;
            this.idempotent = idempotent;
            this.reason     = reason;
            this.paidAmount = paid;
        }

        public static ReconcileResult pass(BigDecimal paid)    { return new ReconcileResult(true,  false, null, paid); }
        public static ReconcileResult idempotent()             { return new ReconcileResult(true,  true,  "IDEMPOTENT", BigDecimal.ZERO); }
        public static ReconcileResult fail(String reason)      { return new ReconcileResult(false, false, reason, BigDecimal.ZERO); }
    }
}
