package cn.iocoder.yudao.framework.ai.core.model.yiyan;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.ai.core.exception.ChatException;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanApi;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanChatCompletionResponse;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.exception.YiYanApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文心一言的 {@link ChatClient} 实现类
 *
 * @author fansili
 */
@Slf4j
public class YiYanChatClient implements ChatClient, StreamingChatClient {

    private final YiYanApi yiYanApi;

    private YiYanChatOptions defaultOptions;

    // TODO @fan：参考 OpenAiChatClient 调整下 retryTemplate；使用 RetryUtils.DEFAULT_RETRY_TEMPLATE；加允许传入？

    public YiYanChatClient(YiYanApi yiYanApi) {
        this.yiYanApi = yiYanApi;
        // TODO @fan：这个情况，是不是搞个 defaultOptions；OpenAiChatOptions.builder().withModel(OpenAiApi.DEFAULT_CHAT_MODEL).withTemperature(0.7f).build()
    }

    public YiYanChatClient(YiYanApi yiYanApi, YiYanChatOptions defaultOptions) {
        Assert.notNull(yiYanApi, "OllamaApi must not be null");
        Assert.notNull(defaultOptions, "DefaultOptions must not be null");
        this.yiYanApi = yiYanApi;
        this.defaultOptions = defaultOptions;
    }

    public final RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(10)
            .retryOn(YiYanApiException.class)
            .exponentialBackoff(Duration.ofMillis(3000), 2, Duration.ofMillis(3 * 60000))
            .withListener(new RetryListener() {

                @Override
                public <T, E extends Throwable> void onError(RetryContext context,
                                                             RetryCallback<T, E> callback, Throwable throwable) {
                    log.warn("重试异常:" + context.getRetryCount(), throwable);
                }

            })
            .build();

    @Override
    public ChatResponse call(Prompt prompt) {
        YiYanChatCompletionRequest request = createRequest(prompt, false);
        return this.retryTemplate.execute(ctx -> {
            // 发送请求
            ResponseEntity<YiYanChatCompletionResponse> response = yiYanApi.chatCompletionEntity(request);
            // 获取结果封装 ChatResponse
            YiYanChatCompletionResponse chatCompletion = response.getBody();
            // TODO @fan：为空时，参考 OpenAiChatClient 的封装；
            // TODO @fan：chatResponseMetadata，参考 OpenAiChatResponseMetadata.from(completionEntity.getBody())
            return new ChatResponse(List.of(new Generation(chatCompletion.getResult())));
        });
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        YiYanChatCompletionRequest request = this.createRequest(prompt, true);
        // TODO done @fan：return this.retryTemplate.execute(ctx -> {
        return retryTemplate.execute(ctx -> {
            // 调用 callWithFunctionSupport 发送请求
            Flux<YiYanChatCompletionResponse> response = this.yiYanApi.chatCompletionStream(request);
            return response.map(chunk -> {
//                System.err.println("---".concat(chunk.getResult()));
                // TODO @fan：ChatResponseMetadata chatResponseMetadata
                return new ChatResponse(List.of(new Generation(chunk.getResult())));
            });
        });
    }



    private YiYanChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        // 参考 https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t 文档，system 是独立字段
        // 1.1 获取 user 和 assistant
        List<YiYanChatCompletionRequest.Message> messageList = prompt.getInstructions().stream()
                // 过滤 system
                .filter(msg -> MessageType.SYSTEM != msg.getMessageType())
                .map(message -> new YiYanChatCompletionRequest.Message()
                        .setRole(message.getMessageType().getValue()).setContent(message.getContent())
                ).toList();
        // 1.2 获取 system
        String systemPrompt = prompt.getInstructions().stream()
                .filter(message -> MessageType.SYSTEM == message.getMessageType())
                .map(Message::getContent)
                .collect(Collectors.joining());

        // 3. 创建 request
        YiYanChatCompletionRequest request = new YiYanChatCompletionRequest(messageList);
        // 复制 YiYanOptions 属性，到 request 中（这里 options 属性和 request 基本保持一致）
        YiYanChatOptions useOptions = getYiYanOptions(prompt);
        BeanUtil.copyProperties(useOptions, request);
        request.setTopP(useOptions.getTopP())
                .setMaxOutputTokens(useOptions.getMaxOutputTokens())
                .setTemperature(useOptions.getTemperature())
                .setSystem(systemPrompt)
                .setStream(stream);
        return request;
    }

    // TODO @fan：Options 的处理，参考下 OpenAiChatClient 的 createRequest
    private YiYanChatOptions getYiYanOptions(Prompt prompt) {
        // 两个都为null 则没有配置文件
        if (defaultOptions == null && prompt.getOptions() == null) {
            // TODO @fan：IllegalArgumentException 参数更好哈
            throw new ChatException("ChatOptions 未配置参数!");
        }
        // 优先使用 Prompt 里面的 ChatOptions
        ChatOptions options = defaultOptions;
        if (prompt.getOptions() != null) {
            options = (ChatOptions) prompt.getOptions();
        }
        // Prompt 里面是一个 ChatOptions，用户可以随意传入，这里做一下判断
        if (!(options instanceof YiYanChatOptions)) {
            // TODO @fan：IllegalArgumentException 参数更好哈
            // TODO @fan：需要兼容 ChatOptionsBuilder 创建出来的
            throw new ChatException("Prompt 传入的不是 YiYanOptions!");
        }
        // 转换 YiYanOptions
        return (YiYanChatOptions) options;
    }

}
