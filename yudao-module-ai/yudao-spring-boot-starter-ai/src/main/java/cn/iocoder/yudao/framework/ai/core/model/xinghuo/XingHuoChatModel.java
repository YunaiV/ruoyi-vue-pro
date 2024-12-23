package cn.iocoder.yudao.framework.ai.core.model.xinghuo;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.metadata.OpenAiChatResponseMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatOptions.MODEL_DEFAULT;

/**
 * 讯飞星火 {@link ChatModel} 实现类
 *
 * @author fansili
 */
@Slf4j
public class XingHuoChatModel implements ChatModel {

    private static final String BASE_URL = "https://spark-api-open.xf-yun.com";

    private final XingHuoChatOptions defaultOptions;
    private final RetryTemplate retryTemplate;

    /**
     * 星火兼容 OpenAI 的 HTTP 接口，所以复用它的实现，简化接入成本
     *
     * 不过要注意，星火没有完全兼容，所以不能使用 {@link org.springframework.ai.openai.OpenAiChatModel} 调用，但是实现会参考它
     */
    private final OpenAiApi openAiApi;

    public XingHuoChatModel(String apiKey, String secretKey) {
        this(apiKey, secretKey,
                XingHuoChatOptions.builder().model(MODEL_DEFAULT).temperature(0.7F).build());
    }

    public XingHuoChatModel(String apiKey, String secretKey, XingHuoChatOptions options) {
       this(apiKey, secretKey, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public XingHuoChatModel(String apiKey, String secretKey, XingHuoChatOptions options, RetryTemplate retryTemplate) {
        Assert.notEmpty(apiKey, "apiKey 不能为空");
        Assert.notEmpty(secretKey, "secretKey 不能为空");
        Assert.notNull(options, "options 不能为空");
        Assert.notNull(retryTemplate, "retryTemplate 不能为空");
        this.openAiApi = new OpenAiApi(BASE_URL, apiKey + ":" + secretKey);
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        OpenAiApi.ChatCompletionRequest request = createRequest(prompt, false);
        return this.retryTemplate.execute(ctx -> {
            // 1.1 发起调用
            ResponseEntity<OpenAiApi.ChatCompletion> completionEntity = openAiApi.chatCompletionEntity(request);
            // 1.2 校验结果
            OpenAiApi.ChatCompletion chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(ListUtil.of());
            }
            List<OpenAiApi.ChatCompletion.Choice> choices = chatCompletion.choices();
            if (choices == null) {
                log.warn("No choices returned for prompt: {}", prompt);
                return new ChatResponse(ListUtil.of());
            }

            // 2. 转换 ChatResponse 返回
            List<Generation> generations = choices.stream().map(choice -> {
                Generation generation = new Generation(choice.message().content(), toMap(chatCompletion.id(), choice));
                if (choice.finishReason() != null) {
                    generation.withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                }
                return generation;
            }).toList();
            return new ChatResponse(generations,
                    OpenAiChatResponseMetadata.from(completionEntity.getBody()));
        });
    }

    private Map<String, Object> toMap(String id, OpenAiApi.ChatCompletion.Choice choice) {
        Map<String, Object> map = new HashMap<>();
        OpenAiApi.ChatCompletionMessage message = choice.message();
        if (message.role() != null) {
            map.put("role", message.role().name());
        }
        if (choice.finishReason() != null) {
            map.put("finishReason", choice.finishReason().name());
        }
        map.put("id", id);
        return map;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        OpenAiApi.ChatCompletionRequest request = createRequest(prompt, true);
        return this.retryTemplate.execute(ctx -> {
            // 1. 发起调用
            Flux<OpenAiApi.ChatCompletionChunk> response = this.openAiApi.chatCompletionStream(request);
            return response.map(chatCompletion -> {
                String id = chatCompletion.id();
                // 2. 转换 ChatResponse 返回
                List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                    String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                    Generation generation = new Generation(choice.delta().content(),
                            Map.of("id", id, "role", choice.delta().role().name(), "finishReason", finish));
                    if (choice.finishReason() != null) {
                        generation = generation.withGenerationMetadata(
                                ChatGenerationMetadata.from(choice.finishReason().name(), null));
                    }
                    return generation;
                }).toList();
                return new ChatResponse(generations);
            });
        });
    }

    OpenAiApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        // 1. 构建 ChatCompletionMessage 对象
        List<OpenAiApi.ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().map(m ->
                new OpenAiApi.ChatCompletionMessage(m.getContent(), OpenAiApi.ChatCompletionMessage.Role.valueOf(m.getMessageType().name()))).toList();
        OpenAiApi.ChatCompletionRequest request = new OpenAiApi.ChatCompletionRequest(chatCompletionMessages, stream);

        // 2.1 补充 prompt 内置的 options
        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                OpenAiChatOptions updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions,
                        ChatOptions.class, OpenAiChatOptions.class);
                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, OpenAiApi.ChatCompletionRequest.class);
            } else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }
        // 2.2 补充默认 options
        if (this.defaultOptions != null) {
            request = ModelOptionsUtils.merge(request, this.defaultOptions, OpenAiApi.ChatCompletionRequest.class);
        }
        return request;
    }

    @Override
    public ChatOptions getDefaultOptions() {
         return XingHuoChatOptions.fromOptions(defaultOptions);
    }

}
