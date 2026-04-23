package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayVariantDO;
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
 * DesignVariantAgent — 一款 → 多爆款变体（Phase 8/9 STEP 13）。
 *
 * <p>基于 {@link Context#finalPrompt} 生成5类变体（颜色/面料/风格），
 * 每类2张图，共10张写入 {@link Context#variantImages}；
 * 同时将结构化变体信息写入 {@link Context#variants}。</p>
 */
@Component
public class DesignVariantAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignVariantAgent.class);

    private static final String[] COLORS  = {"黑色", "白色", "灰色", "米白", "卡其"};
    private static final String[] FABRICS = {"棉",   "牛仔", "针织", "羊毛", "丝绸"};

    // STEP 13 variant prompts
    private static final String[] VARIANT_SUFFIXES = {
        ", different color variations",
        ", premium fabric version",
        ", streetwear version",
        ", minimalist clean version",
        ", luxury high-end version"
    };

    @Resource
    private FluxService fluxService;

    @Override
    public Context run(Context ctx) {
        try {
            String basePrompt = StringUtils.hasText(ctx.finalPrompt) ? ctx.finalPrompt : ctx.stylePrompt;
            if (!StringUtils.hasText(basePrompt)) {
                log.info("[DesignVariantAgent] 无 finalPrompt/stylePrompt，跳过变体生成");
                return ctx;
            }

            String chainCode = ctx.chainCode != null ? ctx.chainCode : "UNKNOWN";
            List<String> variantImageUrls = new ArrayList<>();
            List<DeepayVariantDO> variants = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                String color  = COLORS[i];
                String fabric = FABRICS[i];
                // Use STEP 13 prompt suffix + color/fabric detail
                String variantPrompt = basePrompt + VARIANT_SUFFIXES[i] + ", " + color + ", " + fabric + " fabric";

                // Generate 2 images per variant (STEP 13: 5 variants × 2 = 10 images)
                List<String> generated = fluxService.generateImages(variantPrompt, 2);
                if (generated != null) {
                    variantImageUrls.addAll(generated);
                }

                String imageUrl = (generated != null && !generated.isEmpty()) ? generated.get(0) : "";

                DeepayVariantDO variant = new DeepayVariantDO();
                variant.setParentChainCode(chainCode);
                variant.setVariantCode(chainCode + "-V" + String.format("%03d", i + 1));
                variant.setCategory(ctx.category);
                variant.setColor(color);
                variant.setFabric(fabric);
                variant.setStyle(ctx.style);
                variant.setImageUrl(imageUrl);
                variant.setDesignPrompt(variantPrompt);
                variant.setCreatedAt(LocalDateTime.now());
                variants.add(variant);

                log.info("[DesignVariantAgent] 变体[{}] color={} fabric={} images={}", i + 1, color, fabric, generated != null ? generated.size() : 0);
            }

            ctx.variantImages = variantImageUrls;
            ctx.variants = variants;
            log.info("[DesignVariantAgent] DONE variantImages={} variants={} chainCode={}", variantImageUrls.size(), variants.size(), chainCode);
        } catch (Exception e) {
            log.warn("[DesignVariantAgent] 变体生成异常，跳过", e);
        }
        return ctx;
    }
}
