package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignGeneratedDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignGeneratedMapper;
import cn.iocoder.yudao.module.deepay.service.FluxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DesignGenAgent — Phase 9 原创设计生成。
 *
 * <p>把第8阶段确认的参考图（selectedImage）→ 生成"你自己的原创款"。</p>
 *
 * <h3>Prompt 模板</h3>
 * <pre>
 * 基于参考图生成原创服装设计：
 * 类目：{category}，风格：{style}，面料：{fabric}，市场：{market}。
 * 要求：1.保留核心轮廓 2.修改细节（口袋/领口/剪裁） 3.可量产 4.不包含品牌logo
 * </pre>
 *
 * <h3>去抄袭控制</h3>
 * <p>Prompt 中明确写入"不包含品牌logo"+"修改细节"+"原创"，
 * 结合 RiskControlAgent 的 logo 检测做双重保护。</p>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>ctx.designImages — 新生成的 3~5 张原创图（替换旧的参考图）</li>
 *   <li>ctx.designPrompt — 记录本次 prompt</li>
 *   <li>落库 deepay_design_generated</li>
 * </ul>
 */
@Component
public class DesignGenAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignGenAgent.class);
    private static final int GEN_COUNT = 4;

    @Resource private FluxService              fluxService;
    @Resource private DeepayDesignGeneratedMapper generatedMapper;

    @Override
    public Context run(Context ctx) {
        // Phase 9 入口：designConfirmed=true 且 shouldProduce=true
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[DesignGenAgent] shouldProduce=false，跳过");
            return ctx;
        }

        String prompt = buildPrompt(ctx);
        ctx.designPrompt = prompt;
        log.info("[DesignGenAgent] 生成原创设计 prompt={}", shorten(prompt));

        List<String> images = fluxService.generateImages(prompt, GEN_COUNT);
        ctx.designImages = images;   // 替换为原创图

        // 落库
        persist(ctx, images, prompt);

        log.info("[DesignGenAgent] 生成完成 count={} chainCode={}", images.size(), ctx.chainCode);
        return ctx;
    }

    // ----------------------------------------------------------------

    private String buildPrompt(Context ctx) {
        String category = def(ctx.category, "服装");
        String style    = def(ctx.style,    "休闲");
        String market   = marketLabel(def(ctx.market, "CN"));
        String fabric   = extractFabric(ctx.designFeatures, "棉/涤纶");

        return "基于参考图生成原创服装设计：" +
               "类目：" + category + "，" +
               "风格：" + style + "，" +
               "面料：" + fabric + "，" +
               "目标市场：" + market + "。" +
               "要求：" +
               "1.保留核心轮廓 " +
               "2.修改细节（口袋/领口/剪裁） " +
               "3.可量产 " +
               "4.不包含任何品牌logo " +
               "5.适合批发工厂生产";
    }

    /** 从 designFeatures JSON 提取 fabric 字段（简单字符串解析） */
    private String extractFabric(String featuresJson, String fallback) {
        if (!StringUtils.hasText(featuresJson)) return fallback;
        int idx = featuresJson.indexOf("\"fabric\":");
        if (idx < 0) return fallback;
        int start = featuresJson.indexOf('"', idx + 9) + 1;
        int end   = featuresJson.indexOf('"', start);
        if (start <= 0 || end <= start) return fallback;
        return featuresJson.substring(start, end);
    }

    private void persist(Context ctx, List<String> images, String prompt) {
        int version = 1;
        try {
            version = generatedMapper.countByChainCode(ctx.chainCode) + 1;
        } catch (Exception ignored) {}
        for (String url : images) {
            try {
                DeepayDesignGeneratedDO rec = new DeepayDesignGeneratedDO();
                rec.setChainCode(ctx.chainCode);
                rec.setImageUrl(url);
                rec.setPrompt(prompt.length() > 500 ? prompt.substring(0, 500) : prompt);
                rec.setVersion(version++);
                rec.setCreatedAt(LocalDateTime.now());
                generatedMapper.insert(rec);
            } catch (Exception e) {
                log.warn("[DesignGenAgent] 落库失败（不影响流程）url={}", shorten(url), e);
            }
        }
    }

    private String marketLabel(String m) {
        switch (m.toUpperCase()) {
            case "EU": return "欧洲";
            case "US": return "北美";
            case "ME": return "中东";
            default:   return "国内";
        }
    }
    private String def(String v, String d) { return StringUtils.hasText(v) ? v : d; }
    private static String shorten(String s) {
        return s == null ? "null" : (s.length() > 80 ? s.substring(0, 80) + "…" : s);
    }
}
