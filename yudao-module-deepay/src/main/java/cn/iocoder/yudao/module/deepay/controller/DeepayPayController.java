package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.service.DeepayQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 配额购买支付接口（STEP 41-42）。
 *
 * <pre>
 * POST /api/pay/create           — 创建支付订单，返回 payUrl（前端跳转）
 * POST /api/pay/notify           — 支付成功 Webhook，入账 paid_quota
 * </pre>
 *
 * <p>Plan → 次数映射：
 * <ul>
 *   <li>PACK_S   → 10 次   €1.99</li>
 *   <li>PACK_M   → 30 次   €4.99  （推荐）</li>
 *   <li>PACK_L   → 100 次  €12.99</li>
 *   <li>DAY_PASS → 不限次  €2.99/天（今日限时）</li>
 * </ul>
 * </p>
 */
@Tag(name = "Deepay - 配额购买支付")
@RestController
@RequestMapping("/api/pay")
@Validated
public class DeepayPayController {

    private static final Logger log = LoggerFactory.getLogger(DeepayPayController.class);

    /** 支付网关回调验证密钥（生产环境从配置读取，此处为占位） */
    private static final String WEBHOOK_SECRET = "deepay-webhook-secret";

    @Resource
    private DeepayQuotaService quotaService;

    // ====================================================================
    // POST /api/pay/create — 创建支付，返回 payUrl
    // ====================================================================

    @PostMapping("/create")
    @Operation(summary = "创建配额购买支付订单，返回支付跳转 URL")
    public CommonResult<Map<String, Object>> create(@RequestBody CreatePayReqVO req) {
        DeepayQuotaService.PricingPlan plan;
        try {
            plan = DeepayQuotaService.PricingPlan.valueOf(req.getPlan().toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error",  "无效套餐 ID: " + req.getPlan());
            err.put("valid",  "PACK_S / PACK_M / PACK_L / DAY_PASS");
            return success(err);
        }

        // 生成支付单号（生产环境替换为 Jeepay / Stripe SDK 调用）
        String paymentId = "dp-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // TODO: 调用 Jeepay/Stripe 创建支付，获取真实 payUrl
        // 当前为占位实现：返回模拟 payUrl（参数已 URL 编码，防注入/解析错误）
        String encodedPlan;
        String encodedUserId;
        String encodedPrice;
        try {
            encodedPlan   = URLEncoder.encode(plan.name(),                                            "UTF-8");
            encodedUserId = URLEncoder.encode(req.getUserId() != null ? req.getUserId() : "",         "UTF-8");
            encodedPrice  = URLEncoder.encode(plan.priceEur,                                          "UTF-8");
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 encoding not supported", ex);
        }
        String payUrl = "https://pay.deepay.link/checkout?paymentId=" + paymentId
                + "&plan="    + encodedPlan
                + "&price="   + encodedPrice
                + "&userId="  + encodedUserId;

        log.info("[Pay] 创建支付 userId={} plan={} price={} paymentId={}",
                req.getUserId(), plan.name(), plan.priceEur, paymentId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("paymentId", paymentId);
        resp.put("plan",      plan.name());
        resp.put("quota",     plan.quota < 9999 ? plan.quota + "次" : "今日不限");
        resp.put("priceEur",  plan.priceEur);
        resp.put("payUrl",    payUrl);
        resp.put("desc",      plan.desc);
        return success(resp);
    }

    // ====================================================================
    // POST /api/pay/notify — 支付成功 Webhook（STEP 42）
    // ====================================================================

    @PostMapping("/notify")
    @Operation(summary = "支付成功 Webhook：入账 paid_quota，返回剩余次数")
    public CommonResult<Map<String, Object>> notify(@RequestBody PayNotifyReqVO req) {
        log.info("[Pay] 支付成功通知 userId={} plan={} paymentId={}",
                req.getUserId(), req.getPlan(), req.getPaymentId());

        // 入账配额（幂等由上游 paymentId 保证）
        quotaService.addPaidQuota(req.getUserId(), req.getPlan());

        Map<String, Object> quotaInfo = quotaService.getQuotaInfo(req.getUserId());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("msg",   "支付成功！快去生成爆款吧 🎉");
        resp.put("quota", quotaInfo);
        return success(resp);
    }

    // ====================================================================
    // Request VOs
    // ====================================================================

    public static class CreatePayReqVO {
        @NotBlank(message = "plan 不能为空")
        private String plan;
        private String userId;

        public String getPlan()    { return plan; }
        public void setPlan(String v)    { this.plan = v; }
        public String getUserId()  { return userId; }
        public void setUserId(String v)  { this.userId = v; }
    }

    public static class PayNotifyReqVO {
        @NotBlank(message = "userId 不能为空")
        private String userId;
        @NotBlank(message = "plan 不能为空")
        private String plan;
        private String paymentId;

        public String getUserId()    { return userId; }
        public void setUserId(String v)    { this.userId = v; }
        public String getPlan()      { return plan; }
        public void setPlan(String v)      { this.plan = v; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String v) { this.paymentId = v; }
    }
}
