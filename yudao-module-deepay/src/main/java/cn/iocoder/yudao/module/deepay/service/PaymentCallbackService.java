package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.AIDecisionAgent;
import cn.iocoder.yudao.module.deepay.agent.AnalyticsAgent;
import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.InventoryAgent;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayPaymentLogDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayPaymentLogMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentPlugin;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * PaymentCallbackService — 支付回调业务处理（核心事务层）。
 *
 * <p>Controller 只做：验签 → 转发到此服务。所有支付业务逻辑统一在此处理。</p>
 *
 * <h3>标准流程（原子事务）</h3>
 * <pre>
 * 1. 验签（由 Controller 完成）
 * 2. 查订单（by outTradeNo → payment_id）
 * 3. 幂等：order.status == PAID → 直接返回
 * 4. 金额校验：paidAmount != order.amount → 拒绝
 * 5. 原子更新 order.status = PAID（WHERE status='PENDING'）
 * 6. 扣库存（原子SQL，永不负库存）+ sold_count++
 * 7. 更新 deepay_metrics
 * 8. 写 deepay_payment_log
 * 9. 触发 AIDecisionAgent（BOOST/STOP/REDESIGN）
 * 10. 触发 AnalyticsAgent（数据回流）
 * </pre>
 */
@Service
public class PaymentCallbackService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackService.class);

    /** 允许金额误差（0.01元，应对分/元精度差） */
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.01");

    @Resource private DeepayOrderMapper       orderMapper;
    @Resource private DeepayProductMapper     productMapper;
    @Resource private DeepayMetricsMapper     metricsMapper;
    @Resource private DeepayPaymentLogMapper  paymentLogMapper;
    @Resource private InventoryAgent          inventoryAgent;
    @Resource private AIDecisionAgent         aiDecisionAgent;
    @Resource private AnalyticsAgent          analyticsAgent;
    @Resource private DeepayAuditService      auditService;

    // ====================================================================
    // handle — 支付成功回调（主入口）
    // ====================================================================

    /**
     * 处理支付成功回调（@Transactional 保证原子性）。
     *
     * @param data   回调参数（已通过验签）
     * @param status 已解析的支付状态
     * @param rawBody 原始报文（用于日志）
     * @param paymentServiceV2 已注入的 PaymentServiceV2（提取字段用）
     * @return 处理结果描述
     */
    @Transactional(rollbackFor = Exception.class)
    public CallbackResult handle(Map<String, String> data,
                                  PaymentPlugin.PaymentStatus status,
                                  String rawBody,
                                  PaymentServiceV2 paymentServiceV2) {

        // ── 只处理 PAID 状态 ─────────────────────────────────────
        if (status != PaymentPlugin.PaymentStatus.PAID) {
            log.info("[PaymentCallbackService] 状态非PAID，跳过 state={}", status);
            return CallbackResult.skip("status=" + status);
        }

        // ── 1. 提取我方订单号（outTradeNo = payment_id）─────────
        String outTradeNo = paymentServiceV2.extractOutTradeNo(data);
        if (outTradeNo == null) {
            writeLog(null, null, "MISSING_TRADE_NO", BigDecimal.ZERO, rawBody);
            return CallbackResult.fail("缺少 outTradeNo");
        }

        // ── 2. 查订单 ──────────────────────────────────────────────
        DeepayOrderDO order = orderMapper.selectByPaymentId(outTradeNo);
        if (order == null) {
            writeLog(outTradeNo, null, "ORDER_NOT_FOUND", BigDecimal.ZERO, rawBody);
            return CallbackResult.fail("订单不存在: " + outTradeNo);
        }

        // ── 3. 幂等（PAID → 已处理）────────────────────────────────
        if ("PAID".equals(order.getStatus())) {
            log.info("[PaymentCallbackService] 幂等：outTradeNo={} 已处理", outTradeNo);
            return CallbackResult.idempotent();
        }

        // ── 4. 金额校验（不信任回调金额）──────────────────────────
        BigDecimal paidAmount = paymentServiceV2.extractPaidAmount(data);
        if (order.getAmount() != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = order.getAmount().subtract(paidAmount).abs();
            if (diff.compareTo(AMOUNT_TOLERANCE) > 0) {
                String reason = String.format("金额不符：期望=%.2f 实收=%.2f",
                        order.getAmount(), paidAmount);
                writeLog(outTradeNo, order, "AMOUNT_MISMATCH", paidAmount, rawBody);
                log.warn("[PaymentCallbackService] ❌ {} outTradeNo={}", reason, outTradeNo);
                return CallbackResult.fail(reason);
            }
        }

        // ── 5. 原子标记 PAID（WHERE status='PENDING'，幂等保证）──
        int updated = orderMapper.markPaid(outTradeNo);
        if (updated == 0) {
            // 并发场景：另一个线程刚刚处理完
            log.info("[PaymentCallbackService] markPaid=0（并发幂等）outTradeNo={}", outTradeNo);
            return CallbackResult.idempotent();
        }

        String chainCode = order.getChainCode();

        // ── 6. 扣库存 + sold_count++（Redisson锁在InventoryAgent内）
        int remaining = inventoryAgent.onPaid(chainCode);

        // ── 7. 计算利润 + 更新 metrics ─────────────────────────────
        DeepayProductDO product = productMapper.selectByChainCode(chainCode);
        BigDecimal profit = calcProfit(product);
        BigDecimal roi    = calcRoi(product, profit);
        updateMetrics(chainCode, product, profit, roi);

        // ── 8. 写支付日志 ──────────────────────────────────────────
        writeLog(outTradeNo, order, "PAID", paidAmount, rawBody);

        // ── 9. 审计日志 ────────────────────────────────────────────
        auditService.log(chainCode, "PAY",
                "status=PENDING amount=" + order.getAmount(),
                "status=PAID profit=" + profit + " roi=" + roi);

        // ── 10. 数据回流（AIDecision + Analytics）─────────────────
        Context reCtx = buildReviewContext(chainCode, product, roi);
        try {
            reCtx = aiDecisionAgent.run(reCtx);
            reCtx = analyticsAgent.run(reCtx);
        } catch (Exception e) {
            log.warn("[PaymentCallbackService] 数据回流失败（不影响主流程）chainCode={}", chainCode, e);
        }

        log.info("[PaymentCallbackService] ✅ 支付完成 outTradeNo={} chainCode={} profit={} roi={} action={} remaining={}",
                outTradeNo, chainCode, profit, roi, reCtx.action, remaining);

        return CallbackResult.ok(chainCode, outTradeNo, profit, roi, reCtx.action, remaining);
    }

    // ====================================================================
    // 退款回调
    // ====================================================================

    @Transactional(rollbackFor = Exception.class)
    public CallbackResult handleRefund(String paymentId, String rawBody) {
        DeepayOrderDO order = orderMapper.selectByPaymentId(paymentId);
        if (order == null) return CallbackResult.fail("订单不存在: " + paymentId);
        if (!"PAID".equals(order.getStatus())) return CallbackResult.fail("非PAID状态，不能退款");

        orderMapper.markCancelled(paymentId);
        inventoryAgent.refund(order.getChainCode());
        writeLog(paymentId, order, "REFUND", order.getAmount(), rawBody);
        auditService.log(order.getChainCode(), "REFUND", "status=PAID", "status=CANCELLED");

        return CallbackResult.ok(order.getChainCode(), paymentId, null, null, "REFUND", -1);
    }

    // ====================================================================
    // helpers
    // ====================================================================

    private BigDecimal calcProfit(DeepayProductDO p) {
        if (p == null || p.getPrice() == null || p.getCostPrice() == null) return null;
        if (p.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) return null;
        return p.getPrice().subtract(p.getCostPrice()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcRoi(DeepayProductDO p, BigDecimal profit) {
        if (profit == null || p == null || p.getCostPrice() == null
                || p.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) return null;
        return profit.divide(p.getCostPrice(), 4, RoundingMode.HALF_UP);
    }

    private void updateMetrics(String chainCode, DeepayProductDO product,
                                BigDecimal profit, BigDecimal roi) {
        try {
            DeepayMetricsDO m = metricsMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeepayMetricsDO>()
                            .eq(DeepayMetricsDO::getChainCode, chainCode)
                            .orderByDesc(DeepayMetricsDO::getCreatedAt).last("LIMIT 1"));
            if (m != null) {
                int payCount  = (m.getPayCount() != null ? m.getPayCount() : 0) + 1;
                int viewCount = m.getViewCount() != null ? m.getViewCount() : 0;
                BigDecimal conv = viewCount > 0
                        ? new BigDecimal(payCount).divide(new BigDecimal(viewCount), 4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                metricsMapper.update(null, new LambdaUpdateWrapper<DeepayMetricsDO>()
                        .eq(DeepayMetricsDO::getId, m.getId())
                        .setSql("sold_count = sold_count + 1")
                        .set(DeepayMetricsDO::getPayCount, payCount)
                        .set(DeepayMetricsDO::getConversionRate, conv)
                        .set(DeepayMetricsDO::getProfit, profit)
                        .set(DeepayMetricsDO::getRoi, roi));
            } else {
                DeepayMetricsDO snap = new DeepayMetricsDO();
                snap.setChainCode(chainCode); snap.setSoldCount(1); snap.setPayCount(1);
                snap.setViewCount(0); snap.setConversionRate(BigDecimal.ZERO);
                snap.setPrice(product != null ? product.getPrice() : null);
                snap.setCostPrice(product != null ? product.getCostPrice() : null);
                snap.setProfit(profit); snap.setRoi(roi);
                snap.setCreatedAt(LocalDateTime.now());
                metricsMapper.insert(snap);
            }
        } catch (Exception e) {
            log.warn("[PaymentCallbackService] 更新metrics失败 chainCode={}", chainCode, e);
        }
    }

    private void writeLog(String paymentId, DeepayOrderDO order,
                           String status, BigDecimal amount, String rawBody) {
        try {
            DeepayPaymentLogDO rec = new DeepayPaymentLogDO();
            rec.setPaymentId(paymentId != null ? paymentId : "UNKNOWN");
            rec.setOrderId(order != null ? String.valueOf(order.getId()) : null);
            rec.setStatus(status);
            rec.setAmount(amount);
            rec.setCallbackRaw(rawBody != null && rawBody.length() > 2000
                    ? rawBody.substring(0, 2000) : rawBody);
            rec.setCreatedAt(LocalDateTime.now());
            paymentLogMapper.insert(rec);
        } catch (Exception e) {
            log.warn("[PaymentCallbackService] 写日志失败 paymentId={}", paymentId, e);
        }
    }

    private Context buildReviewContext(String chainCode, DeepayProductDO p, BigDecimal roi) {
        Context ctx = new Context();
        ctx.chainCode = chainCode;
        if (p != null) {
            ctx.keyword   = p.getTitle(); ctx.price = p.getPrice();
            ctx.costPrice = p.getCostPrice(); ctx.soldCount = p.getSoldCount();
            ctx.stock     = p.getStock(); ctx.roi = roi;
            String key    = chainCode + "-recheck";
            ctx.designImages = Collections.singletonList(key);
            int sold      = p.getSoldCount() != null ? p.getSoldCount() : 0;
            ctx.imageScores  = Collections.singletonMap(key, sold >= 10 ? 90 : 70);
        }
        return ctx;
    }

    // ====================================================================
    // Result VO
    // ====================================================================

    public static class CallbackResult {
        public final boolean    ok;
        public final boolean    idempotent;
        public final String     msg;
        public final String     chainCode;
        public final String     paymentId;
        public final BigDecimal profit;
        public final BigDecimal roi;
        public final String     action;
        public final int        remainingStock;

        private CallbackResult(boolean ok, boolean idempotent, String msg, String chainCode,
                                String paymentId, BigDecimal profit, BigDecimal roi,
                                String action, int remainingStock) {
            this.ok = ok; this.idempotent = idempotent; this.msg = msg;
            this.chainCode = chainCode; this.paymentId = paymentId;
            this.profit = profit; this.roi = roi; this.action = action;
            this.remainingStock = remainingStock;
        }

        public static CallbackResult ok(String cc, String pid, BigDecimal profit,
                                         BigDecimal roi, String action, int remaining) {
            return new CallbackResult(true, false, "ok", cc, pid, profit, roi, action, remaining);
        }
        public static CallbackResult idempotent() {
            return new CallbackResult(true, true, "IDEMPOTENT", null, null, null, null, null, -1);
        }
        public static CallbackResult fail(String reason) {
            return new CallbackResult(false, false, reason, null, null, null, null, null, -1);
        }
        public static CallbackResult skip(String reason) {
            return new CallbackResult(true, false, reason, null, null, null, null, null, -1);
        }
    }
}
