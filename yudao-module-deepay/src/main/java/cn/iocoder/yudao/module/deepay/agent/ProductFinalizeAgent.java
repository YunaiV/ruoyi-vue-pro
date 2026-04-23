package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ProductFinalizeAgent — Phase 10 商品定稿。
 *
 * <p>把 Phase 9 finalDesign + cost数据 + 客户画像 → 可售卖商品数据。</p>
 *
 * <h3>核心规则</h3>
 * <ul>
 *   <li>标题去AI味：不用"简约"/"百搭"/"时尚"这类词，直接说卖点</li>
 *   <li>自动组合卖点：市场 + 风格 + 品类（3段结构）</li>
 *   <li>描述：面料 + 版型 + 适用人群（不超2句）</li>
 * </ul>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>ctx.title — 人工味标题</li>
 *   <li>ctx.description — 卖点描述</li>
 *   <li>落库 deepay_product.title / description / cdnImageUrl / channel</li>
 * </ul>
 */
@Component
public class ProductFinalizeAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ProductFinalizeAgent.class);

    @Resource
    private DeepayProductMapper productMapper;

    // ---- 市场前缀（去AI味词库）--------------------------------------
    private static final Map<String, String> MARKET_PREFIX;
    private static final Map<String, String> CROWD_WORD;
    private static final Map<String, List<String>> STYLE_ADJECTIVE;

    static {
        Map<String, String> mp = new LinkedHashMap<>();
        mp.put("EU", "欧版"); mp.put("US", "美版"); mp.put("ME", "中东版"); mp.put("CN", "国内款");
        MARKET_PREFIX = Collections.unmodifiableMap(mp);

        Map<String, String> cw = new LinkedHashMap<>();
        cw.put("男装", "男款"); cw.put("少女", "女款"); cw.put("中老年", "熟龄款");
        cw.put("运动", "运动款"); cw.put("MALE", "男款"); cw.put("FEMALE", "女款");
        CROWD_WORD = Collections.unmodifiableMap(cw);

        Map<String, List<String>> sa = new LinkedHashMap<>();
        sa.put("工装", Arrays.asList("多口袋", "耐穿", "实用"));
        sa.put("极简", Arrays.asList("干净利落", "不挑人", "好搭配"));
        sa.put("性感", Arrays.asList("显身材", "曲线设计", "气质感"));
        sa.put("休闲", Arrays.asList("宽松舒适", "日常百搭", "好穿脱"));
        sa.put("街头", Arrays.asList("潮感", "个性设计", "街头氛围"));
        sa.put("轻奢", Arrays.asList("高质感", "精致细节", "档次感"));
        STYLE_ADJECTIVE = Collections.unmodifiableMap(sa);
    }

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[ProductFinalizeAgent] shouldProduce=false，跳过");
            return ctx;
        }

        String title = buildTitle(ctx);
        String desc  = buildDescription(ctx);

        ctx.title       = title;
        ctx.description = desc;

        // 落库：更新 deepay_product
        if (ctx.productId != null) {
            try {
                Long pid = Long.parseLong(ctx.productId);
                productMapper.updateTitleAndDescription(pid, title, desc);
                if (StringUtils.hasText(ctx.finalDesign)) {
                    productMapper.updateCdnImageUrl(pid, ctx.finalDesign);
                }
                if (StringUtils.hasText(ctx.productChannel)) {
                    productMapper.updateChannel(pid, ctx.productChannel);
                }
            } catch (Exception e) {
                log.warn("[ProductFinalizeAgent] 落库失败（不影响流程）productId={}", ctx.productId, e);
            }
        } else {
            // 没有 productId 说明 ProductDraftAgent 还没跑，新建一条记录
            createDraft(ctx, title, desc);
        }

        log.info("[ProductFinalizeAgent] 商品定稿完成 title={} chainCode={}", title, ctx.chainCode);
        return ctx;
    }

    // ----------------------------------------------------------------

    private String buildTitle(Context ctx) {
        String market = MARKET_PREFIX.getOrDefault(
                ctx.market != null ? ctx.market.toUpperCase() : "CN", "");
        String crowd  = CROWD_WORD.getOrDefault(ctx.crowd, "");
        String cat    = StringUtils.hasText(ctx.category) ? ctx.category : "服装";
        String adj    = pickAdjective(ctx.style);

        // 格式：[市场前缀][人群][风格描述][品类]
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(market)) sb.append(market);
        if (StringUtils.hasText(crowd))  sb.append(crowd);
        if (StringUtils.hasText(adj))    sb.append(adj);
        sb.append(cat);
        return sb.toString();
    }

    private String buildDescription(Context ctx) {
        String fabric = extractFabric(ctx.designFeatures);
        String fit    = extractFit(ctx.designFeatures);
        String market = marketDesc(ctx.market);
        String style  = StringUtils.hasText(ctx.style) ? ctx.style + "风格" : "";

        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(fabric)) sb.append(fabric).append("材质");
        if (StringUtils.hasText(fit))    { if (sb.length() > 0) sb.append("，"); sb.append(fit).append("版型"); }
        if (StringUtils.hasText(style))  { if (sb.length() > 0) sb.append("，"); sb.append(style); }
        sb.append("。");
        if (StringUtils.hasText(market)) sb.append("适合").append(market).append("市场，爆款潜力。");
        return sb.toString();
    }

    private String pickAdjective(String style) {
        if (!StringUtils.hasText(style)) return "";
        List<String> adjs = STYLE_ADJECTIVE.get(style);
        if (adjs == null || adjs.isEmpty()) return style;
        return adjs.get(0);      // 取第一个最直接的卖点词
    }

    private String extractFabric(String featuresJson) {
        return extractField(featuresJson, "fabric");
    }

    private String extractFit(String featuresJson) {
        return extractField(featuresJson, "structure");
    }

    private String extractField(String json, String key) {
        if (!StringUtils.hasText(json)) return "";
        int idx = json.indexOf("\"" + key + "\":");
        if (idx < 0) return "";
        int start = json.indexOf('"', idx + key.length() + 3) + 1;
        int end   = json.indexOf('"', start);
        if (start <= 0 || end <= start) return "";
        return json.substring(start, end);
    }

    private String marketDesc(String market) {
        if (market == null) return "国内";
        switch (market.toUpperCase()) {
            case "EU": return "欧美"; case "US": return "北美"; case "ME": return "中东";
            default:   return "国内";
        }
    }

    private void createDraft(Context ctx, String title, String desc) {
        try {
            DeepayProductDO p = new DeepayProductDO();
            p.setChainCode(ctx.chainCode);
            p.setTitle(title);
            p.setDescription(desc);
            p.setCdnImageUrl(StringUtils.hasText(ctx.finalDesign) ? ctx.finalDesign : ctx.selectedImage);
            p.setCategory(ctx.category);
            p.setStatus("DRAFT");
            p.setStock(0);
            p.setSoldCount(0);
            p.setChannel(ctx.productChannel != null ? ctx.productChannel : "H5");
            p.setCreatedAt(LocalDateTime.now());
            productMapper.insert(p);
            ctx.productId = String.valueOf(p.getId());
        } catch (Exception e) {
            log.warn("[ProductFinalizeAgent] 新建草稿失败", e);
        }
    }
}
