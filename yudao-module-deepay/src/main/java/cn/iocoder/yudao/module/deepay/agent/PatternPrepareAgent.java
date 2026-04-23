package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * PatternPrepareAgent — 在 PatternAgent 之前准备版型文件名和技术包 URL（Phase 8）。
 *
 * <p>若 PatternAgent 已经写入 {@link Context#patternFile}，则直接跳过。
 * 否则根据 chainCode 和 category 生成预设的文件路径，供后续 PatternAgent 使用或直接使用。</p>
 */
@Component
public class PatternPrepareAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PatternPrepareAgent.class);

    @Override
    public Context run(Context ctx) {
        try {
            if (StringUtils.hasText(ctx.patternFile)) {
                log.info("[PatternPrepareAgent] patternFile 已存在，跳过 patternFile={}", ctx.patternFile);
                return ctx;
            }

            String code     = ctx.chainCode != null ? ctx.chainCode : "UNKNOWN";
            String category = ctx.category  != null ? ctx.category  : "unknown";

            String patternFileName = "pattern_" + code + "_" + category + ".pdf";
            String techPackUrl     = "https://deepay-assets.example.com/techpack/" + code + ".pdf";

            ctx.patternFile  = patternFileName;
            ctx.techPackUrl  = techPackUrl;

            log.info("[PatternPrepareAgent] patternFile={} techPackUrl={}", patternFileName, techPackUrl);
        } catch (Exception e) {
            log.warn("[PatternPrepareAgent] 版型准备异常，跳过", e);
        }
        return ctx;
    }

}
