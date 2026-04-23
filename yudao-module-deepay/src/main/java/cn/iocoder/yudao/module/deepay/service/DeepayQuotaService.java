package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserQuotaDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserQuotaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配额与定价服务（Monetization 核心）。
 *
 * <p>职责：
 * <ul>
 *   <li>初始化新用户免费额度（3 次）</li>
 *   <li>原子扣减额度（免费优先 → 付费）</li>
 *   <li>超限时返回友好 Upsell 消息而非硬拒绝</li>
 *   <li>付费充值入账</li>
 *   <li>返回剩余额度供前端展示</li>
 * </ul>
 * </p>
 *
 * <h3>定价体系</h3>
 * <pre>
 *   FREE   : 每日 3 次（拉新）
 *   PACK_S : +10 次  €1.99   — 低门槛首单
 *   PACK_M : +30 次  €4.99   — 性价比首选
 *   PACK_L : +100 次 €12.99  — 重度用户
 * </pre>
 */
@Slf4j
@Service
public class DeepayQuotaService {

    /** 新用户免费额度 */
    public static final int FREE_QUOTA_INIT = 3;

    /** 定价包 ID → {次数, 价格(欧元), 描述} */
    public enum PricingPlan {
        PACK_S  (10,   "1.99",  "再生成10款爆款只需 €1.99"),
        PACK_M  (30,   "4.99",  "30次畅享设计 ⭐推荐"),
        PACK_L  (100,  "12.99", "100次专业版，€12.99 省到爆"),
        DAY_PASS(9999, "2.99",  "今日不限次数，€2.99/天 限时");

        public final int    quota;
        public final String priceEur;
        public final String desc;

        PricingPlan(int quota, String priceEur, String desc) {
            this.quota    = quota;
            this.priceEur = priceEur;
            this.desc     = desc;
        }
    }

    @Resource
    private DeepayUserQuotaMapper quotaMapper;

    // ====================================================================
    // 配额查询
    // ====================================================================

    /**
     * 获取用户剩余配额信息，供前端展示"今日剩余 N 次"。
     * 用户不存在时自动初始化。
     */
    public Map<String, Object> getQuotaInfo(String userId) {
        if (!StringUtils.hasText(userId)) return buildAnonymousQuota();
        quotaMapper.initIfAbsent(userId);
        DeepayUserQuotaDO q = quotaMapper.selectByUserId(userId);
        return buildQuotaMap(q);
    }

    // ====================================================================
    // 配额消费（Controller 调用）
    // ====================================================================

    /**
     * 原子扣减配额：免费优先，不足时扣付费额度。
     *
     * <p>超限时 <strong>不抛异常</strong>，而是返回 Upsell 信息，
     * 前端可直接展示购买弹窗，转化率比硬报错翻倍。</p>
     *
     * @return {@code null} 表示扣减成功；非 null 表示超限，包含 upsell 消息
     */
    @Transactional(rollbackFor = Exception.class)
    public QuotaCheckResult checkAndConsume(String userId) {
        if (!StringUtils.hasText(userId)) {
            // 匿名用户：给 1 次体验机会，不扣库存
            return null;
        }

        quotaMapper.initIfAbsent(userId);
        int rows = quotaMapper.consumeQuota(userId);

        if (rows > 0) {
            log.info("[QuotaService] 扣减成功 userId={}", userId);
            return null; // 成功
        }

        // 超限：构建 Upsell 消息
        DeepayUserQuotaDO q = quotaMapper.selectByUserId(userId);
        log.warn("[QuotaService] 配额不足 userId={} free={} paid={}",
                userId, q != null ? q.getFreeQuota() : 0, q != null ? q.getPaidQuota() : 0);
        return QuotaCheckResult.upsell();
    }

    // ====================================================================
    // 付费充值
    // ====================================================================

    /**
     * 付费成功后入账配额（Jeepay 回调时调用）。
     *
     * @param userId 用户 ID
     * @param planId 套餐 ID（PACK_S / PACK_M / PACK_L）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPaidQuota(String userId, String planId) {
        PricingPlan plan = PricingPlan.valueOf(planId.toUpperCase());
        quotaMapper.initIfAbsent(userId);
        quotaMapper.addPaidQuota(userId, plan.quota);
        log.info("[QuotaService] 充值成功 userId={} plan={} quota+{}", userId, planId, plan.quota);
    }

    // ====================================================================
    // helpers
    // ====================================================================

    private Map<String, Object> buildQuotaMap(DeepayUserQuotaDO q) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (q == null) return buildAnonymousQuota();
        int remainFree = q.getFreeQuota() != null ? q.getFreeQuota() : 0;
        int remainPaid = q.getPaidQuota() != null ? q.getPaidQuota() : 0;
        map.put("remainFree",  remainFree);
        map.put("remainPaid",  remainPaid);
        map.put("totalRemain", remainFree + remainPaid);
        map.put("usedQuota",   q.getUsedQuota() != null ? q.getUsedQuota() : 0);
        map.put("plans",       buildPlans());
        return map;
    }

    private Map<String, Object> buildAnonymousQuota() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("remainFree",  1);
        map.put("remainPaid",  0);
        map.put("totalRemain", 1);
        map.put("usedQuota",   0);
        map.put("plans",       buildPlans());
        return map;
    }

    private Object buildPlans() {
        java.util.List<Map<String, Object>> plans = new java.util.ArrayList<>();
        for (PricingPlan p : PricingPlan.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",       p.name());
            item.put("quota",    p.quota);
            item.put("priceEur", p.priceEur);
            item.put("desc",     p.desc);
            plans.add(item);
        }
        return plans;
    }

    // ====================================================================
    // Quota check result
    // ====================================================================

    public static class QuotaCheckResult {
        public final boolean exceeded;
        public final String  message;
        public final String  upsellTitle;
        public final Object  plans;

        private QuotaCheckResult(boolean exceeded, String message, String upsellTitle, Object plans) {
            this.exceeded    = exceeded;
            this.message     = message;
            this.upsellTitle = upsellTitle;
            this.plans       = plans;
        }

        public static QuotaCheckResult upsell() {
            java.util.List<Map<String, Object>> plans = new java.util.ArrayList<>();
            for (PricingPlan p : PricingPlan.values()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id",       p.name());
                item.put("quota",    p.quota);
                item.put("priceEur", p.priceEur);
                item.put("desc",     p.desc);
                plans.add(item);
            }
            return new QuotaCheckResult(
                    true,
                    "今日免费次数已用完 — AI已分析1688/TikTok爆款趋势，再生成10款只需 €1.99",
                    "解锁更多爆款设计次数",
                    plans
            );
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("exceeded",    exceeded);
            m.put("message",     message);
            m.put("upsellTitle", upsellTitle);
            m.put("plans",       plans);
            m.put("code",        402);
            return m;
        }
    }
}
