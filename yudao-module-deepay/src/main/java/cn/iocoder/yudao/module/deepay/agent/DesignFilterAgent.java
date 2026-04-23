package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DesignFilterAgent — AI 生成图质量过滤（Phase 8）。
 *
 * <h3>职责</h3>
 * <p>在 DesignAgent 生成 3~6 张图之后、进入 JudgeAgent 评分之前，
 * 先做一次规则过滤，去掉明显"垃圾图"，只保留"能卖"的图。</p>
 *
 * <h3>过滤规则（先写死，后接 AI 视觉检测）</h3>
 * <ol>
 *   <li><b>URL 有效性</b>：非空、非 error/failed/null 关键词</li>
 *   <li><b>品类一致性</b>：URL 中不含其他品类关键词（防止"外套"出现在"内裤"场景）</li>
 *   <li><b>保留默认图</b>：URL 含 "default" 的保底图直接放行</li>
 * </ol>
 *
 * <h3>兜底</h3>
 * <p>过滤后若无图可用，保留原始列表（防止流程卡死），并记录告警。</p>
 */
@Component
public class DesignFilterAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignFilterAgent.class);

    /** 明确无效的 URL 关键词 */
    private static final String[] INVALID_KEYWORDS = {"error", "failed", "null", "undefined", "exception"};

    /** 已知品类关键词（用于跨品类过滤）*/
    private static final String[] KNOWN_CATEGORIES = {
            "coat", "jacket", "外套", "大衣",
            "underwear", "lingerie", "内裤", "内衣",
            "dress", "连衣裙", "裙子",
            "tshirt", "t-shirt", "t恤", "上衣",
            "pants", "trousers", "裤子",
            "shirt", "衬衫",
            "suit", "西装",
            "sweater", "knitwear", "毛衣",
            "puffer", "down jacket", "羽绒服"
    };

    @Override
    public Context run(Context ctx) {
        if (ctx.designImages == null || ctx.designImages.isEmpty()) {
            log.debug("[DesignFilterAgent] 无设计图，跳过过滤");
            return ctx;
        }

        String targetCategory = StringUtils.hasText(ctx.category)
                ? ctx.category.toLowerCase() : null;

        List<String> passed    = new ArrayList<>();
        List<String> discarded = new ArrayList<>();

        for (String imageRef : ctx.designImages) {
            if (isValid(imageRef, targetCategory)) {
                passed.add(imageRef);
            } else {
                discarded.add(imageRef);
            }
        }

        if (!discarded.isEmpty()) {
            log.info("[DesignFilterAgent] 过滤垃圾图 discarded={} category={}",
                    discarded.size(), ctx.category);
        }

        if (passed.isEmpty()) {
            // 全部过滤掉 → 保留原始列表，下游低分处理
            log.warn("[DesignFilterAgent] 过滤后无图，保留原始列表防止流程中断 category={}",
                    ctx.category);
        } else {
            ctx.designImages = passed;
            // 同步清理评分（移除被过滤图片的评分）
            if (ctx.imageScores != null) {
                ctx.imageScores.keySet().retainAll(passed);
            }
        }

        log.info("[DesignFilterAgent] 过滤完成 category={} passed={} discarded={}",
                ctx.category, passed.size(), discarded.size());
        return ctx;
    }

    // ====================================================================
    // 过滤逻辑
    // ====================================================================

    private boolean isValid(String imageRef, String targetCategory) {
        if (!StringUtils.hasText(imageRef)) {
            return false;
        }
        String lower = imageRef.toLowerCase();

        // 规则 1：无效关键词 → 丢弃
        for (String kw : INVALID_KEYWORDS) {
            if (lower.contains(kw)) {
                return false;
            }
        }

        // 规则 2：保底图（default）→ 直接放行
        if (lower.contains("default")) {
            return true;
        }

        // 规则 3：无 targetCategory 或 URL 包含目标品类 → 放行
        if (targetCategory == null || lower.contains(targetCategory)) {
            return true;
        }

        // 规则 4：URL 中含有竞争品类关键词 → 丢弃
        for (String cat : KNOWN_CATEGORIES) {
            if (!cat.toLowerCase().equals(targetCategory) && lower.contains(cat.toLowerCase())) {
                return false;
            }
        }

        // 默认放行（不含品类信息的 AI 生成图，不误杀）
        return true;
    }

}
