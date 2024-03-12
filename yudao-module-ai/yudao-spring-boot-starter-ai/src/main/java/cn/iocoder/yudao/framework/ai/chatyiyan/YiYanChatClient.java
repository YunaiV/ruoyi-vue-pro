package cn.iocoder.yudao.framework.ai.chatyiyan;

import cn.iocoder.yudao.framework.ai.chat.ChatClient;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.Generation;
import cn.iocoder.yudao.framework.ai.chat.StreamingChatClient;
import cn.iocoder.yudao.framework.ai.chat.messages.Message;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletion;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletionMessage;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.chatyiyan.exception.YiYanApiException;
import cn.iocoder.yudao.framework.ai.model.function.AbstractFunctionCallSupport;
import cn.iocoder.yudao.framework.ai.model.function.FunctionCallbackContext;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 文心一言
 *
 * author: fansili
 * time: 2024/3/8 19:11
 */
@Slf4j
public class YiYanChatClient
        extends AbstractFunctionCallSupport<YiYanChatCompletionMessage, YiYanChatCompletionRequest, ResponseEntity<YiYanChatCompletion>>
        implements ChatClient, StreamingChatClient {

    private YiYanApi yiYanApi;

    public YiYanChatClient(YiYanApi yiYanApi) {
        super(new FunctionCallbackContext());
        this.yiYanApi = yiYanApi;
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
            ResponseEntity<YiYanChatCompletion> response = this.callWithFunctionSupport(request);
            // 获取结果封装 ChatResponse
            YiYanChatCompletion chatCompletion = response.getBody();
            return new ChatResponse(List.of(new Generation(chatCompletion.getResult())));
        });
    }

    private YiYanChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        List<YiYanChatCompletionRequest.Message> messages = new ArrayList<>();
        List<Message> instructions = prompt.getInstructions();
        for (Message instruction : instructions) {
            YiYanChatCompletionRequest.Message message = new YiYanChatCompletionRequest.Message();
            message.setContent(instruction.getContent());
            message.setRole(instruction.getMessageType().getValue());
            messages.add(message);
        }
        YiYanChatCompletionRequest request = new YiYanChatCompletionRequest(messages);
        request.setStream(stream);
        return request;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // ctx 会有重试的信息
        // 创建 request 请求，stream模式需要供应商支持
        YiYanChatCompletionRequest request = this.createRequest(prompt, true);
        // 调用 callWithFunctionSupport 发送请求
        Flux<YiYanChatCompletion> response = this.yiYanApi.chatCompletionStream(request);
//        response.subscribe(new Consumer<YiYanChatCompletion>() {
//            @Override
//            public void accept(YiYanChatCompletion chatCompletion) {
//                // {"id":"as-p0nfjuuasg","object":"chat.completion","created":1710033402,"sentence_id":0,"is_end":false,"is_truncated":false,"result":"编程语","need_clear_history":false,"finish_reason":"normal","usage":{"prompt_tokens":5,"completion_tokens":0,"total_tokens":5}}
//                System.err.println(chatCompletion);
//            }
//        });
        return response.map(res -> {
            return new ChatResponse(List.of(new Generation(res.getResult())));
        });
    }

    @Override
    protected YiYanChatCompletionRequest doCreateToolResponseRequest(YiYanChatCompletionRequest previousRequest, YiYanChatCompletionMessage responseMessage, List<YiYanChatCompletionMessage> conversationHistory) {
        return null;
    }

    @Override
    protected List<YiYanChatCompletionMessage> doGetUserMessages(YiYanChatCompletionRequest request) {
        return null;
    }

    @Override
    protected YiYanChatCompletionMessage doGetToolResponseMessage(ResponseEntity<YiYanChatCompletion> response) {
        return null;
    }

    @Override
    protected ResponseEntity<YiYanChatCompletion> doChatCompletion(YiYanChatCompletionRequest request) {
        return yiYanApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<YiYanChatCompletion> response) {
        return false;
    }
}
