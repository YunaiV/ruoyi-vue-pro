package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.service.DeepayQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 配额与定价接口。
 *
 * <pre>
 * GET  /api/quota/info          — 查询剩余次数 + 定价套餐列表（前端展示"今日剩余 N 次"）
 * POST /api/quota/buy/callback  — 付费成功回调，入账 paid_quota（Jeepay/Stripe 调用）
 * GET  /api/quota/plans         — 仅返回定价套餐（购买弹窗用）
 * </pre>
 */
@Tag(name = "Deepay - 配额与定价")
@RestController
@RequestMapping("/api/quota")
@Validated
public class DeepayQuotaController {

    @Resource
    private DeepayQuotaService quotaService;

    // ====================================================================
    // GET /api/quota/info
    // ====================================================================

    @GetMapping("/info")
    @Operation(summary = "查询用户剩余配额 + 定价套餐（前端展示用）")
    public CommonResult<Map<String, Object>> info(
            @RequestParam(value = "userId", required = false) String userId) {
        Map<String, Object> quotaInfo = quotaService.getQuotaInfo(userId);
        return success(quotaInfo);
    }

    // ====================================================================
    // GET /api/quota/plans
    // ====================================================================

    @GetMapping("/plans")
    @Operation(summary = "返回定价套餐列表")
    public CommonResult<Object> plans() {
        // 复用 getQuotaInfo 里的 plans 字段
        Object plans = quotaService.getQuotaInfo(null).get("plans");
        return success(plans);
    }

    // ====================================================================
    // POST /api/quota/buy/callback — 付费成功回调
    // ====================================================================

    /**
     * 付费成功后由支付网关（Jeepay / Stripe Webhook）调用，入账 paid_quota。
     *
     * <p>幂等性由上游支付系统保证（同一 paymentId 只回调一次）。</p>
     *
     * @param req {@code userId}、{@code planId}（PACK_S/PACK_M/PACK_L）、{@code paymentId}
     */
    @PostMapping("/buy/callback")
    @Operation(summary = "付费成功回调：入账 paid_quota")
    public CommonResult<Map<String, Object>> buyCallback(@RequestBody BuyCallbackReqVO req) {
        quotaService.addPaidQuota(req.getUserId(), req.getPlanId());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("userId",    req.getUserId());
        resp.put("planId",    req.getPlanId());
        resp.put("quota",     quotaService.getQuotaInfo(req.getUserId()));
        resp.put("message",   "充值成功，快去生成爆款吧！");
        return success(resp);
    }

    // ====================================================================
    // Request VO
    // ====================================================================

    public static class BuyCallbackReqVO {
        @NotBlank(message = "userId 不能为空")
        private String userId;
        @NotBlank(message = "planId 不能为空")
        private String planId;
        private String paymentId;

        public String getUserId()    { return userId; }
        public void setUserId(String v)    { this.userId = v; }
        public String getPlanId()    { return planId; }
        public void setPlanId(String v)    { this.planId = v; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String v) { this.paymentId = v; }
    }

}
