package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.service.StyleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * StyleEngineAgent — 风格组合引擎（Phase 9 STEP 2）。
 *
 * <p>将客户的四个维度属性组合成可直接发送给 FLUX AI 的英文出图 Prompt：
 * <ul>
 *   <li>品类（category）：上衣 / 外套 / 裤子 / 裙子</li>
 *   <li>风格（stylePreference）：工装 / 极简 / 性感 / 运动</li>
 *   <li>市场（targetMarket）：欧美 / 中东 / 国内</li>
 *   <li>价格带（priceLevel）：低价 / 中端 / 高端</li>
 * </ul>
 * 结果写入 {@link Context#finalPrompt}，由 DesignGenAgent 直接消费。</p>
 *
 * <p>示例输出：
 * <pre>
 *   jacket, outerwear, workwear style, utility, functional pockets,
 *   european and american fashion trend, balanced quality and cost,
 *   high quality fashion design, clean background, professional clothing photography
 * </pre>
 * </p>
 *
 * <p>兜底策略：任意字段为空时使用通用描述，保证 finalPrompt 始终非空。</p>
 */
@Component
public class StyleEngineAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(StyleEngineAgent.class);

    /** 固定结尾修饰词，保证出图质量 */
    private static final String QUALITY_SUFFIX =
            ", high quality fashion design, clean background, professional clothing photography";

    @Resource
    private StyleEngine styleEngine;

    @Override
    public Context run(Context ctx) {
        log.info("[StyleEngineAgent] START category={} stylePreference={} targetMarket={} priceLevel={}",
                ctx.category, ctx.stylePreference, ctx.targetMarket, ctx.priceLevel);

        // 优先使用 stylePreference（中文），降级到 style（英文枚举）
        String styleInput = StringUtils.hasText(ctx.stylePreference)
                ? ctx.stylePreference
                : ctx.style;

        // 优先使用 targetMarket（中文），降级到 market（英文枚举）
        String marketInput = StringUtils.hasText(ctx.targetMarket)
                ? ctx.targetMarket
                : ctx.market;

        String prompt = buildPrompt(ctx.category, styleInput, marketInput, ctx.priceLevel);

        ctx.finalPrompt = prompt;

        // 同步写 stylePrompt 供其他 Agent 复用
        if (!StringUtils.hasText(ctx.stylePrompt)) {
            ctx.stylePrompt = mapStyle(styleInput);
        }

        log.info("[StyleEngineAgent] DONE finalPrompt={}", prompt);
        return ctx;
    }

    // ====================================================================
    // Prompt 组装
    // ====================================================================

    private String buildPrompt(String category, String style, String market, String price) {
        String categoryDesc = mapCategory(category);
        String styleDesc    = mapStyle(style);
        String marketDesc   = mapMarket(market);
        String priceDesc    = mapPrice(price);

        StringBuilder sb = new StringBuilder();
        sb.append(categoryDesc);

        if (StringUtils.hasText(styleDesc)) {
            sb.append(", ").append(styleDesc);
        }
        if (StringUtils.hasText(marketDesc)) {
            sb.append(", ").append(marketDesc);
        }
        if (StringUtils.hasText(priceDesc)) {
            sb.append(", ").append(priceDesc);
        }

        sb.append(QUALITY_SUFFIX);
        return sb.toString();
    }

    // ====================================================================
    // 映射表
    // ====================================================================

    private String mapCategory(String c) {
        if (!StringUtils.hasText(c)) return "fashion clothing";
        switch (c) {
            case "外套": case "大衣": return "jacket, outerwear";
            case "裤子":             return "pants, trousers";
            case "裙子": case "连衣裙": return "dress";
            case "上衣": case "T恤": return "top, shirt";
            case "内裤": case "内衣": return "underwear, lingerie";
            default:                return c + ", fashion clothing";
        }
    }

    private String mapStyle(String s) {
        if (!StringUtils.hasText(s)) return "modern style";
        switch (s) {
            case "工装":  return "workwear style, utility, functional pockets";
            case "极简":  case "MINIMAL": case "minimalist": return "minimalist, clean lines, neutral tones";
            case "性感":  case "SEXY":    return "sexy, body fit, elegant";
            case "运动":  case "SPORT":   return "sport style, activewear";
            case "轻奢":  case "LUXURY":  return "luxury style, premium fabric, high-end design";
            case "休闲":  case "CASUAL":  return "casual, relaxed fit, everyday wear";
            case "街头":               return "streetwear, urban style, bold graphics";
            default:                   return s + " style";
        }
    }

    private String mapMarket(String m) {
        if (!StringUtils.hasText(m)) return "";
        switch (m) {
            case "欧美": case "EU": case "US": return "european and american fashion trend";
            case "中东": case "ME":            return "modest fashion, middle east style";
            case "国内": case "CN":            return "china fashion trend";
            default:                           return m + " market trend";
        }
    }

    private String mapPrice(String p) {
        if (!StringUtils.hasText(p)) return "";
        switch (p) {
            case "低价": case "LOW":  return "mass production, low cost fabric";
            case "中端": case "MID":  return "balanced quality and cost";
            case "高端": case "HIGH": return "premium fabric, luxury design";
            default:                  return "";
        }
    }

}
