package cn.iocoder.yudao.framework.ai.chatxinghuo;

import cn.iocoder.yudao.framework.ai.chat.ChatClient;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.Generation;
import cn.iocoder.yudao.framework.ai.chat.StreamingChatClient;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoChatCompletion;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoChatCompletionMessage;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.chatxinghuo.exception.XingHuoApiException;
import cn.iocoder.yudao.framework.ai.model.function.AbstractFunctionCallSupport;
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
 * 讯飞星火 client
 * <p>
 * author: fansili
 * time: 2024/3/11 10:19
 */
@Slf4j
public class XingHuoChatClient extends AbstractFunctionCallSupport<XingHuoChatCompletionMessage, XingHuoChatCompletionRequest, ResponseEntity<XingHuoChatCompletion>>
        implements ChatClient, StreamingChatClient {

    private XingHuoApi xingHuoApi;

    public final RetryTemplate retryTemplate = RetryTemplate.builder()
            // 最大重试次数 10
            .maxAttempts(10)
            .retryOn(XingHuoApiException.class)
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

    public XingHuoChatClient(XingHuoApi xingHuoApi) {
        super(null);
        this.xingHuoApi = xingHuoApi;
    }

    @Override
    public ChatResponse call(Prompt prompt) {

        return this.retryTemplate.execute(ctx -> {
            // ctx 会有重试的信息
            // 创建 request 请求，stream模式需要供应商支持
            XingHuoChatCompletionRequest request = this.createRequest(prompt, false);
            // 调用 callWithFunctionSupport 发送请求
            ResponseEntity<XingHuoChatCompletion> response = this.callWithFunctionSupport(request);
            // 获取结果封装 ChatResponse
            return new ChatResponse(List.of(new Generation(response.getBody().getPayload().getChoices().getText().get(0).getContent())));
        });
    }

    private XingHuoChatCompletionRequest createRequest(Prompt prompt, boolean b) {
        // 创建 header
        XingHuoChatCompletionRequest.Header header = new XingHuoChatCompletionRequest.Header().setApp_id(xingHuoApi.getAppId());
        // 创建 params
        XingHuoChatCompletionRequest.Parameter parameter = new XingHuoChatCompletionRequest.Parameter()
                .setChat(new XingHuoChatCompletionRequest.Parameter.Chat().setDomain(xingHuoApi.getUseChatModel().getValue()));
        // 创建 payload text 信息
        XingHuoChatCompletionRequest.Payload.Message.Text text = new XingHuoChatCompletionRequest.Payload.Message.Text();
        text.setRole(XingHuoChatCompletionRequest.Payload.Message.Text.Role.USER.getName());
        text.setContent(prompt.getContents());
        // 创建 payload
        XingHuoChatCompletionRequest.Payload payload = new XingHuoChatCompletionRequest.Payload()
                .setMessage(new XingHuoChatCompletionRequest.Payload.Message().setText(List.of(text)));
        // 创建 request
        return new XingHuoChatCompletionRequest()
                .setHeader(header)
                .setParameter(parameter)
                .setPayload(payload);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // 创建 request 请求，stream模式需要供应商支持
        XingHuoChatCompletionRequest request = this.createRequest(prompt, false);
        // 发送请求
        Flux<XingHuoChatCompletion> response = this.xingHuoApi.chatCompletionStream(request);
        return response.map(res -> {
            String content = res.getPayload().getChoices().getText().stream()
                    .map(item -> item.getContent()).collect(Collectors.joining());
            return new ChatResponse(List.of(new Generation(content)));
        });
    }

    @Override
    protected XingHuoChatCompletionRequest doCreateToolResponseRequest(XingHuoChatCompletionRequest previousRequest, XingHuoChatCompletionMessage responseMessage, List<XingHuoChatCompletionMessage> conversationHistory) {
        return null;
    }

    @Override
    protected List<XingHuoChatCompletionMessage> doGetUserMessages(XingHuoChatCompletionRequest request) {
        return null;
    }

    @Override
    protected XingHuoChatCompletionMessage doGetToolResponseMessage(ResponseEntity<XingHuoChatCompletion> response) {
        return null;
    }

    @Override
    protected ResponseEntity<XingHuoChatCompletion> doChatCompletion(XingHuoChatCompletionRequest request) {
        return xingHuoApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<XingHuoChatCompletion> response) {
        return false;
    }
}
