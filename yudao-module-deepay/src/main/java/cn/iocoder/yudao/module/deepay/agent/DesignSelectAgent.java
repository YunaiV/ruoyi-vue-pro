package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * DesignSelectAgent — 设计图选择（Phase 8）。
 *
 * <h3>两种模式</h3>
 * <dl>
 *   <dt>🅰 人工选（HUMAN，默认）</dt>
 *   <dd>将过滤后的 designImages 写入 ctx.selectionImages，设置 pendingField="designSelection"，
 *       返回给前端让设计师 / 客户手动选择。收到回答（ctx.selectedImage）后继续流程。</dd>
 *   <dt>🅱 AI选（AI）</dt>
 *   <dd>利用 JudgeAgent 已打的 imageScores，自动选分数最高的图作为 selectedImage。
 *       适用于自动化流水线（无人干预）。</dd>
 * </dl>
 *
 * <h3>模式切换</h3>
 * <pre>
 *   ctx.designSelectMode = "HUMAN"  → 人工选
 *   ctx.designSelectMode = "AI"     → AI 自动选（默认，imageScores 已有时）
 *   未设置且 imageScores 有数据     → AI 自动选
 *   未设置且 imageScores 无数据     → HUMAN 模式
 * </pre>
 */
@Component
public class DesignSelectAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignSelectAgent.class);

    @Override
    public Context run(Context ctx) {
        // 如果 selectedImage 已由上游（AIDecisionAgent / 人工回答）填入，直接跳过
        if (StringUtils.hasText(ctx.selectedImage)) {
            log.info("[DesignSelectAgent] selectedImage 已就绪，跳过 selectedImage={}",
                    shorten(ctx.selectedImage));
            return ctx;
        }

        String mode = resolveMode(ctx);
        log.info("[DesignSelectAgent] 模式={} designImages={}", mode,
                ctx.designImages != null ? ctx.designImages.size() : 0);

        if ("HUMAN".equals(mode)) {
            return doHumanSelect(ctx);
        } else {
            return doAISelect(ctx);
        }
    }

    // ====================================================================
    // 人工选模式
    // ====================================================================

    private Context doHumanSelect(Context ctx) {
        // 把待选图写入 selectionImages（与选款引导层复用同一字段）
        if (ctx.designImages != null) {
            ctx.selectionImages = ctx.designImages;
        }
        // 暂停流程，等待前端回传 selectedImage
        ctx.pendingQuestion = "请从以下设计图中选择您最满意的一张（发送图片序号或 URL）";
        ctx.pendingField    = "selectedImage";
        log.info("[DesignSelectAgent] ⏸ 等待人工选图 selectionImages={}",
                ctx.selectionImages != null ? ctx.selectionImages.size() : 0);
        return ctx;
    }

    // ====================================================================
    // AI 自动选模式
    // ====================================================================

    private Context doAISelect(Context ctx) {
        if (ctx.imageScores == null || ctx.imageScores.isEmpty()) {
            // 无评分 → 取第一张
            if (ctx.designImages != null && !ctx.designImages.isEmpty()) {
                ctx.selectedImage = ctx.designImages.get(0);
                log.info("[DesignSelectAgent] AI 无评分，取第一张 selectedImage={}",
                        shorten(ctx.selectedImage));
            }
            return ctx;
        }

        // 按评分降序取最高分图
        String best = ctx.imageScores.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (StringUtils.hasText(best)) {
            ctx.selectedImage = best;
            log.info("[DesignSelectAgent] AI 选图完成 selectedImage={} score={}",
                    shorten(best), ctx.imageScores.get(best));
        } else if (ctx.designImages != null && !ctx.designImages.isEmpty()) {
            ctx.selectedImage = ctx.designImages.get(0);
            log.info("[DesignSelectAgent] AI 评分无有效图，兜底取第一张");
        }
        return ctx;
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private String resolveMode(Context ctx) {
        if (StringUtils.hasText(ctx.designSelectMode)) {
            return ctx.designSelectMode.toUpperCase();
        }
        // 有评分数据 → AI 模式；否则 → 人工
        return (ctx.imageScores != null && !ctx.imageScores.isEmpty()) ? "AI" : "HUMAN";
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        return s.length() > 60 ? s.substring(0, 60) + "..." : s;
    }

}
