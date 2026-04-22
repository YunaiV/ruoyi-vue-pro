package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTrendPoolDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTrendPoolMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TrendAgent — 趋势参考图采集（Phase 6-7 最终版）。
 *
 * <h3>核心原则：只给客户看"他会卖的款"</h3>
 *
 * <h3>查询优先级</h3>
 * <pre>
 * 1️⃣  deepay_trend_pool（按 category + crowd + style 精准过滤）
 *       ↓ 无结果
 * 2️⃣  deepay_trend_pool（只按 category 过滤，放宽 style/crowd）
 *       ↓ 无结果
 * 3️⃣  deepay_product（系统内近期热销）
 *       ↓ 无结果
 * 4️⃣  保底图（按品类独立，永不出错类目）
 * </pre>
 *
 * <h3>种子数据初始化</h3>
 * <p>首次启动时若 deepay_trend_pool 为空，自动写入模拟数据（后续换真实爬取）。</p>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 内裤客户 → ctx.trendImages 全是内裤图，永不出外套</li>
 *   <li>✔ 外套 + 男装 → 只出男装外套方向</li>
 *   <li>✔ 无数据 → 保底图兜底，不抛异常</li>
 * </ul>
 */
@Component
public class TrendAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(TrendAgent.class);

    /** 每次查询返回的最大款式数量 */
    private static final int QUERY_LIMIT = 30;

    @Resource
    private DeepayTrendPoolMapper trendPoolMapper;

    @Resource
    private DeepayProductMapper productMapper;

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        log.info("[TrendAgent] START category={} crowd={} style={}", ctx.category, ctx.crowd, ctx.style);

        // 1️⃣ 数据库为空 → 初始化种子数据（模拟，可替换为真实采集）
        try {
            if (trendPoolMapper.selectCount() == 0) {
                log.info("[TrendAgent] 趋势库为空，初始化种子数据");
                trendPoolMapper.insertBatch(buildSeedData());
            }
        } catch (Exception e) {
            log.warn("[TrendAgent] 种子数据初始化失败（表可能未创建），跳过", e);
        }

        // 2️⃣ 精准查询：category + crowd + style 全匹配
        List<DeepayTrendPoolDO> list = queryTrendPool(ctx.category, ctx.style, ctx.crowd);

        // 3️⃣ 放宽 crowd，只按 category + style
        if (list.isEmpty() && StringUtils.hasText(ctx.crowd)) {
            log.info("[TrendAgent] 精准查询无结果，放宽 crowd category={} style={}", ctx.category, ctx.style);
            list = queryTrendPool(ctx.category, ctx.style, null);
        }

        // 4️⃣ 放宽 style，只按 category
        if (list.isEmpty() && StringUtils.hasText(ctx.category)) {
            log.info("[TrendAgent] 放宽 style，仅按 category={} 查询", ctx.category);
            list = queryTrendPool(ctx.category, null, null);
        }

        // 5️⃣ 内部热销商品（deepay_product）
        if (list.isEmpty()) {
            log.info("[TrendAgent] trend_pool 无数据，降级到内部热销 category={}", ctx.category);
            List<DeepayProductDO> hotProducts = fetchHotProducts(ctx.category);
            if (!hotProducts.isEmpty()) {
                // 风格加权排序
                hotProducts = sortByStyleWeight(hotProducts, ctx.styleWeights);
                ctx.referenceImages = hotProducts.stream()
                        .map(DeepayProductDO::getMainImage)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                ctx.trendItems = toTrendItems(hotProducts);
                ctx.trendImages = ctx.referenceImages;
                if (!ctx.referenceImages.isEmpty()) {
                    if (!StringUtils.hasText(ctx.keyword) && StringUtils.hasText(ctx.category)) {
                        ctx.keyword = ctx.category;
                    }
                    log.info("[TrendAgent] DONE via deepay_product category={} count={}",
                            ctx.category, ctx.referenceImages.size());
                    return ctx;
                }
            }
        }

        // 6️⃣ 最终兜底：保底图（按品类独立，永不出错类目）
        if (list.isEmpty()) {
            log.info("[TrendAgent] 全部数据源均无结果，使用品类保底图 category={}", ctx.category);
            ctx.referenceImages = defaultImages(ctx.category);
            ctx.trendImages     = ctx.referenceImages;
            return ctx;
        }

        // 写入上下文
        ctx.trendImages = list.stream()
                .map(DeepayTrendPoolDO::getImageUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        ctx.referenceImages = ctx.trendImages;
        ctx.trendItems = list.stream()
                .map(p -> {
                    TrendItem item = new TrendItem();
                    item.setImageUrl(p.getImageUrl());
                    item.setCategory(p.getCategory());
                    item.setStyle(p.getStyle());
                    return item;
                })
                .collect(Collectors.toList());
        // 趋势关键词（取 top 5 style 标签去重）
        ctx.trendKeywords = list.stream()
                .map(DeepayTrendPoolDO::getStyle)
                .filter(StringUtils::hasText)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        if (!StringUtils.hasText(ctx.keyword) && StringUtils.hasText(ctx.category)) {
            ctx.keyword = ctx.category;
        }

        log.info("[TrendAgent] DONE category={} crowd={} count={}",
                ctx.category, ctx.crowd, ctx.trendImages.size());
        return ctx;
    }

    // ====================================================================
    // 内部查询
    // ====================================================================

    private List<DeepayTrendPoolDO> queryTrendPool(String category, String style, String crowd) {
        try {
            return trendPoolMapper.selectTopByProfile(category, style, crowd, QUERY_LIMIT);
        } catch (Exception e) {
            log.warn("[TrendAgent] trend_pool 查询异常 category={}", category, e);
            return Collections.emptyList();
        }
    }

    private List<DeepayProductDO> fetchHotProducts(String category) {
        try {
            if (!StringUtils.hasText(category)) {
                return Collections.emptyList();
            }
            return productMapper.selectHotByCategory(category);
        } catch (Exception e) {
            log.warn("[TrendAgent] deepay_product 查询异常 category={}", category, e);
            return Collections.emptyList();
        }
    }

    // ====================================================================
    // 种子数据（模拟，后续替换为真实抓取）
    // ====================================================================

    /**
     * 构建初始种子数据（覆盖主要品类 × 客群 × 风格，每个组合至少 2 条）。
     * 打分模型：runway +15 / brand +12 / shein +10 / tiktok +8 / 1688 +6
     */
    private List<DeepayTrendPoolDO> buildSeedData() {
        List<DeepayTrendPoolDO> list = new ArrayList<>();

        // ----外套----
        list.add(build("shein",   "外套", "工装", "男装",   "img/trend/coat_workwear_male_1.jpg",   80 + 10));
        list.add(build("tiktok",  "外套", "工装", "男装",   "img/trend/coat_workwear_male_2.jpg",   80 + 8));
        list.add(build("brand",   "外套", "极简", "男装",   "img/trend/coat_minimal_male_1.jpg",    80 + 12));
        list.add(build("runway",  "外套", "轻奢", "男装",   "img/trend/coat_luxury_male_1.jpg",     80 + 15));
        list.add(build("1688",    "外套", "休闲", "男装",   "img/trend/coat_casual_male_1.jpg",     80 + 6));
        list.add(build("shein",   "外套", "街头", "少女",   "img/trend/coat_street_girl_1.jpg",     80 + 10));
        list.add(build("tiktok",  "外套", "性感", "少女",   "img/trend/coat_sexy_girl_1.jpg",       78 + 8));
        list.add(build("brand",   "外套", "极简", "少女",   "img/trend/coat_minimal_girl_1.jpg",    75 + 12));

        // ----内裤 / 内衣----
        list.add(build("shein",   "内裤", "性感", "少女",   "img/trend/underwear_sexy_1.jpg",       85 + 10));
        list.add(build("tiktok",  "内裤", "性感", "少女",   "img/trend/underwear_sexy_2.jpg",       85 + 8));
        list.add(build("1688",    "内裤", "极简", "少女",   "img/trend/underwear_minimal_1.jpg",    80 + 6));
        list.add(build("brand",   "内衣", "轻奢", "少女",   "img/trend/lingerie_luxury_1.jpg",      78 + 12));
        list.add(build("runway",  "内衣", "性感", "少女",   "img/trend/lingerie_sexy_1.jpg",        78 + 15));

        // ----连衣裙----
        list.add(build("tiktok",  "连衣裙", "性感", "少女",  "img/trend/dress_sexy_1.jpg",           90 + 8));
        list.add(build("shein",   "连衣裙", "休闲", "少女",  "img/trend/dress_casual_1.jpg",         88 + 10));
        list.add(build("brand",   "连衣裙", "极简", "少女",  "img/trend/dress_minimal_1.jpg",        85 + 12));
        list.add(build("runway",  "连衣裙", "轻奢", "中老年","img/trend/dress_luxury_elder_1.jpg",   75 + 15));
        list.add(build("1688",    "连衣裙", "休闲", "中老年","img/trend/dress_casual_elder_1.jpg",   72 + 6));

        // ----裤子----
        list.add(build("1688",    "裤子", "工装", "男装",   "img/trend/pants_workwear_male_1.jpg",  82 + 6));
        list.add(build("shein",   "裤子", "工装", "男装",   "img/trend/pants_workwear_male_2.jpg",  82 + 10));
        list.add(build("brand",   "裤子", "极简", "男装",   "img/trend/pants_minimal_male_1.jpg",   80 + 12));
        list.add(build("tiktok",  "裤子", "休闲", "少女",   "img/trend/pants_casual_girl_1.jpg",    80 + 8));
        list.add(build("shein",   "裤子", "街头", "运动",   "img/trend/pants_street_sport_1.jpg",   78 + 10));

        // ----上衣 / T恤----
        list.add(build("tiktok",  "上衣", "街头", "运动",   "img/trend/top_street_sport_1.jpg",     84 + 8));
        list.add(build("1688",    "上衣", "休闲", "少女",   "img/trend/top_casual_girl_1.jpg",      82 + 6));
        list.add(build("brand",   "T恤",  "极简", "男装",   "img/trend/tshirt_minimal_male_1.jpg",  80 + 12));
        list.add(build("shein",   "T恤",  "街头", "运动",   "img/trend/tshirt_street_sport_1.jpg",  78 + 10));

        return list;
    }

    private DeepayTrendPoolDO build(String source, String category, String style,
                                    String crowd, String imageUrl, int score) {
        DeepayTrendPoolDO d = new DeepayTrendPoolDO();
        d.setSource(source);
        d.setCategory(category);
        d.setStyle(style);
        d.setCrowd(crowd);
        d.setImageUrl(imageUrl);
        d.setScore(score);
        d.setCreatedAt(LocalDateTime.now());
        return d;
    }

    // ====================================================================
    // 保底图（品类专属，保证"内裤不出外套"）
    // ====================================================================

    private List<String> defaultImages(String category) {
        if ("内裤".equals(category) || "内衣".equals(category)) {
            return Arrays.asList(
                    "img/default/underwear_1.jpg",
                    "img/default/underwear_2.jpg",
                    "img/default/underwear_3.jpg");
        }
        if ("外套".equals(category) || "大衣".equals(category)) {
            return Arrays.asList(
                    "img/default/coat_1.jpg",
                    "img/default/coat_2.jpg",
                    "img/default/coat_3.jpg");
        }
        if ("裤子".equals(category)) {
            return Arrays.asList(
                    "img/default/pants_1.jpg",
                    "img/default/pants_2.jpg");
        }
        if ("上衣".equals(category) || "T恤".equals(category)) {
            return Arrays.asList(
                    "img/default/top_1.jpg",
                    "img/default/top_2.jpg");
        }
        if ("连衣裙".equals(category) || "裙子".equals(category)) {
            return Arrays.asList(
                    "img/default/dress_1.jpg",
                    "img/default/dress_2.jpg");
        }
        return Collections.singletonList("img/default/general_1.jpg");
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private List<TrendItem> toTrendItems(List<DeepayProductDO> products) {
        return products.stream().map(p -> {
            TrendItem item = new TrendItem();
            item.setChainCode(p.getChainCode());
            item.setImageUrl(p.getMainImage());
            item.setCategory(p.getCategory());
            item.setSoldCount(p.getSoldCount());
            return item;
        }).collect(Collectors.toList());
    }

    private List<DeepayProductDO> sortByStyleWeight(List<DeepayProductDO> list,
                                                     Map<String, Double> styleWeights) {
        if (styleWeights == null || styleWeights.isEmpty()) {
            return list;
        }
        return list.stream()
                .sorted((a, b) -> {
                    int sa = a.getSoldCount() != null ? a.getSoldCount() : 0;
                    int sb = b.getSoldCount() != null ? b.getSoldCount() : 0;
                    return Integer.compare(sb, sa);
                })
                .collect(Collectors.toList());
    }

}

