package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * StyleConsistencyAgent — 锁定风格 Prompt，防止后续 Agent 风格漂移（Phase 8）。
 *
 * <p>在 AIDecisionAgent 之后运行，将品类、风格偏好和市场信息固化为一个一致的
 * 英文 Prompt 字符串，确保后续 DesignGenAgent 使用统一的风格描述。</p>
 */
@Component
public class StyleConsistencyAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(StyleConsistencyAgent.class);

    @Override
    public Context run(Context ctx) {
        try {
            String categoryEn = mapCategoryToEn(ctx.category);
            String styleEn    = mapStyleToEn(ctx.stylePreference != null ? ctx.stylePreference : ctx.style);
            String marketEn   = mapMarketToEn(ctx.targetMarket != null ? ctx.targetMarket : ctx.market);

            String lockedPrompt = buildLockedPrompt(categoryEn, styleEn, marketEn);

            if (StringUtils.hasText(ctx.finalPrompt)) {
                ctx.finalPrompt = ctx.finalPrompt + ", consistent style, no logo, no brand";
            } else {
                ctx.finalPrompt = lockedPrompt;
            }

            // 同步更新 stylePrompt
            ctx.stylePrompt = categoryEn + ", " + styleEn + " style";

            log.info("[StyleConsistencyAgent] 风格已锁定 finalPrompt={}", ctx.finalPrompt);
        } catch (Exception e) {
            log.warn("[StyleConsistencyAgent] 风格锁定异常，跳过", e);
        }
        return ctx;
    }

    private String buildLockedPrompt(String categoryEn, String styleEn, String marketEn) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(categoryEn)) sb.append(categoryEn);
        if (StringUtils.hasText(styleEn))    sb.append(", ").append(styleEn);
        if (StringUtils.hasText(marketEn))   sb.append(", ").append(marketEn).append(" market");
        sb.append(", consistent style, no logo, no brand");
        return sb.toString();
    }

    private String mapCategoryToEn(String category) {
        if (!StringUtils.hasText(category)) return "clothing";
        switch (category) {
            case "外套":   return "outerwear";
            case "上衣":   return "top";
            case "裤子":   return "pants";
            case "内裤":   return "underwear";
            case "裙子":   return "skirt";
            case "连衣裙": return "dress";
            default:       return category;
        }
    }

    private String mapStyleToEn(String style) {
        if (!StringUtils.hasText(style)) return "casual";
        switch (style) {
            case "极简": case "minimalist": return "minimalist";
            case "工装":                   return "workwear";
            case "性感":                   return "sexy";
            case "运动":                   return "sporty";
            case "奢华": case "luxury":    return "luxury";
            case "休闲": case "casual":    return "casual";
            default:                       return style.toLowerCase();
        }
    }

    private String mapMarketToEn(String market) {
        if (!StringUtils.hasText(market)) return "";
        switch (market) {
            case "欧美": case "EU": case "US": return "European/American";
            case "中东": case "ME":            return "Middle East";
            case "国内": case "CN":            return "Chinese";
            default:                           return market;
        }
    }

}
