package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * DesignConfirmAgent — 确保品类字段已设置，否则暂停流程请求用户输入（Phase 8）。
 *
 * <p>有效品类：上衣、外套、裤子、内裤、裙子、连衣裙。</p>
 */
@Component
public class DesignConfirmAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignConfirmAgent.class);

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "上衣", "外套", "裤子", "内裤", "裙子", "连衣裙"
    );

    @Override
    public Context run(Context ctx) {
        try {
            if (!StringUtils.hasText(ctx.category)) {
                ctx.pendingQuestion = "请确认您的品类（上衣 / 外套 / 裤子 / 内裤 / 裙子）";
                log.info("[DesignConfirmAgent] 品类未设置，暂停等待用户输入");
                return ctx;
            }

            // 规范化别名
            String normalized = normalize(ctx.category);
            if (!normalized.equals(ctx.category)) {
                log.info("[DesignConfirmAgent] 品类规范化 {} → {}", ctx.category, normalized);
                ctx.category = normalized;
            }

            log.info("[DesignConfirmAgent] 品类已确认 category={}", ctx.category);
        } catch (Exception e) {
            log.warn("[DesignConfirmAgent] 确认品类异常，跳过", e);
        }
        return ctx;
    }

    private String normalize(String category) {
        if (category == null) return "";
        switch (category.trim()) {
            case "T恤": case "衬衫": case "针织衫": return "上衣";
            case "大衣": case "风衣": case "夹克": return "外套";
            case "牛仔裤": case "休闲裤": case "运动裤": return "裤子";
            case "内衣": return "内裤";
            default: return category.trim();
        }
    }

}
