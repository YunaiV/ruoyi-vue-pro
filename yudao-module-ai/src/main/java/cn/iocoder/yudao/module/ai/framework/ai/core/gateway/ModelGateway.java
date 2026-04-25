package cn.iocoder.yudao.module.ai.framework.ai.core.gateway;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.runpod.RunpodChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

/**
 * AI 模型网关
 *
 * <p>统一封装：</p>
 * <ul>
 *   <li><b>Fallback</b>：主模型失败/超时/异常时自动切换 Runpod</li>
 *   <li><b>Usage 记录</b>：tenantId / customerId / module / sessionId /
 *       provider / modelId / latencyMs / error / token（拿不到则估算并标记 estimated）</li>
 * </ul>
 *
 * <p>注入示例：
 * <pre>{@code
 * @Resource private ModelGateway modelGateway;
 *
 * ChatResponse resp = modelGateway.call(primaryModel, "openai", "gpt-4o", prompt,
 *         AiUsageContext.builder().tenantId(1L).module("chat").sessionId("sess-1").build());
 * }</pre>
 * </p>
 *
 * @author deepay
 */
@Component
@Slf4j
public class ModelGateway {

    @Nullable
    private final RunpodChatModel fallback;

    public ModelGateway(@Nullable RunpodChatModel fallback) {
        this.fallback = fallback;
        if (fallback != null) {
            log.info("[ModelGateway] Runpod fallback 已启用");
        } else {
            log.info("[ModelGateway] Runpod fallback 未配置（RUNPOD_API_KEY 未设置），仅主模型可用");
        }
    }

    // ========== 同步调用 ==========

    /**
     * 带 Runpod fallback 的同步调用。
     *
     * @param primary         主模型
     * @param primaryProvider 主模型 provider 标识（用于 usage 记录，如 "openai"）
     * @param primaryModelId  主模型 ID（用于 usage 记录，如 "gpt-4o"）
     * @param prompt          调用 Prompt
     * @param context         usage 上下文（tenantId / customerId / module / sessionId）
     * @return ChatResponse
     */
    public ChatResponse call(ChatModel primary, String primaryProvider, String primaryModelId,
                             Prompt prompt, @Nullable AiUsageContext context) {
        Instant start = Instant.now();
        try {
            ChatResponse response = primary.call(prompt);
            recordUsage(context, primaryProvider, primaryModelId,
                    Duration.between(start, Instant.now()), null, response);
            return response;
        } catch (Exception ex) {
            log.warn("[ModelGateway] 主模型 {}/{} 调用失败，切换到 Runpod fallback: {}",
                    primaryProvider, primaryModelId, ex.getMessage());
            return callFallback(prompt, context, ex);
        }
    }

    // ========== 流式调用 ==========

    /**
     * 带 Runpod fallback 的流式调用。
     * 主模型流式出错时降级为 Runpod 同步调用（包装成单元素 Flux）。
     */
    public Flux<ChatResponse> stream(StreamingChatModel primary, String primaryProvider, String primaryModelId,
                                     Prompt prompt, @Nullable AiUsageContext context) {
        Instant start = Instant.now();
        return primary.stream(prompt)
                .doOnComplete(() -> recordUsage(context, primaryProvider, primaryModelId,
                        Duration.between(start, Instant.now()), null, null))
                .onErrorResume(ex -> {
                    log.warn("[ModelGateway] 主模型 {}/{} 流式调用失败，切换到 Runpod fallback: {}",
                            primaryProvider, primaryModelId, ex.getMessage());
                    Instant fbStart = Instant.now();
                    try {
                        ChatResponse fbResponse = callFallback(prompt, context, (Exception) ex);
                        String fbModelId = (fallback != null && fallback.getDefaultOptions() != null)
                                ? fallback.getDefaultOptions().getModel() : "unknown";
                        recordUsage(context, "runpod", fbModelId,
                                Duration.between(fbStart, Instant.now()), null, fbResponse);
                        return Flux.just(fbResponse);
                    } catch (Exception fbEx) {
                        recordUsage(context, "runpod", "unknown",
                                Duration.between(fbStart, Instant.now()), fbEx.getMessage(), null);
                        return Flux.error(fbEx);
                    }
                });
    }

    // ========== 私有方法 ==========

    private ChatResponse callFallback(Prompt prompt, @Nullable AiUsageContext context, Exception primaryEx) {
        if (fallback == null) {
            throw new IllegalStateException(
                    "[ModelGateway] 主模型调用失败且 Runpod fallback 未配置（请设置 RUNPOD_API_KEY）", primaryEx);
        }
        Instant start = Instant.now();
        String fallbackModelId = (fallback.getDefaultOptions() != null)
                ? fallback.getDefaultOptions().getModel() : "unknown";
        try {
            ChatResponse response = fallback.call(prompt);
            recordUsage(context, "runpod", fallbackModelId,
                    Duration.between(start, Instant.now()), null, response);
            return response;
        } catch (Exception ex) {
            recordUsage(context, "runpod", fallbackModelId,
                    Duration.between(start, Instant.now()), ex.getMessage(), null);
            throw new RuntimeException("[ModelGateway] 主模型与 Runpod fallback 均调用失败", ex);
        }
    }

    /**
     * 记录 usage（当前以 log 形式输出，后续可扩展写入 Mongo / 计费系统）
     */
    private void recordUsage(@Nullable AiUsageContext context, String provider, String modelId,
                             Duration latency, @Nullable String error, @Nullable ChatResponse response) {
        AiUsageRecord.AiUsageRecordBuilder builder = AiUsageRecord.builder()
                .tenantId(context != null ? context.getTenantId() : TenantContextHolder.getTenantId())
                .customerId(context != null ? context.getCustomerId() : null)
                .module(context != null ? context.getModule() : null)
                .sessionId(context != null ? context.getSessionId() : null)
                .provider(provider)
                .modelId(modelId)
                .latencyMs(latency.toMillis())
                .error(error);

        // 从 Spring AI ChatResponseMetadata 中提取 token usage
        if (response != null && response.getMetadata() != null) {
            Usage usage = response.getMetadata().getUsage();
            if (usage != null) {
                builder.promptTokens(usage.getPromptTokens() != null
                        ? usage.getPromptTokens().intValue() : null)
                        .completionTokens(usage.getCompletionTokens() != null
                                ? usage.getCompletionTokens().intValue() : null)
                        .estimated(false);
            } else {
                // 拿不到 token：估算并标记
                builder.estimated(true);
            }
        }

        AiUsageRecord record = builder.build();
        log.info("[ModelGateway] usage: {}", record);
    }

}
