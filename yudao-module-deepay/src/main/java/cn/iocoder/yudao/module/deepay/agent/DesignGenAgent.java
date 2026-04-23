package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.service.FluxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * DesignGenAgent — 使用最终 Prompt 生成 4 张设计图（Phase 8）。
 *
 * <p>Prompt 优先级：
 * <ol>
 *   <li>{@link Context#finalPrompt}（StyleEngineAgent / StyleConsistencyAgent 输出，最高优先级）</li>
 *   <li>{@link Context#stylePrompt}（StyleEngine 组装的风格描述）</li>
 *   <li>{@link Context#keyword}（兜底关键词）</li>
 * </ol>
 * </p>
 */
@Component
public class DesignGenAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignGenAgent.class);

    @Resource
    private FluxService fluxService;

    @Override
    public Context run(Context ctx) {
        try {
            String prompt = resolvePrompt(ctx);
            log.info("[DesignGenAgent] START prompt={}", prompt);
            ctx.designImages = fluxService.generateImages(prompt, 4);
            log.info("[DesignGenAgent] DONE 生成 {} 张图片", ctx.designImages.size());
        } catch (Exception e) {
            log.warn("[DesignGenAgent] 生成设计图异常，跳过", e);
        }
        return ctx;
    }

    private String resolvePrompt(Context ctx) {
        if (StringUtils.hasText(ctx.finalPrompt)) {
            return ctx.finalPrompt;
        }
        if (StringUtils.hasText(ctx.stylePrompt)) {
            return ctx.stylePrompt;
        }
        return StringUtils.hasText(ctx.keyword) ? ctx.keyword : "";
    }

}
