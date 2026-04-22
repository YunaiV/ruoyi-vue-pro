package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * DesignConfirmAgent — 层2 设计图最终确认门控（Phase 8）。
 *
 * <h3>功能</h3>
 * <p>在 StyleConsistencyAgent + RiskControlAgent 通过之后、PatternAgent 之前，
 * 做一次最终确认门控：</p>
 * <ul>
 *   <li><b>HUMAN 模式</b>（默认）：设置 pendingQuestion 暂停流程，等待设计师 / 客户
 *       确认 selectedImage，收到确认后继续进入打版</li>
 *   <li><b>AUTO 模式</b>：ctx.selectedImage 已有且 shouldProduce=true → 直接通过，
 *       不打断流程（适合自动化测试 / 全自动流水线）</li>
 * </ul>
 *
 * <h3>模式切换</h3>
 * <pre>
 *   ctx.designSelectMode = "AI"    → AUTO 模式（不打断）
 *   ctx.designSelectMode = "HUMAN" → HUMAN 模式（暂停等确认）
 *   未设置且 selectedImage 已有    → AUTO 模式
 *   未设置且 selectedImage 为空    → HUMAN 模式
 * </pre>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>{@link Context#pendingQuestion} — 非 null 时流程暂停</li>
 *   <li>{@link Context#pendingField}    — "confirmDesign"</li>
 *   <li>{@link Context#designConfirmed} — true 表示已确认，可继续</li>
 * </ul>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 自动化流水线（designSelectMode=AI）→ 不打断，直接设 designConfirmed=true</li>
 *   <li>✔ 人工流水线 → pendingQuestion 返回给前端，收到确认后继续</li>
 *   <li>✔ selectedImage 为空且 designImages 非空 → 暂停请求选图</li>
 * </ul>
 */
@Component
public class DesignConfirmAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignConfirmAgent.class);

    @Override
    public Context run(Context ctx) {
        // 已确认过（二次进入）→ 直接跳过
        if (Boolean.TRUE.equals(ctx.designConfirmed)) {
            log.debug("[DesignConfirmAgent] 已确认，跳过");
            return ctx;
        }

        // shouldProduce=false → RiskControl 已拦截，不再追问
        if (Boolean.FALSE.equals(ctx.shouldProduce)) {
            log.debug("[DesignConfirmAgent] shouldProduce=false，跳过确认");
            return ctx;
        }

        String mode = resolveMode(ctx);
        log.info("[DesignConfirmAgent] 模式={} selectedImage={} chainCode={}",
                mode, StringUtils.hasText(ctx.selectedImage) ? "✓" : "✗", ctx.chainCode);

        if ("AUTO".equals(mode)) {
            return doAutoConfirm(ctx);
        } else {
            return doHumanConfirm(ctx);
        }
    }

    // ====================================================================
    // AUTO 模式
    // ====================================================================

    private Context doAutoConfirm(Context ctx) {
        // AUTO 模式：selectedImage 已有 → 直接通过
        if (StringUtils.hasText(ctx.selectedImage)) {
            ctx.designConfirmed = true;
            ctx.shouldProduce   = true;
            log.info("[DesignConfirmAgent] AUTO 确认 selectedImage={}", shorten(ctx.selectedImage));
        } else if (ctx.designImages != null && !ctx.designImages.isEmpty()) {
            // 自动取第一张
            ctx.selectedImage   = ctx.designImages.get(0);
            ctx.designConfirmed = true;
            ctx.shouldProduce   = true;
            log.info("[DesignConfirmAgent] AUTO 自动选第一张 selectedImage={}", shorten(ctx.selectedImage));
        } else {
            // 无图 → RiskControl 应已拦截，这里再兜一次
            ctx.shouldProduce  = false;
            ctx.decisionReason = "[DesignConfirm] AUTO模式无可用设计图，终止";
            log.warn("[DesignConfirmAgent] AUTO 无图，终止 chainCode={}", ctx.chainCode);
        }
        return ctx;
    }

    // ====================================================================
    // HUMAN 模式
    // ====================================================================

    private Context doHumanConfirm(Context ctx) {
        if (StringUtils.hasText(ctx.selectedImage)) {
            // 已有 selectedImage（从上游填入）→ 确认通过
            ctx.designConfirmed = true;
            ctx.shouldProduce   = true;
            log.info("[DesignConfirmAgent] HUMAN 已有 selectedImage，确认通过");
        } else {
            // 暂停流程，等待设计师确认
            ctx.pendingQuestion = "请确认您满意的设计图（回传 selectedImage URL 即可继续生产）";
            ctx.pendingField    = "confirmDesign";
            log.info("[DesignConfirmAgent] ⏸ 等待人工确认 chainCode={} designImages={}",
                    ctx.chainCode, ctx.designImages != null ? ctx.designImages.size() : 0);
        }
        return ctx;
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private String resolveMode(Context ctx) {
        if (StringUtils.hasText(ctx.designSelectMode)) {
            return "AI".equalsIgnoreCase(ctx.designSelectMode) ? "AUTO" : "HUMAN";
        }
        // 有 selectedImage → AUTO；否则 → HUMAN
        return StringUtils.hasText(ctx.selectedImage) ? "AUTO" : "HUMAN";
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        return s.length() > 60 ? s.substring(0, 60) + "..." : s;
    }

}
