package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * DesignSplitAgent — 将设计拆分为版型 / 面料 / 设计细节三个维度（Phase 8）。
 *
 * <p>基于品类与风格的规则映射，填充 {@link Context#patternType}、
 * {@link Context#fabric}、{@link Context#designDetails}，
 * 并将这些信息追加到 {@link Context#finalPrompt}。</p>
 */
@Component
public class DesignSplitAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignSplitAgent.class);

    @Override
    public Context run(Context ctx) {
        try {
            String category = ctx.category;
            String style    = ctx.stylePreference != null ? ctx.stylePreference : ctx.style;

            applyRules(ctx, category, style);

            // 追加到 finalPrompt
            if (StringUtils.hasText(ctx.finalPrompt)) {
                ctx.finalPrompt = ctx.finalPrompt
                        + ", " + ctx.patternType + " cut"
                        + ", " + ctx.fabric + " fabric"
                        + ", " + ctx.designDetails;
            }

            log.info("[DesignSplitAgent] patternType={} fabric={} designDetails={}",
                    ctx.patternType, ctx.fabric, ctx.designDetails);
        } catch (Exception e) {
            log.warn("[DesignSplitAgent] 设计拆分异常，跳过", e);
        }
        return ctx;
    }

    private void applyRules(Context ctx, String category, String style) {
        if ("外套".equals(category) && "工装".equals(style)) {
            ctx.patternType   = "oversize";
            ctx.fabric        = "棉";
            ctx.designDetails = "多口袋, 拉链";
        } else if ("外套".equals(category) && "极简".equals(style)) {
            ctx.patternType   = "straight";
            ctx.fabric        = "羊毛";
            ctx.designDetails = "极简剪裁";
        } else if ("裤子".equals(category)) {
            ctx.patternType   = "straight";
            ctx.fabric        = "棉";
            ctx.designDetails = "锥形裤腿";
        } else if (("裙子".equals(category) || "连衣裙".equals(category)) && "性感".equals(style)) {
            ctx.patternType   = "fitted";
            ctx.fabric        = "丝绸";
            ctx.designDetails = "开叉, 低领";
        } else {
            ctx.patternType   = "straight";
            ctx.fabric        = "棉";
            ctx.designDetails = "基础款";
        }
    }

}
