package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.agent.AIDecisionAgent;
import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.InventoryAgent;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignImageMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.service.DeepayAuditService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 支付回调（Webhook）Controller — Phase 3/4 升级版。
 *
 * <pre>
 * POST /deepay/callback/payment  — 支付成功回调
 *   1. 幂等校验（DB 层 markPaid WHERE status='PENDING'）
 *   2. Redisson 分布式锁在 InventoryAgent.onPaid() 内部持有
 *   3. 扣减库存（原子 SQL，永不负库存）
 *   4. 计算并持久化 profit / roi（Phase 4）
 *   5. 更新 deepay_metrics（sold_count++, pay_count++, 重算 conversion_rate）
 *   6. 写审计日志
 *   7. AIDecisionAgent 二次决策
 *
 * POST /deepay/callback/refund   — 退款回调
 *   1. 幂等：ORDER 必须为 PAID 状态
 *   2. InventoryAgent.refund() 库存回滚
 *   3. 写审计日志
 * </pre>
 */
@Tag(name = "Deepay - 支付回调")
@RestController
@RequestMapping("/deepay/callback")
@Validated
public class DeepayPaymentCallbackController {

    private static final Logger log = LoggerFactory.getLogger(DeepayPaymentCallbackController.class);

    @Resource private DeepayOrderMapper   deepayOrderMapper;
    @Resource private DeepayProductMapper deepayProductMapper;
    @Resource private DeepayMetricsMapper deepayMetricsMapper;
    @Resource private DeepayDesignImageMapper deepayDesignImageMapper;
    @Resource private InventoryAgent      inventoryAgent;
    @Resource private AIDecisionAgent     aiDecisionAgent;
    @Resource private DeepayAuditService  auditService;

    // ====================================================================
    // 支付成功回调
    // ====================================================================

    @PostMapping("/payment")
    @Operation(summary = "支付成功回调")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Map<String, Object>> onPayment(@Valid @RequestBody PaymentCallbackReqVO req) {
        log.info("支付回调收到，paymentId={}", req.getPaymentId());

        // 1. 查订单
        DeepayOrderDO order = deepayOrderMapper.selectByPaymentId(req.getPaymentId());
        if (order == null) {
            return CommonResult.error(404, "订单不存在: " + req.getPaymentId());
        }

        // 2. 原子标记 PAID（WHERE status='PENDING'，幂等保证）
        int updated = deepayOrderMapper.markPaid(req.getPaymentId());
        if (updated == 0) {
            log.info("支付回调幂等：paymentId={} 已处理，直接返回", req.getPaymentId());
            return success(buildAlreadyPaidResp(order));
        }

        String chainCode = order.getChainCode();

        // 3. 扣减库存（Redisson 分布式锁 + 原子 SQL，在 InventoryAgent 内部持有）
        int remaining = inventoryAgent.onPaid(chainCode);

        // 4. Phase 4 — 查询商品，计算并持久化 profit / roi
        DeepayProductDO product = deepayProductMapper.selectByChainCode(chainCode);
        BigDecimal profit = null;
        BigDecimal roi    = null;
        if (product != null && product.getPrice() != null && product.getCostPrice() != null
                && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
            profit = product.getPrice().subtract(product.getCostPrice()).setScale(2, RoundingMode.HALF_UP);
            roi    = profit.divide(product.getCostPrice(), 4, RoundingMode.HALF_UP);
        }

        // 4b. STEP 28 — 对该商品关联的设计图递增 order_count，闭合权重学习回路
        if (product != null && product.getDesignId() != null) {
            try {
                cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignImageDO designImg =
                        deepayDesignImageMapper.selectById(product.getDesignId());
                if (designImg != null) {
                    deepayDesignImageMapper.incrementOrderCount(designImg.getUrl());
                }
            } catch (Exception ex) {
                log.warn("[PaymentCallback] order_count 更新失败 chainCode={}", chainCode, ex);
            }
        }

        // 5. 更新 deepay_metrics：sold_count++, pay_count++, 利润快照, conversion_rate
        updateMetricsOnPaid(chainCode, product, profit, roi);

        // 6. 写审计日志
        auditService.log(chainCode, "PAY",
                "status=PENDING amount=" + order.getAmount(),
                "status=PAID profit=" + profit + " roi=" + roi);

        // 7. AIDecisionAgent 二次决策
        Context reCtx = buildReviewContext(chainCode, product, roi);
        aiDecisionAgent.run(reCtx);
        log.info("二次决策完成 chainCode={} action={}", chainCode, reCtx.action);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode",      chainCode);
        resp.put("paymentId",      req.getPaymentId());
        resp.put("orderStatus",    "PAID");
        resp.put("remainingStock", remaining);
        resp.put("profit",         profit);
        resp.put("roi",            roi);
        resp.put("action",         reCtx.action);
        resp.put("decisionReason", reCtx.decisionReason);
        return success(resp);
    }

    // ====================================================================
    // 退款回调
    // ====================================================================

    @PostMapping("/refund")
    @Operation(summary = "退款回调：库存回滚")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Map<String, Object>> onRefund(@Valid @RequestBody PaymentCallbackReqVO req) {
        log.info("退款回调收到，paymentId={}", req.getPaymentId());

        DeepayOrderDO order = deepayOrderMapper.selectByPaymentId(req.getPaymentId());
        if (order == null) {
            return CommonResult.error(404, "订单不存在: " + req.getPaymentId());
        }

        // 幂等：只处理 PAID 状态订单
        if (!"PAID".equals(order.getStatus())) {
            return CommonResult.error(400, "订单状态不支持退款: " + order.getStatus());
        }

        // 取消订单
        deepayOrderMapper.markCancelled(req.getPaymentId());

        // 库存回滚（Redisson 锁在 InventoryAgent 内持有）
        inventoryAgent.refund(order.getChainCode());

        // 写审计日志
        auditService.log(order.getChainCode(), "REFUND",
                "status=PAID paymentId=" + req.getPaymentId(),
                "status=CANCELLED stock+1");

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("paymentId",   req.getPaymentId());
        resp.put("orderStatus", "CANCELLED");
        resp.put("chainCode",   order.getChainCode());
        return success(resp);
    }

    // ---------------------------------------------------------------- helpers

    private void updateMetricsOnPaid(String chainCode, DeepayProductDO product,
                                     BigDecimal profit, BigDecimal roi) {
        try {
            // 查最新 metrics 记录
            DeepayMetricsDO metrics = deepayMetricsMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeepayMetricsDO>()
                            .eq(DeepayMetricsDO::getChainCode, chainCode)
                            .orderByDesc(DeepayMetricsDO::getCreatedAt)
                            .last("LIMIT 1"));

            if (metrics != null) {
                int payCount  = (metrics.getPayCount()  != null ? metrics.getPayCount()  : 0) + 1;
                int viewCount = metrics.getViewCount()  != null ? metrics.getViewCount()  : 0;
                BigDecimal convRate = viewCount > 0
                        ? new BigDecimal(payCount).divide(new BigDecimal(viewCount), 4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                deepayMetricsMapper.update(null, new LambdaUpdateWrapper<DeepayMetricsDO>()
                        .eq(DeepayMetricsDO::getChainCode, chainCode)
                        .eq(DeepayMetricsDO::getId, metrics.getId())
                        .setSql("sold_count = sold_count + 1")
                        .set(DeepayMetricsDO::getPayCount,        payCount)
                        .set(DeepayMetricsDO::getConversionRate,  convRate)
                        .set(DeepayMetricsDO::getProfit,          profit)
                        .set(DeepayMetricsDO::getRoi,             roi));
            } else {
                // 无指标记录时新建快照（防御）
                DeepayMetricsDO snapshot = new DeepayMetricsDO();
                snapshot.setChainCode(chainCode);
                snapshot.setSoldCount(1);
                snapshot.setPayCount(1);
                snapshot.setViewCount(0);
                snapshot.setConversionRate(BigDecimal.ZERO);
                snapshot.setPrice(product != null ? product.getPrice() : null);
                snapshot.setCostPrice(product != null ? product.getCostPrice() : null);
                snapshot.setProfit(profit);
                snapshot.setRoi(roi);
                snapshot.setCreatedAt(LocalDateTime.now());
                deepayMetricsMapper.insert(snapshot);
            }
        } catch (Exception e) {
            log.warn("[PaymentCallback] 更新 metrics 失败 chainCode={}", chainCode, e);
        }
    }

    private Context buildReviewContext(String chainCode, DeepayProductDO product, BigDecimal roi) {
        Context ctx = new Context();
        ctx.chainCode = chainCode;
        if (product != null) {
            ctx.keyword   = product.getTitle();
            ctx.price     = product.getPrice();
            ctx.costPrice = product.getCostPrice();
            ctx.soldCount = product.getSoldCount();
            ctx.stock     = product.getStock();
            ctx.roi       = roi;

            int sold      = product.getSoldCount() != null ? product.getSoldCount() : 0;
            int threshold = resolveReviewThreshold(product.getTitle());
            String key    = chainCode + "-recheck";
            ctx.designImages = Collections.singletonList(key);
            ctx.imageScores  = Collections.singletonMap(key, sold > threshold ? 90 : 70);
        }
        return ctx;
    }

    private int resolveReviewThreshold(String keyword) {
        try {
            Double avg = deepayMetricsMapper.selectAvgSoldCountByCategory(keyword);
            if (avg != null && avg > 0) {
                return (int) Math.round(avg);
            }
        } catch (Exception e) {
            log.warn("resolveReviewThreshold: 查询历史均销量失败，使用默认阈值 5", e);
        }
        return 5;
    }

    private Map<String, Object> buildAlreadyPaidResp(DeepayOrderDO order) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("paymentId",   order.getPaymentId());
        resp.put("orderStatus", order.getStatus());
        resp.put("chainCode",   order.getChainCode());
        resp.put("idempotent",  true);
        return resp;
    }

    // ---- Request VO ----
    public static class PaymentCallbackReqVO {
        @NotBlank(message = "paymentId 不能为空")
        private String paymentId;

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String p) { this.paymentId = p; }
    }

}

