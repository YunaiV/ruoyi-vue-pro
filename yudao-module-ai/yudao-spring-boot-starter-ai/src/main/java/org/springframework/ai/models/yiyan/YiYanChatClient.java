package org.springframework.ai.models.yiyan;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.ai.chat.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.models.yiyan.api.YiYanApi;
import org.springframework.ai.models.yiyan.api.YiYanChatCompletion;
import org.springframework.ai.models.yiyan.api.YiYanChatCompletionRequest;
import org.springframework.ai.models.yiyan.exception.YiYanApiException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文心一言
 * <p>
 * author: fansili
 * time: 2024/3/8 19:11
 */
@Slf4j
public class YiYanChatClient implements ChatClient, StreamingChatClient {

    private YiYanApi yiYanApi;

    private YiYanOptions yiYanOptions;

    public YiYanChatClient(YiYanApi yiYanApi) {
        this.yiYanApi = yiYanApi;
    }

    public YiYanChatClient(YiYanApi yiYanApi, YiYanOptions yiYanOptions) {
        this.yiYanApi = yiYanApi;
        this.yiYanOptions = yiYanOptions;
    }

    public final RetryTemplate retryTemplate = RetryTemplate.builder()
            // 最大重试次数 10
            .maxAttempts(10)
            .retryOn(YiYanApiException.class)
            // 最大重试5次，第一次间隔3000ms，第二次3000ms * 2，第三次3000ms * 3，以此类推，最大间隔3 * 60000ms
            .exponentialBackoff(Duration.ofMillis(3000), 2, Duration.ofMillis(3 * 60000))
            .withListener(new RetryListener() {
                @Override
                public <T extends Object, E extends Throwable> void onError(RetryContext context,
                                                                            RetryCallback<T, E> callback, Throwable throwable) {
                    log.warn("重试异常:" + context.getRetryCount(), throwable);
                }

                ;
            })
            .build();

    @Override
    public String call(String message) {
        return ChatClient.super.call(message);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.retryTemplate.execute(ctx -> {
            // ctx 会有重试的信息
            // 创建 request 请求，stream模式需要供应商支持
            YiYanChatCompletionRequest request = this.createRequest(prompt, false);
            // 调用 callWithFunctionSupport 发送请求
            ResponseEntity<YiYanChatCompletion> response = yiYanApi.chatCompletionEntity(request);
            // 获取结果封装 ChatResponse
            YiYanChatCompletion chatCompletion = response.getBody();
            return new ChatResponse(List.of(new Generation(chatCompletion.getResult())));
        });
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // ctx 会有重试的信息
        // 创建 request 请求，stream模式需要供应商支持
        YiYanChatCompletionRequest request = this.createRequest(prompt, true);
        // 调用 callWithFunctionSupport 发送请求
        Flux<YiYanChatCompletion> response = this.yiYanApi.chatCompletionStream(request);
        response.doOnComplete(new Runnable() {
            @Override
            public void run() {
                String a = ";";
            }
        });
        return response.map(res -> {
            // TODO @fan：这里缺少了 usage 的封装
            return new ChatResponse(List.of(new Generation(res.getResult())));
        });
    }

    private YiYanChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        // 获取配置
        YiYanOptions useOptions = getYiYanOptions(prompt);
        // 创建 request

        // tip: 百度的 system 不在 message 里面
        // tip：百度的 message 只有 user 和 assistant
        // https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t

        // 获取 user 和 assistant
        List<YiYanChatCompletionRequest.Message> messageList = prompt.getInstructions().stream()
                // 过滤 system
                .filter(msg -> MessageType.SYSTEM != msg.getMessageType())
                .map(msg -> new YiYanChatCompletionRequest.Message()
                        .setRole(msg.getMessageType().getValue())
                        .setContent(msg.getContent())
                ).toList();
        // 获取 system
        String systemPrompt = prompt.getInstructions().stream()
                .filter(msg -> MessageType.SYSTEM == msg.getMessageType())
                .map(Message::getContent)
                .collect(Collectors.joining());

        YiYanChatCompletionRequest request = new YiYanChatCompletionRequest(messageList);
        // 复制 qianWenOptions 属性取 request（这里 options 属性和 request 基本保持一致）
        // top: 由于遵循 spring-ai规范，支持在构建client的时候传入默认的 chatOptions
        BeanUtil.copyProperties(useOptions, request);
        request.setTop_p(useOptions.getTopP());
        request.setMax_output_tokens(useOptions.getMaxOutputTokens());
        request.setTemperature(useOptions.getTemperature());
        request.setSystem(systemPrompt);
        // 设置 stream
        request.setStream(stream);
        return request;
    }

    private @NotNull YiYanOptions getYiYanOptions(Prompt prompt) {
        // 两个都为null 则没有配置文件
        if (yiYanOptions == null && prompt.getOptions() == null) {
            throw new ChatException("ChatOptions 未配置参数!");
        }
        // 优先使用 Prompt 里面的 ChatOptions
        ChatOptions options = yiYanOptions;
        if (prompt.getOptions() != null) {
            options = (ChatOptions) prompt.getOptions();
        }
        // Prompt 里面是一个 ChatOptions，用户可以随意传入，这里做一下判断
        if (!(options instanceof YiYanOptions)) {
            throw new ChatException("Prompt 传入的不是 YiYanOptions!");
        }
        // 转换 YiYanOptions
        YiYanOptions useOptions = (YiYanOptions) options;
        return useOptions;
    }
}
