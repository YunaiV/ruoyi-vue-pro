package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayHotProductDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayVariantDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayHotProductMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayVariantMapper;
import cn.iocoder.yudao.module.deepay.service.FluxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HotCloneAgent — 爆款复制引擎（Phase 8）。
 *
 * <h3>核心流程</h3>
 * <pre>
 * 爆款识别（sold_count &ge; 10）
 *    ↓
 * 写入 deepay_hot_product
 *    ↓
 * ProductDNA 结构拆解（品类 / 颜色 / 面料 / 版型 / 风格）
 *    ↓
 * 排列组合 → 最多 {@value #MAX_VARIANTS_PER_PRODUCT} 个变体
 *    ↓
 * DesignAgent（FluxService）逐一出图
 *    ↓
 * 持久化 deepay_variant
 * </pre>
 *
 * <h3>爆款识别阈值</h3>
 * <ul>
 *   <li>sold_count &ge; {@value #HOT_THRESHOLD}       → {@code HOT}</li>
 *   <li>sold_count &ge; {@value #SUPER_HOT_THRESHOLD} → {@code SUPER_HOT}</li>
 * </ul>
 *
 * <h3>变体 DNA 参数（第一版写死）</h3>
 * <p>每个品类对应三组参数（颜色 / 面料 / 版型），排列组合后取前
 * {@value #MAX_VARIANTS_PER_PRODUCT} 个。</p>
 *
 * <h3>run(ctx) 行为</h3>
 * <ul>
 *   <li>若 {@code ctx.chainCode} 非空：仅处理该单品（若达到爆款阈值则克隆）。</li>
 *   <li>若 {@code ctx.chainCode} 为空：批量扫描数据库中所有爆款并为尚未克隆的品生成变体。</li>
 * </ul>
 *
 * <p>任何异常均被捕获并记录日志，不影响调用方主流程。</p>
 */
@Component
public class HotCloneAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(HotCloneAgent.class);

    /** 普通爆款阈值：sold_count &ge; 10 → HOT */
    static final int HOT_THRESHOLD       = 10;
    /** 超级爆款阈值：sold_count &ge; 50 → SUPER_HOT */
    static final int SUPER_HOT_THRESHOLD = 50;
    /** 每个爆款最多生成的变体数量（第一版上限） */
    static final int MAX_VARIANTS_PER_PRODUCT = 15;
    /** 批量扫描时每批最多处理的爆款数量 */
    private static final int BATCH_LIMIT = 20;

    // ====================================================================
    // 第一版写死 DNA 参数（颜色 / 面料 / 版型）
    // ====================================================================

    /** 颜色选项：key=品类，value=颜色列表 */
    private static final Map<String, List<String>> COLORS_BY_CATEGORY;
    /** 面料选项：key=品类，value=面料列表 */
    private static final Map<String, List<String>> FABRICS_BY_CATEGORY;
    /** 版型选项：key=品类，value=版型列表 */
    private static final Map<String, List<String>> FITS_BY_CATEGORY;
    /** 领型默认值：key=品类，value=领型 */
    private static final Map<String, String> COLLAR_BY_CATEGORY;
    /** 长度默认值：key=品类，value=长度 */
    private static final Map<String, String> LENGTH_BY_CATEGORY;

    static {
        COLORS_BY_CATEGORY = new HashMap<>();
        COLORS_BY_CATEGORY.put("外套",  Arrays.asList("黑", "灰", "米白", "深蓝", "卡其"));
        COLORS_BY_CATEGORY.put("大衣",  Arrays.asList("黑", "灰", "米白", "深蓝", "卡其"));
        COLORS_BY_CATEGORY.put("内裤",  Arrays.asList("黑", "白", "肤色", "灰"));
        COLORS_BY_CATEGORY.put("内衣",  Arrays.asList("黑", "白", "肤色", "粉"));
        COLORS_BY_CATEGORY.put("连衣裙", Arrays.asList("黑", "白", "红", "粉", "蓝"));
        COLORS_BY_CATEGORY.put("裙子",  Arrays.asList("黑", "白", "红", "粉", "蓝"));
        COLORS_BY_CATEGORY.put("裤子",  Arrays.asList("黑", "灰", "深蓝", "卡其"));
        COLORS_BY_CATEGORY.put("上衣",  Arrays.asList("白", "黑", "灰", "米色"));
        COLORS_BY_CATEGORY.put("T恤",   Arrays.asList("白", "黑", "灰", "藏蓝"));

        FABRICS_BY_CATEGORY = new HashMap<>();
        FABRICS_BY_CATEGORY.put("外套",  Arrays.asList("棉", "羊毛", "牛仔"));
        FABRICS_BY_CATEGORY.put("大衣",  Arrays.asList("羊毛", "混纺", "棉"));
        FABRICS_BY_CATEGORY.put("内裤",  Arrays.asList("棉", "莫代尔", "蕾丝"));
        FABRICS_BY_CATEGORY.put("内衣",  Arrays.asList("棉", "莫代尔", "蕾丝"));
        FABRICS_BY_CATEGORY.put("连衣裙", Arrays.asList("雪纺", "丝绸", "棉"));
        FABRICS_BY_CATEGORY.put("裙子",  Arrays.asList("雪纺", "丝绸", "棉"));
        FABRICS_BY_CATEGORY.put("裤子",  Arrays.asList("牛仔", "棉", "西装面料"));
        FABRICS_BY_CATEGORY.put("上衣",  Arrays.asList("棉", "针织", "雪纺"));
        FABRICS_BY_CATEGORY.put("T恤",   Arrays.asList("纯棉", "莫代尔", "竹节棉"));

        FITS_BY_CATEGORY = new HashMap<>();
        FITS_BY_CATEGORY.put("外套",  Arrays.asList("宽松", "修身"));
        FITS_BY_CATEGORY.put("大衣",  Arrays.asList("宽松", "修身"));
        FITS_BY_CATEGORY.put("内裤",  Collections.singletonList("标准"));
        FITS_BY_CATEGORY.put("内衣",  Collections.singletonList("标准"));
        FITS_BY_CATEGORY.put("连衣裙", Arrays.asList("修身", "宽松"));
        FITS_BY_CATEGORY.put("裙子",  Arrays.asList("修身", "宽松"));
        FITS_BY_CATEGORY.put("裤子",  Arrays.asList("宽松", "修身", "直筒"));
        FITS_BY_CATEGORY.put("上衣",  Arrays.asList("宽松", "修身"));
        FITS_BY_CATEGORY.put("T恤",   Arrays.asList("宽松", "修身"));

        COLLAR_BY_CATEGORY = new HashMap<>();
        COLLAR_BY_CATEGORY.put("外套",  "翻领");
        COLLAR_BY_CATEGORY.put("大衣",  "翻领");
        COLLAR_BY_CATEGORY.put("连衣裙", "V领");
        COLLAR_BY_CATEGORY.put("上衣",  "圆领");
        COLLAR_BY_CATEGORY.put("T恤",   "圆领");

        LENGTH_BY_CATEGORY = new HashMap<>();
        LENGTH_BY_CATEGORY.put("外套",  "中长款");
        LENGTH_BY_CATEGORY.put("大衣",  "长款");
        LENGTH_BY_CATEGORY.put("裤子",  "长款");
        LENGTH_BY_CATEGORY.put("连衣裙", "中长款");
        LENGTH_BY_CATEGORY.put("裙子",  "中长款");
        LENGTH_BY_CATEGORY.put("上衣",  "短款");
        LENGTH_BY_CATEGORY.put("T恤",   "短款");
    }

    // ====================================================================
    // 依赖注入
    // ====================================================================

    @Resource private DeepayProductMapper    productMapper;
    @Resource private DeepayMetricsMapper    metricsMapper;
    @Resource private DeepayHotProductMapper hotProductMapper;
    @Resource private DeepayVariantMapper    variantMapper;
    @Resource private FluxService            fluxService;

    // ====================================================================
    // Agent.run
    // ====================================================================

    /**
     * 单品或批量模式入口。
     *
     * <ul>
     *   <li>{@code ctx.chainCode} 非空 → 仅处理该单品</li>
     *   <li>{@code ctx.chainCode} 为空 → 批量扫描数据库中尚未克隆的爆款</li>
     * </ul>
     */
    @Override
    public Context run(Context ctx) {
        if (StringUtils.hasText(ctx.chainCode)) {
            runSingle(ctx.chainCode, ctx);
        } else {
            runBatch(ctx);
        }
        return ctx;
    }

    /**
     * 批量扫描入口（由 {@link cn.iocoder.yudao.module.deepay.scheduler.DeepayHotCloneScheduler} 调用）。
     */
    public void runBatch() {
        runBatch(new Context());
    }

    // ====================================================================
    // 核心实现
    // ====================================================================

    private void runSingle(String chainCode, Context ctx) {
        log.info("[HotCloneAgent] 单品扫描 chainCode={}", chainCode);
        try {
            DeepayProductDO product = productMapper.selectByChainCode(chainCode);
            if (product == null) {
                log.debug("[HotCloneAgent] 商品不存在 chainCode={}", chainCode);
                return;
            }
            int soldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
            if (soldCount < HOT_THRESHOLD) {
                log.debug("[HotCloneAgent] 销量 {} 未达爆款阈值 {} chainCode={}",
                        soldCount, HOT_THRESHOLD, chainCode);
                return;
            }
            DeepayHotProductDO hotProduct = upsertHotProduct(product, soldCount);
            List<DeepayVariantDO> variants = cloneVariants(hotProduct);
            ctx.variants = variants;
            log.info("[HotCloneAgent] 单品克隆完成 chainCode={} variants={}",
                    chainCode, variants.size());
        } catch (Exception e) {
            log.error("[HotCloneAgent] 单品克隆异常 chainCode={}", chainCode, e);
        }
    }

    private void runBatch(Context ctx) {
        log.info("[HotCloneAgent] 批量扫描开始");
        try {
            // 先扫描新爆款并写入 deepay_hot_product
            scanAndRegisterHotProducts();
            // 再克隆尚未生成变体的爆款
            List<DeepayHotProductDO> pending = hotProductMapper.selectHotWithoutVariants(BATCH_LIMIT);
            log.info("[HotCloneAgent] 待克隆爆款数量={}", pending.size());
            List<DeepayVariantDO> allVariants = new ArrayList<>();
            for (DeepayHotProductDO hotProduct : pending) {
                try {
                    List<DeepayVariantDO> variants = cloneVariants(hotProduct);
                    allVariants.addAll(variants);
                } catch (Exception e) {
                    log.error("[HotCloneAgent] 克隆异常 chainCode={}", hotProduct.getChainCode(), e);
                }
            }
            ctx.variants = allVariants;
            log.info("[HotCloneAgent] 批量扫描完成，共生成变体={}", allVariants.size());
        } catch (Exception e) {
            log.error("[HotCloneAgent] 批量扫描异常", e);
        }
    }

    /**
     * 扫描 deepay_product + deepay_metrics，将销量达标的新商品注册为爆款。
     */
    private void scanAndRegisterHotProducts() {
        // 使用 TrendService 已有的 selectHotByCategory 是品类限定的，这里用 selectBySelling
        // 遍历所有在售商品，按 soldCount 判断
        List<DeepayProductDO> sellingProducts = productMapper.selectBySelling();
        int registered = 0;
        for (DeepayProductDO product : sellingProducts) {
            int soldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
            if (soldCount >= HOT_THRESHOLD && hotProductMapper.selectByChainCode(product.getChainCode()) == null) {
                upsertHotProduct(product, soldCount);
                registered++;
            }
        }
        if (registered > 0) {
            log.info("[HotCloneAgent] 新增爆款记录 count={}", registered);
        }
    }

    /**
     * 将商品写入（或更新）爆款表。
     */
    private DeepayHotProductDO upsertHotProduct(DeepayProductDO product, int soldCount) {
        DeepayHotProductDO existing = hotProductMapper.selectByChainCode(product.getChainCode());
        if (existing != null) {
            return existing;
        }
        DeepayHotProductDO hot = new DeepayHotProductDO();
        hot.setChainCode(product.getChainCode());
        hot.setCategory(product.getCategory());
        hot.setImageUrl(product.getCdnImageUrl());
        hot.setSoldCount(soldCount);
        hot.setHotLevel(soldCount >= SUPER_HOT_THRESHOLD ? "SUPER_HOT" : "HOT");
        hot.setScore(soldCount / 50.0);
        hot.setCreatedAt(LocalDateTime.now());

        // 从 metrics 补充 style 字段
        try {
            String style = resolveStyle(product.getChainCode());
            hot.setStyle(style);
        } catch (Exception e) {
            log.warn("[HotCloneAgent] 查询 style 失败 chainCode={}", product.getChainCode(), e);
        }

        hotProductMapper.insert(hot);
        log.info("[HotCloneAgent] 爆款注册 chainCode={} level={} soldCount={}",
                hot.getChainCode(), hot.getHotLevel(), soldCount);
        return hot;
    }

    /**
     * 为一个爆款生成所有变体，持久化后返回列表。
     */
    private List<DeepayVariantDO> cloneVariants(DeepayHotProductDO hotProduct) {
        String category = hotProduct.getCategory();
        ProductDNA baseDna = buildBaseDNA(hotProduct);
        List<ProductDNA> variantDnas = generateVariants(baseDna, category);

        List<DeepayVariantDO> saved = new ArrayList<>();
        int seq = 1;
        for (ProductDNA dna : variantDnas) {
            String variantCode = hotProduct.getChainCode() + "-V" + String.format("%03d", seq);
            // 已存在则跳过
            if (variantMapper.selectByVariantCode(variantCode) != null) {
                seq++;
                continue;
            }

            String prompt = dna.toPrompt();
            String imageUrl = generateImage(prompt);

            DeepayVariantDO variant = new DeepayVariantDO();
            variant.setParentChainCode(hotProduct.getChainCode());
            variant.setVariantCode(variantCode);
            variant.setCategory(dna.category);
            variant.setColor(dna.color);
            variant.setFabric(dna.fabric);
            variant.setFit(dna.fit);
            variant.setStyle(dna.style);
            variant.setImageUrl(imageUrl);
            variant.setDesignPrompt(prompt);
            variant.setCreatedAt(LocalDateTime.now());

            try {
                variantMapper.insert(variant);
                saved.add(variant);
                log.debug("[HotCloneAgent] 变体生成 variantCode={} color={} fabric={} fit={}",
                        variantCode, dna.color, dna.fabric, dna.fit);
            } catch (Exception e) {
                log.warn("[HotCloneAgent] 变体写库失败 variantCode={}", variantCode, e);
            }
            seq++;
        }

        log.info("[HotCloneAgent] 爆款克隆完成 parent={} variantsGenerated={}",
                hotProduct.getChainCode(), saved.size());
        return saved;
    }

    // ====================================================================
    // DNA 构建 & 变体生成
    // ====================================================================

    /**
     * 从爆款记录构建父款 DNA（品类 / 版型 / 长度 / 领型 / 风格固定，颜色/面料取品类默认首选）。
     */
    private ProductDNA buildBaseDNA(DeepayHotProductDO hot) {
        String category = hot.getCategory();
        ProductDNA dna = new ProductDNA();
        dna.category = category;
        dna.style    = hot.getStyle();
        dna.fit      = defaultFit(category);
        dna.length   = LENGTH_BY_CATEGORY.getOrDefault(category, null);
        dna.collar   = COLLAR_BY_CATEGORY.getOrDefault(category, null);
        dna.color    = defaultColor(category);
        dna.fabric   = defaultFabric(category);
        return dna;
    }

    /**
     * 核心变体生成算法：颜色 × 面料 × 版型 排列组合，上限 {@value #MAX_VARIANTS_PER_PRODUCT}。
     */
    private List<ProductDNA> generateVariants(ProductDNA base, String category) {
        List<String> colors  = COLORS_BY_CATEGORY.getOrDefault(category,  Arrays.asList("黑", "白", "灰"));
        List<String> fabrics = FABRICS_BY_CATEGORY.getOrDefault(category, Arrays.asList("棉", "针织"));
        List<String> fits    = FITS_BY_CATEGORY.getOrDefault(category,    Collections.singletonList("标准"));

        List<ProductDNA> variants = new ArrayList<>();
        outer:
        for (String fit : fits) {
            for (String fabric : fabrics) {
                for (String color : colors) {
                    if (variants.size() >= MAX_VARIANTS_PER_PRODUCT) break outer;
                    // 跳过与父款完全相同的组合
                    if (color.equals(base.color) && fabric.equals(base.fabric) && fit.equals(base.fit)) {
                        continue;
                    }
                    ProductDNA variant = base.copy();
                    variant.color  = color;
                    variant.fabric = fabric;
                    variant.fit    = fit;
                    variants.add(variant);
                }
            }
        }
        return variants;
    }

    // ====================================================================
    // 工具方法
    // ====================================================================

    private String generateImage(String prompt) {
        try {
            List<String> images = fluxService.generateImages(prompt);
            if (images != null && !images.isEmpty()) {
                return images.get(0);
            }
        } catch (Exception e) {
            log.warn("[HotCloneAgent] FluxService 出图异常，跳过 prompt={}", prompt, e);
        }
        return null;
    }

    private String resolveStyle(String chainCode) {
        // 从 metrics 最新记录取 style（如可用）
        // 此处复用 MetricsMapper 现有方法，通过 selectTopTrend 过滤
        // 由于 MetricsMapper 没有按 chainCode 取 style 的专用方法，直接返回 null 兜底
        return null;
    }

    private String defaultColor(String category) {
        List<String> colors = COLORS_BY_CATEGORY.getOrDefault(category, Arrays.asList("黑", "白", "灰"));
        return colors.isEmpty() ? "黑" : colors.get(0);
    }

    private String defaultFabric(String category) {
        List<String> fabrics = FABRICS_BY_CATEGORY.getOrDefault(category, Arrays.asList("棉", "针织"));
        return fabrics.isEmpty() ? "棉" : fabrics.get(0);
    }

    private String defaultFit(String category) {
        List<String> fits = FITS_BY_CATEGORY.getOrDefault(category, Collections.singletonList("标准"));
        return fits.isEmpty() ? "标准" : fits.get(0);
    }

}
