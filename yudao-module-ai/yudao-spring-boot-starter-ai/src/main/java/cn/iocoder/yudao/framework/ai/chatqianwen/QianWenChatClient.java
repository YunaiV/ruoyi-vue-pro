package cn.iocoder.yudao.framework.ai.chatqianwen;

import cn.iocoder.yudao.framework.ai.chat.*;
import cn.iocoder.yudao.framework.ai.chat.prompt.ChatOptions;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenApi;
import cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.chatyiyan.exception.YiYanApiException;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
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
 * 阿里 通义千问 client
 * <p>
 * 文档地址：https://help.aliyun.com/document_detail/2587494.html?spm=a2c4g.2587492.0.0.53f33c566sXskp
 * <p>
 * author: fansili
 * time: 2024/3/13 21:06
 */
@Slf4j
public class QianWenChatClient implements ChatClient, StreamingChatClient {

    private QianWenApi qianWenApi;

    private QianWenOptions qianWenOptions;


    public QianWenChatClient() {
    }

    public QianWenChatClient(QianWenApi qianWenApi) {
        this.qianWenApi = qianWenApi;
    }

    public QianWenChatClient(QianWenApi qianWenApi, QianWenOptions qianWenOptions) {
        this.qianWenApi = qianWenApi;
        this.qianWenOptions = qianWenOptions;
    }

    // TODO @fansili：看看咋公用出来，允许传入类似异常之类的参数；
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
            })
            .build();

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.retryTemplate.execute(ctx -> {
            // ctx 会有重试的信息
            // 创建 request 请求，stream模式需要供应商支持
            QianWenChatCompletionRequest request = this.createRequest(prompt, false);
            // 调用 callWithFunctionSupport 发送请求
            ResponseEntity<GenerationResult> responseEntity = qianWenApi.chatCompletionEntity(request);
            // 获取结果封装 chatCompletion
            GenerationResult response = responseEntity.getBody();
//            if (!response.isSuccess()) {
//                return new ChatResponse(List.of(new Generation(String.format("failed to create completion, requestId: %s, code: %s, message: %s\n",
//                        response.getRequestId(), response.getCode(), response.getMessage()))));
//            }
            // 转换为 Generation 返回
            return new ChatResponse(List.of(new Generation(response.getOutput().getText())));
        });
    }

    private QianWenChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        // 两个都为null 则没有配置文件
        if (qianWenOptions == null && prompt.getOptions() == null) {
            throw new ChatException("ChatOptions 未配置参数!");
        }
        // 优先使用 Prompt 里面的 ChatOptions
        ChatOptions options = qianWenOptions;
        if (prompt.getOptions() != null) {
            options = (ChatOptions) prompt.getOptions();
        }
        // Prompt 里面是一个 ChatOptions，用户可以随意传入，这里做一下判断
        if (!(options instanceof QianWenOptions qianWenOptions)) {
            throw new ChatException("Prompt 传入的不是 QianWenOptions!");
        }
        return (QianWenChatCompletionRequest) QianWenChatCompletionRequest.builder()
                .model(qianWenApi.getQianWenChatModal().getValue())
                .apiKey(qianWenApi.getApiKey())
                .messages(prompt.getInstructions().stream().map(m -> {
                    Message message = new Message();
                    message.setRole(m.getMessageType().getValue());
                    message.setContent(m.getContent());
                    return message;
                }).collect(Collectors.toList()))
                .resultFormat(QwenParam.ResultFormat.MESSAGE)
                // 动态改变的三个参数
                .topP(Double.valueOf(qianWenOptions.getTopP()))
                .topK(qianWenOptions.getTopK())
                .temperature(qianWenOptions.getTemperature())
                .incrementalOutput(true)
                .build();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // ctx 会有重试的信息
        // 创建 request 请求，stream模式需要供应商支持
        QianWenChatCompletionRequest request = this.createRequest(prompt, true);
        // 调用 callWithFunctionSupport 发送请求
        Flowable<GenerationResult> responseResult = this.qianWenApi.chatCompletionStream(request);
        return Flux.create(fluxSink ->
                responseResult.subscribe(
                        value -> fluxSink.next(new ChatResponse(List.of(new Generation(value.getOutput().getText())))),
                        error -> fluxSink.error(error),
                        () -> fluxSink.complete()
                )
        );
    }
}
