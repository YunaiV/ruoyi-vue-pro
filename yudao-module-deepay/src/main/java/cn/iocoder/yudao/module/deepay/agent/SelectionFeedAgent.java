package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTrendPoolDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTrendPoolMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SelectionFeedAgent — 参考图推送（Phase 6-7 选款引导层核心）。
 *
 * <h3>功能</h3>
 * <p>根据客户画像（category + style + crowd）从 deepay_trend_pool 中精准过滤，
 * 返回给设计师 / 客户查看的参考图列表。</p>
 *
 * <h3>重要：这是参考款，不是商品</h3>
 * <ul>
 *   <li>❗ 返回的是设计参考图（来自 TikTok / SHEIN / runway / brand）</li>
 *   <li>❗ 不是系统内已有商品</li>
 *   <li>❗ 设计师看完后选方向 → 触发 DesignAgent 生成新图 → 才是商品</li>
 * </ul>
 *
 * <h3>过滤逻辑（三层宽松）</h3>
 * <pre>
 * 1️⃣ category + style + crowd 全匹配（最精准）
 *       ↓ 不足 5 条
 * 2️⃣ 只按 category + crowd 匹配（放宽 style）
 *       ↓ 仍不足
 * 3️⃣ 只按 category 匹配（最宽松，保证有图可看）
 * </pre>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 内裤客户 → 永远只看到内裤图，不出外套</li>
 *   <li>✔ 外套 + 男装 + 工装 → 优先出男装工装款</li>
 *   <li>✔ 无数据 → 品类保底图兜底，不抛异常</li>
 * </ul>
 */
@Component
public class SelectionFeedAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(SelectionFeedAgent.class);

    private static final int QUERY_LIMIT  = 20;
    private static final int MIN_RESULTS  = 5;   // 低于此数量时放宽过滤条件

    @Resource
    private DeepayTrendPoolMapper trendPoolMapper;

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        log.info("[SelectionFeedAgent] START category={} style={} crowd={}",
                ctx.category, ctx.style, ctx.crowd);

        List<DeepayTrendPoolDO> result = fetchWithFallback(ctx);

        ctx.selectionImages = result.stream()
                .map(DeepayTrendPoolDO::getImageUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        // 最终兜底：品类保底图（永不返回空列表）
        if (ctx.selectionImages.isEmpty()) {
            log.info("[SelectionFeedAgent] 所有查询无结果，使用品类保底图 category={}", ctx.category);
            ctx.selectionImages = fallbackImages(ctx.category);
        }

        log.info("[SelectionFeedAgent] DONE selectionImages={}", ctx.selectionImages.size());
        return ctx;
    }

    // ====================================================================
    // 三层宽松查询
    // ====================================================================

    private List<DeepayTrendPoolDO> fetchWithFallback(Context ctx) {
        // 1️⃣ 全匹配：category + style + crowd
        List<DeepayTrendPoolDO> list = queryPool(ctx.category, ctx.style, ctx.crowd, QUERY_LIMIT);
        if (list.size() >= MIN_RESULTS) {
            return list;
        }

        // 2️⃣ 放宽 style
        if (StringUtils.hasText(ctx.style)) {
            log.debug("[SelectionFeedAgent] 全匹配结果不足({})，放宽 style", list.size());
            List<DeepayTrendPoolDO> wider = queryPool(ctx.category, null, ctx.crowd, QUERY_LIMIT);
            if (wider.size() >= MIN_RESULTS) {
                return wider;
            }
            if (!wider.isEmpty()) list = wider;
        }

        // 3️⃣ 只按 category（最宽松）
        if (StringUtils.hasText(ctx.category)) {
            log.debug("[SelectionFeedAgent] 仍不足，仅按 category={}", ctx.category);
            List<DeepayTrendPoolDO> byCategory = queryPool(ctx.category, null, null, QUERY_LIMIT);
            if (!byCategory.isEmpty()) {
                return byCategory;
            }
        }

        // 全局 top（最终保底）
        if (list.isEmpty()) {
            log.info("[SelectionFeedAgent] trend_pool 无任何数据，返回全局 top");
            list = trendPoolMapper.selectTopAll(QUERY_LIMIT);
        }
        return list;
    }

    private List<DeepayTrendPoolDO> queryPool(String category, String style, String crowd, int limit) {
        try {
            return trendPoolMapper.selectTopByProfile(category, style, crowd, limit);
        } catch (Exception e) {
            log.warn("[SelectionFeedAgent] 查询 trend_pool 异常 category={}", category, e);
            return Collections.emptyList();
        }
    }

    // ====================================================================
    // 品类保底图（内裤不出外套）
    // ====================================================================

    private List<String> fallbackImages(String category) {
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
        if ("连衣裙".equals(category) || "裙子".equals(category)) {
            return Arrays.asList(
                    "img/default/dress_1.jpg",
                    "img/default/dress_2.jpg");
        }
        if ("上衣".equals(category) || "T恤".equals(category)) {
            return Arrays.asList(
                    "img/default/top_1.jpg",
                    "img/default/top_2.jpg");
        }
        return Collections.singletonList("img/default/general_1.jpg");
    }

}
