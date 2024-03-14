package cn.iocoder.yudao.framework.ai.chatqianwen;

import cn.iocoder.yudao.framework.ai.chat.ChatClient;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.Generation;
import cn.iocoder.yudao.framework.ai.chat.StreamingChatClient;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionMessage;
import cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletion;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.chatyiyan.exception.YiYanApiException;
import cn.iocoder.yudao.framework.ai.model.function.AbstractFunctionCallSupport;
import cn.iocoder.yudao.framework.ai.model.function.FunctionCallbackContext;
import com.aliyun.broadscope.bailian.sdk.models.ChatRequestMessage;
import com.aliyun.broadscope.bailian.sdk.models.ChatUserMessage;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsResponse;
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
 *
 * 文档地址：https://help.aliyun.com/document_detail/2587494.html?spm=a2c4g.2587492.0.0.53f33c566sXskp
 *
 * author: fansili
 * time: 2024/3/13 21:06
 */
@Slf4j
public class QianWenChatClient extends AbstractFunctionCallSupport<QianWenChatCompletionMessage, ChatRequestMessage, ResponseEntity<CompletionsResponse>>
        implements ChatClient, StreamingChatClient {

    private QianWenApi qianWenApi;

    public QianWenChatClient(QianWenApi qianWenApi) {
        super(null);
        this.qianWenApi = qianWenApi;
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
                };
            })
            .build();

    public QianWenChatClient(FunctionCallbackContext functionCallbackContext) {
        super(functionCallbackContext);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.retryTemplate.execute(ctx -> {
            // ctx 会有重试的信息
            // 创建 request 请求，stream模式需要供应商支持
            ChatRequestMessage request = this.createRequest(prompt, false);
            // 调用 callWithFunctionSupport 发送请求
            ResponseEntity<CompletionsResponse> responseEntity = this.callWithFunctionSupport(request);
            // 获取结果封装 chatCompletion
            CompletionsResponse response = responseEntity.getBody();
            if (!response.isSuccess()) {
                return new ChatResponse(List.of(new Generation(String.format("failed to create completion, requestId: %s, code: %s, message: %s\n",
                        response.getRequestId(), response.getCode(), response.getMessage()))));
            }
            List<Generation> generations = response.getData().getChoices().stream()
                    .map(item -> new Generation(item.getMessage().getContent())).collect(Collectors.toList());
            return new ChatResponse(generations);
        });
    }

    private ChatRequestMessage createRequest(Prompt prompt, boolean b) {
        return new ChatUserMessage(prompt.getContents());
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // ctx 会有重试的信息
        // 创建 request 请求，stream模式需要供应商支持
        ChatRequestMessage request = this.createRequest(prompt, true);
        // 调用 callWithFunctionSupport 发送请求
        Flux<CompletionsResponse> response = this.qianWenApi.chatCompletionStream(request);
        return response.map(res -> {
            return new ChatResponse(List.of(new Generation(res.getData().getText())));
        });
    }

    @Override
    protected QianWenChatCompletionRequest doCreateToolResponseRequest(ChatRequestMessage previousRequest, QianWenChatCompletionMessage responseMessage, List<QianWenChatCompletionMessage> conversationHistory) {
        return null;
    }

    @Override
    protected List<QianWenChatCompletionMessage> doGetUserMessages(ChatRequestMessage request) {
        return null;
    }

    @Override
    protected QianWenChatCompletionMessage doGetToolResponseMessage(ResponseEntity<CompletionsResponse> response) {
        return null;
    }

    @Override
    protected ResponseEntity<CompletionsResponse> doChatCompletion(ChatRequestMessage request) {
        return qianWenApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<CompletionsResponse> response) {
        return false;
    }
}
