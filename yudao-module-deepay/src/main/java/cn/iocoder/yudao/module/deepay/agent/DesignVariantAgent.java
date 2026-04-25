package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignVariantDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignVariantMapper;
import cn.iocoder.yudao.module.deepay.service.FluxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DesignVariantAgent — Phase 9 多版本扩展，至少生成 3 个变体，自动选最高分。
 *
 * <h3>三个固定变体</h3>
 * <pre>
 * variant1 — 原版微调（保留主结构，改细节）
 * variant2 — 风格强化（向 style 极致发展）
 * variant3 — 简化版（去掉复杂结构，主打好卖）
 * </pre>
 *
 * <h3>评分（ScoreUtil.computeDesignScore）</h3>
 * <pre>
 * designScore = originality×0.3 + producibility×0.3 + costScore×0.2 + marketMatch×0.2
 *
 * originality  — 简化版最高（85），强化版中（75），微调最低（65）
 * producibility — 简化版最高（90），微调中（75），强化版低（60）
 * costScore    — 简化版最高（85），微调中（75），强化版低（65）
 * marketMatch  — 由 ctx.market 决定：EU/US→强化版加成，CN→简化版加成
 * </pre>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>ctx.designVariants — 3 个 DesignVariant（含分数）</li>
 *   <li>ctx.finalDesign    — 最高分变体的 imageUrl</li>
 *   <li>ctx.selectedImage  — 同 finalDesign（供后续 Agent 使用）</li>
 *   <li>落库 deepay_design_variant</li>
 * </ul>
 */
@Component
public class DesignVariantAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignVariantAgent.class);

    @Resource private FluxService            fluxService;
    @Resource private DeepayDesignVariantMapper variantMapper;

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[DesignVariantAgent] shouldProduce=false，跳过");
            return ctx;
        }

        List<DesignVariant> variants = new ArrayList<>();
        variants.add(generateVariant(ctx, "原版微调",   buildMicroTunePrompt(ctx),   65, 75, 75, marketMatchMicro(ctx)));
        variants.add(generateVariant(ctx, "风格强化",   buildStyleBoostPrompt(ctx),   75, 60, 65, marketMatchBoost(ctx)));
        variants.add(generateVariant(ctx, "简化版",     buildSimplifiedPrompt(ctx),   85, 90, 85, marketMatchSimple(ctx)));

        // 取最高分
        DesignVariant best = variants.stream()
                .max((a, b) -> Integer.compare(a.getScore(), b.getScore()))
                .orElse(variants.get(0));
        best.setSelected(true);

        ctx.designVariants = variants;
        ctx.finalDesign    = best.getImageUrl();
        ctx.selectedImage  = best.getImageUrl();   // 供 ProductDraftAgent 使用

        // 落库
        persistAll(ctx, variants);

        log.info("[DesignVariantAgent] 完成 best={} score={} chainCode={}",
                best.getStyleTag(), best.getScore(), ctx.chainCode);
        return ctx;
    }

    // ----------------------------------------------------------------
    // 变体生成
    // ----------------------------------------------------------------

    private DesignVariant generateVariant(Context ctx, String tag, String prompt,
                                           int originality, int producibility,
                                           int costScore,   int marketMatch) {
        int score = ScoreUtil.computeDesignScore(originality, producibility, costScore, marketMatch);
        List<String> imgs = fluxService.generateImages(prompt, 1);
        String url = (imgs != null && !imgs.isEmpty()) ? imgs.get(0) : fallbackUrl(ctx, tag);
        return new DesignVariant(url, tag, score, prompt);
    }

    // ----------------------------------------------------------------
    // Prompt 构建（三种变体方向）
    // ----------------------------------------------------------------

    private String buildMicroTunePrompt(Context ctx) {
        return "服装设计微调版：保留" + def(ctx.category,"服装") + "的主体结构和" +
               def(ctx.style,"基础") + "风格，仅修改口袋/纽扣/领口等细节，可量产，不含品牌logo";
    }

    private String buildStyleBoostPrompt(Context ctx) {
        return "服装设计风格强化版：" + def(ctx.category,"服装") + "，" +
               "将" + def(ctx.style,"休闲") + "风格推向极致，" +
               "突出" + marketStyleHint(ctx.market) + "审美，可量产，不含品牌logo";
    }

    private String buildSimplifiedPrompt(Context ctx) {
        return "服装设计简化版（主打畅销款）：" + def(ctx.category,"服装") + "，" +
               "去掉所有复杂结构，保留核心廓形，风格：" + def(ctx.style,"基础") + "，" +
               "成本优先，适合大批量生产，不含品牌logo";
    }

    // ----------------------------------------------------------------
    // 市场匹配分
    // ----------------------------------------------------------------

    private int marketMatchMicro(Context ctx)   { return 70; }
    private int marketMatchBoost(Context ctx) {
        return ("EU".equalsIgnoreCase(ctx.market) || "US".equalsIgnoreCase(ctx.market)) ? 85 : 65;
    }
    private int marketMatchSimple(Context ctx) {
        return ("CN".equalsIgnoreCase(ctx.market)) ? 90 : 70;
    }

    private String marketStyleHint(String market) {
        if (market == null) return "现代";
        switch (market.toUpperCase()) {
            case "EU": return "欧式简约";
            case "US": return "美式休闲运动";
            case "ME": return "轻奢优雅";
            default:   return "国内流行";
        }
    }

    // ----------------------------------------------------------------
    // 落库
    // ----------------------------------------------------------------

    private void persistAll(Context ctx, List<DesignVariant> variants) {
        for (DesignVariant v : variants) {
            try {
                DeepayDesignVariantDO rec = new DeepayDesignVariantDO();
                rec.setChainCode(ctx.chainCode);
                rec.setImageUrl(v.getImageUrl());
                rec.setStyleTag(v.getStyleTag());
                rec.setScore(v.getScore());
                rec.setSelected(v.isSelected() ? 1 : 0);
                rec.setCreatedAt(LocalDateTime.now());
                variantMapper.insert(rec);
            } catch (Exception e) {
                log.warn("[DesignVariantAgent] 落库失败（不影响流程）tag={}", v.getStyleTag(), e);
            }
        }
    }

    private String fallbackUrl(Context ctx, String tag) {
        return "/design/fallback/" + ctx.chainCode + "/" + tag.replaceAll("\\s+", "_") + ".jpg";
    }
    private String def(String v, String d) { return StringUtils.hasText(v) ? v : d; }
}
