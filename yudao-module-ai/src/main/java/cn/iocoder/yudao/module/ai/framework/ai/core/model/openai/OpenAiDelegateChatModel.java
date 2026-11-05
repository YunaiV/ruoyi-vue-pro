package cn.iocoder.yudao.module.ai.framework.ai.core.model.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 复用 {@link OpenAiChatModel} 的 {@link ChatModel} 实现类
 *
 * OpenAI、Azure OpenAI，以及 Grok、通义等兼容 OpenAI 接口的平台，都经过本类调用 {@link OpenAiChatModel}
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class OpenAiDelegateChatModel implements ChatModel {

    /**
     * {@link OpenAiChatModel} 写入 AssistantMessage metadata 的思考内容 KEY；流式时为本次调用已收到的累计值
     */
    public static final String REASONING_CONTENT = "reasoningContent";
    /**
     * 流式时，本次 chunk 新增的思考内容 KEY，由 {@link #stream(Prompt)} 写入
     */
    public static final String REASONING_CONTENT_DELTA = "reasoningContentDelta";

    /**
     * 被复用的 {@link OpenAiChatModel}
     */
    private final ChatModel openAiChatModel;

    @Override
    public ChatResponse call(Prompt prompt) {
        return openAiChatModel.call(prompt);
    }

    /**
     * 流式调用
     *
     * 特殊：Spring AI 2.x 的 {@link OpenAiChatModel} 流式时，把每个 chunk 的思考内容累加后写入 {@link #REASONING_CONTENT}，
     * 业务按 chunk 增量拼接会重复。这里按「本次累计 - 上次累计」计算增量，写入 {@link #REASONING_CONTENT_DELTA}，
     * 原累计值保持不变，供 ChatClient 工具调用聚合、续调用回传使用
     */
    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // 每次调用（含 ChatClient 工具调用的每一轮）独立记录累计值，不放在实例字段，避免缓存的 ChatModel 被并发请求串用
        return Flux.defer(() -> {
            Map<String, String> lastReasoningContents = new HashMap<>();
            return openAiChatModel.stream(prompt).map(response -> buildReasoningContentDelta(response, lastReasoningContents));
        });
    }

    /**
     * 为 chunk 的每个 generation 写入思考内容的增量
     *
     * @param response              流式 chunk
     * @param lastReasoningContents 本次调用中，各 generation 上次的累计思考内容，KEY 与 {@link OpenAiChatModel} 一致为 id:index
     * @return 写入增量后的 chunk；没有思考内容时原样返回
     */
    static ChatResponse buildReasoningContentDelta(ChatResponse response, Map<String, String> lastReasoningContents) {
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            return response;
        }
        List<Generation> generations = new ArrayList<>(response.getResults().size());
        boolean changed = false;
        for (int i = 0; i < response.getResults().size(); i++) {
            Generation generation = response.getResults().get(i);
            AssistantMessage output = generation.getOutput();
            Object reasoningContent = output.getMetadata().get(REASONING_CONTENT);
            if (!(reasoningContent instanceof String)) {
                generations.add(generation);
                continue;
            }
            // 1. 计算增量：累计值以上次累计为前缀时取新增部分；否则（不应出现）整段作为增量，不丢内容
            String key = output.getMetadata().get("id") + ":" + Objects.requireNonNullElse(output.getMetadata().get("index"), i);
            String current = (String) reasoningContent;
            String last = lastReasoningContents.getOrDefault(key, "");
            String delta = current.startsWith(last) ? current.substring(last.length()) : current;
            lastReasoningContents.put(key, current);
            // 2. 写入增量，其余内容不变
            Map<String, Object> metadata = new HashMap<>(output.getMetadata());
            metadata.put(REASONING_CONTENT_DELTA, delta);
            generations.add(new Generation(output.mutate().properties(metadata).build(), generation.getMetadata()));
            changed = true;
        }
        return changed ? ChatResponse.builder().from(response).generations(generations).build() : response;
    }

    @Override
    public ChatOptions getOptions() {
        return openAiChatModel.getOptions();
    }

    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public ChatOptions getDefaultOptions() {
        return getOptions();
    }

}
