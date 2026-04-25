package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * CategoryFilterAgent — 品类过滤防错核心（Phase 6 任务 5）。
 *
 * <p>验收：客户做内裤 → 永远不出现外套图片。</p>
 *
 * <p>过滤规则：
 * <pre>
 *   for image in ctx.designImages:
 *       if image.embeddedCategory != ctx.category:
 *           discard(image)
 * </pre>
 * </p>
 *
 * <p>图片品类识别逻辑（无外部 AI 时）：
 * 从图片 URL 或占位符字符串中提取 category 标签。约定 DesignAgent 生成图片时，
 * 保底图 URL 格式为 {@code deepay://design/{category}/{uuid}}，
 * 真实图 URL 在调用 FluxService 时已将 category 嵌入 prompt，
 * 因此这里检查 URL 是否包含 {@code ctx.category} 子串作为过滤依据。
 * </p>
 *
 * <p>若 {@code ctx.category} 为空，跳过过滤（全放行）；
 * 若过滤后图片数量为 0，保留原始列表（防止出图为空导致流程卡死），并记录告警。</p>
 */
@Component
public class CategoryFilterAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(CategoryFilterAgent.class);

    @Override
    public Context run(Context ctx) {
        // 无 category 或无图片时跳过
        if (!StringUtils.hasText(ctx.category)
                || ctx.designImages == null
                || ctx.designImages.isEmpty()) {
            log.debug("[CategoryFilterAgent] 跳过过滤（category={} designImages={}）",
                    ctx.category, ctx.designImages == null ? 0 : ctx.designImages.size());
            return ctx;
        }

        String targetCategory = ctx.category.toLowerCase();
        List<String> passed   = new ArrayList<>();
        List<String> discarded = new ArrayList<>();

        for (String imageRef : ctx.designImages) {
            if (matchesCategory(imageRef, targetCategory)) {
                passed.add(imageRef);
            } else {
                discarded.add(imageRef);
            }
        }

        if (!discarded.isEmpty()) {
            log.info("[CategoryFilterAgent] 过滤品类不符的图片 discarded={} category={}",
                    discarded.size(), ctx.category);
        }

        if (passed.isEmpty()) {
            // 全部过滤后无图可用 → 告警但保留原始列表，后续由 JudgeAgent 低分处理
            log.warn("[CategoryFilterAgent] 过滤后无图片匹配品类={}，保留全部原始图片防止流程中断",
                    ctx.category);
        } else {
            ctx.designImages = passed;
            // 同步清理 imageScores（移除被过滤图片的评分）
            if (ctx.imageScores != null) {
                ctx.imageScores.keySet().retainAll(passed);
            }
        }

        log.info("[CategoryFilterAgent] 过滤完成 category={} passed={} discarded={}",
                ctx.category, passed.size(), discarded.size());
        return ctx;
    }

    // ====================================================================
    // 品类匹配逻辑
    // ====================================================================

    /**
     * 判断一张图片（URL / 占位符）是否属于目标品类。
     *
     * <p>匹配规则（按优先级）：
     * <ol>
     *   <li>图片引用包含目标品类子串（大小写不敏感）</li>
     *   <li>图片引用为默认保底图（URL 含 "default"）— 允许通过（保底图不含品类信息，不应被过滤）</li>
     *   <li>图片引用是 deepay://trend/ 前缀（趋势图，由 TrendSourceAgent 生成）— 已过品类筛选，放行</li>
     * </ol>
     * </p>
     */
    private boolean matchesCategory(String imageRef, String targetCategory) {
        if (!StringUtils.hasText(imageRef)) {
            return false;
        }
        String lower = imageRef.toLowerCase();

        // 规则 1：URL 直接含目标品类
        if (lower.contains(targetCategory)) {
            return true;
        }

        // 规则 2：保底图（default）— 允许通过
        if (lower.contains("default")) {
            return true;
        }

        // 规则 3：趋势图占位符 — 已经过 TrendSourceAgent 品类过滤
        if (lower.startsWith("deepay://trend/")) {
            return true;
        }

        // 不含品类信息的外部图 — 默认放行（避免误杀真实 AI 生成图）
        // 只有在图片引用中明确含有其他品类关键词时才丢弃
        // 常见品类黑名单（防止"外套"图出现在"内裤"场景）
        return !containsCompetingCategory(lower, targetCategory);
    }

    /**
     * 判断图片引用中是否包含与目标品类冲突的其他品类关键词。
     */
    private boolean containsCompetingCategory(String lower, String targetCategory) {
        // 简化版：常见一级品类列表，若图片 URL 含其中的非目标品类，视为冲突
        String[] knownCategories = {
                "外套", "coat", "jacket",
                "内裤", "underwear", "lingerie",
                "连衣裙", "dress",
                "t恤", "tshirt", "t-shirt",
                "裤子", "pants", "trousers",
                "衬衫", "shirt",
                "西装", "suit",
                "毛衣", "sweater", "knitwear",
                "羽绒服", "down jacket", "puffer"
        };

        for (String cat : knownCategories) {
            if (!cat.equals(targetCategory) && lower.contains(cat)) {
                return true;
            }
        }
        return false;
    }

}
