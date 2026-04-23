package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.service.PaymentCallbackService;
import cn.iocoder.yudao.module.deepay.service.PaymentServiceV2;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentPlugin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 支付回调（Webhook）Controller — Phase 10 插件化版本。
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>Controller 只做两件事：① 验签 ② 转发给 PaymentCallbackService</li>
 *   <li>所有业务逻辑（幂等/库存/分析）统一在 PaymentCallbackService 处理</li>
 *   <li>支持 Jeepay 回调格式（含 sign, state, outTradeNo, amount 字段）</li>
 *   <li>支持旧版 VO 格式（paymentId）向后兼容</li>
 * </ul>
 *
 * <h3>Jeepay 回调格式</h3>
 * <pre>
 * POST /deepay/callback/payment
 * {
 *   "mchNo":      "M123",
 *   "appId":      "A123",
 *   "outTradeNo": "PAY-ABC123",   // = 我方 payment_id
 *   "amount":     1999,           // 分（€19.99）
 *   "currency":   "EUR",
 *   "state":      2,              // 1=WAIT 2=PAID 3=FAIL
 *   "sign":       "xxxx"
 * }
 * </pre>
 */
@Tag(name = "Deepay - 支付回调")
@RestController
@RequestMapping("/deepay/callback")
@Validated
public class DeepayPaymentCallbackController {

    private static final Logger log = LoggerFactory.getLogger(DeepayPaymentCallbackController.class);

    @Resource private PaymentServiceV2      paymentService;
    @Resource private PaymentCallbackService callbackService;

    // ====================================================================
    // Jeepay 标准回调（Map 参数）
    // ====================================================================

    /**
     * Jeepay 支付回调（主接口）。
     *
     * <p>Controller 只做：① 验签 → ② 解析状态 → ③ 转发 PaymentCallbackService</p>
     */
    @PostMapping("/payment")
    @Operation(summary = "支付回调（Jeepay标准格式）")
    public CommonResult<Map<String, Object>> onPayment(
            @RequestBody Map<String, String> data,
            @RequestHeader(value = "X-Raw-Body", required = false) String rawHeader) {

        log.info("[PaymentCallback] 收到回调 outTradeNo={} state={}",
                data.get("outTradeNo"), data.get("state"));

        // ── 1. 验签（Controller 唯一职责之一）─────────────────────
        if (!paymentService.verify(data)) {
            log.warn("[PaymentCallback] 签名验证失败，拒绝");
            return CommonResult.error(400, "签名验证失败");
        }

        // ── 2. 解析支付状态 ────────────────────────────────────────
        PaymentPlugin.PaymentStatus status = paymentService.parseStatus(data);
        log.info("[PaymentCallback] 状态解析 state={} → {}", data.get("state"), status);

        // ── 3. 转发业务层（Controller 唯一职责之二）──────────────
        String rawBody = buildRawBody(data);
        PaymentCallbackService.CallbackResult result =
                callbackService.handle(data, status, rawBody, paymentService);

        // ── 4. 构造响应 ────────────────────────────────────────────
        if (!result.ok) {
            log.warn("[PaymentCallback] 业务处理失败 reason={}", result.msg);
            return CommonResult.error(500, result.msg);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok",             true);
        resp.put("idempotent",     result.idempotent);
        resp.put("chainCode",      result.chainCode);
        resp.put("paymentId",      result.paymentId);
        resp.put("profit",         result.profit);
        resp.put("roi",            result.roi);
        resp.put("action",         result.action);
        resp.put("remainingStock", result.remainingStock);
        return success(resp);
    }

    // ====================================================================
    // 旧版 VO 格式（向后兼容）
    // ====================================================================

    @PostMapping("/payment/v1")
    @Operation(summary = "支付回调（旧版VO格式，向后兼容）")
    public CommonResult<Map<String, Object>> onPaymentV1(
            @RequestBody PaymentCallbackReqVO req) {

        log.info("[PaymentCallback-v1] paymentId={}", req.getPaymentId());

        // 转换为 Map 格式，state=2 直接标记 PAID
        Map<String, String> data = new java.util.HashMap<>();
        data.put("outTradeNo", req.getPaymentId());
        data.put("state",      "2");   // 旧接口默认 PAID
        data.put("amount",     req.getAmount() != null ? req.getAmount().toPlainString() : "0");

        // 旧接口跳过验签（内网调用）
        PaymentCallbackService.CallbackResult result =
                callbackService.handle(data, PaymentPlugin.PaymentStatus.PAID,
                        req.getPaymentId(), paymentService);

        if (!result.ok) return CommonResult.error(500, result.msg);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("paymentId",  result.paymentId);
        resp.put("chainCode",  result.chainCode);
        resp.put("action",     result.action);
        return success(resp);
    }

    // ====================================================================
    // 退款回调
    // ====================================================================

    @PostMapping("/refund")
    @Operation(summary = "退款回调")
    public CommonResult<Map<String, Object>> onRefund(
            @RequestBody Map<String, String> data) {

        log.info("[RefundCallback] outTradeNo={}", data.get("outTradeNo"));

        if (!paymentService.verify(data)) {
            return CommonResult.error(400, "签名验证失败");
        }

        String paymentId = paymentService.extractOutTradeNo(data);
        PaymentCallbackService.CallbackResult result =
                callbackService.handleRefund(paymentId, buildRawBody(data));

        if (!result.ok) return CommonResult.error(500, result.msg);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("paymentId",  result.paymentId);
        resp.put("chainCode",  result.chainCode);
        resp.put("status",     "CANCELLED");
        return success(resp);
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private String buildRawBody(Map<String, String> data) {
        try {
            StringBuilder sb = new StringBuilder("{");
            data.forEach((k, v) -> sb.append("\"").append(k).append("\":\"").append(v).append("\","));
            if (sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return data.toString();
        }
    }

    // ---- 向后兼容 VO ----
    public static class PaymentCallbackReqVO {
        private String paymentId;
        private java.math.BigDecimal amount;

        public String              getPaymentId() { return paymentId; }
        public void                setPaymentId(String v) { this.paymentId = v; }
        public java.math.BigDecimal getAmount()   { return amount; }
        public void                setAmount(java.math.BigDecimal v) { this.amount = v; }
    }
}
