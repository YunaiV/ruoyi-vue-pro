package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BrandFilterAgent — 层1 爆款过滤，只保留目标市场 / 客群对应品牌的款式（Phase 6-7）。
 *
 * <h3>功能</h3>
 * <p>在 SelectionFeedAgent 推图之前（或之后），根据客户的 market + crowd + priceLevel
 * 过滤 {@link Context#selectionImages}，去掉不属于目标品牌范围的款式图，
 * 防止"欧美外套客户看到中东晚礼服"的乱推现象。</p>
 *
 * <h3>品牌白名单（可扩展为数据库配置）</h3>
 * <pre>
 * 市场 EU + 高端  → Gucci, Prada, Balenciaga, COS
 * 市场 EU + 中低端 → ZARA, H&M, Mango, Uniqlo
 * 市场 US + 高端  → Off-White, Coach, Ralph Lauren
 * 市场 US + 中低端 → Nike, Adidas, Levi's, Tommy
 * 市场 ME        → Gucci, Prada, Chanel, ZARA
 * 市场 CN        → SHEIN, 优衣库, H&M, 1688
 * </pre>
 *
 * <h3>过滤规则</h3>
 * <ol>
 *   <li>selectionImages 中 URL 包含 blacklist 品牌关键词 → 丢弃</li>
 *   <li>URL 不含任何品牌信息 → 默认保留（不误杀）</li>
 *   <li>过滤后若列表为空 → 保留原列表（防止流程卡死）</li>
 * </ol>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ EU+高端客户 → 不出现 SHEIN / 1688 关键词图片</li>
 *   <li>✔ CN+低端客户 → 不出现 Balenciaga / Gucci 关键词图片</li>
 *   <li>✔ 无 market 信息 → 不过滤（全放行）</li>
 * </ul>
 */
@Component
public class BrandFilterAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(BrandFilterAgent.class);

    // ====================================================================
    // 品牌黑名单（目标市场不合适的品牌关键词 → 过滤）
    // ====================================================================

    /** market → 该市场的黑名单品牌关键词（URL 中出现则过滤） */
    private static final Map<String, List<String>> MARKET_BLACKLIST;

    static {
        Map<String, List<String>> m = new HashMap<>();
        // 欧美市场：屏蔽东南亚廉价款关键词
        m.put("EU", Arrays.asList("shein", "1688", "aliexpress", "taobao", "pinduoduo"));
        m.put("US", Arrays.asList("shein", "1688", "aliexpress", "taobao", "pinduoduo"));
        // 中东市场：屏蔽廉价快时尚（中东偏轻奢）
        m.put("ME", Arrays.asList("shein", "1688", "aliexpress", "h&m", "primark", "forever21"));
        // 国内市场：不做特殊屏蔽
        m.put("CN", Collections.emptyList());
        MARKET_BLACKLIST = Collections.unmodifiableMap(m);
    }

    /** priceLevel → 价位黑名单（高端不出现快时尚关键词） */
    private static final Map<String, List<String>> PRICE_BLACKLIST;

    static {
        Map<String, List<String>> m = new HashMap<>();
        m.put("HIGH", Arrays.asList("shein", "1688", "aliexpress", "primark", "forever21", "h&m"));
        m.put("MID",  Collections.emptyList());
        m.put("LOW",  Arrays.asList("gucci", "prada", "balenciaga", "chanel", "hermes", "lv"));
        PRICE_BLACKLIST = Collections.unmodifiableMap(m);
    }

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        if (ctx.selectionImages == null || ctx.selectionImages.isEmpty()) {
            log.debug("[BrandFilterAgent] selectionImages 为空，跳过");
            return ctx;
        }
        if (!StringUtils.hasText(ctx.market) && !StringUtils.hasText(ctx.priceLevel)) {
            log.debug("[BrandFilterAgent] 无 market/priceLevel，不过滤");
            return ctx;
        }

        List<String> blacklist = buildBlacklist(ctx.market, ctx.priceLevel);
        if (blacklist.isEmpty()) {
            return ctx;
        }

        List<String> passed    = new java.util.ArrayList<>();
        List<String> discarded = new java.util.ArrayList<>();

        for (String url : ctx.selectionImages) {
            if (isBlocked(url, blacklist)) {
                discarded.add(url);
            } else {
                passed.add(url);
            }
        }

        if (!discarded.isEmpty()) {
            log.info("[BrandFilterAgent] 品牌过滤 discarded={} market={} priceLevel={}",
                    discarded.size(), ctx.market, ctx.priceLevel);
        }

        // 防止全部被过滤（保底保留原列表）
        if (!passed.isEmpty()) {
            ctx.selectionImages = passed;
        } else {
            log.warn("[BrandFilterAgent] 过滤后无图，保留原列表 market={}", ctx.market);
        }

        log.info("[BrandFilterAgent] 完成 passed={} discarded={}", passed.size(), discarded.size());
        return ctx;
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private List<String> buildBlacklist(String market, String priceLevel) {
        java.util.Set<String> set = new java.util.LinkedHashSet<>();
        if (StringUtils.hasText(market)) {
            List<String> mb = MARKET_BLACKLIST.get(market.toUpperCase());
            if (mb != null) set.addAll(mb);
        }
        if (StringUtils.hasText(priceLevel)) {
            List<String> pb = PRICE_BLACKLIST.get(priceLevel.toUpperCase());
            if (pb != null) set.addAll(pb);
        }
        return new java.util.ArrayList<>(set);
    }

    private boolean isBlocked(String url, List<String> blacklist) {
        if (!StringUtils.hasText(url)) return false;
        String lower = url.toLowerCase();
        for (String kw : blacklist) {
            if (lower.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
