package cn.iocoder.yudao.framework.ai.core.model.xinghuo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.ai.core.exception.ChatException;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoChatCompletion;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoChatCompletionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

// TODO @fan：参考 yiyan 的修改建议，调整下 xinghuo 的实现；可以等 yiyan 修改完建议，然后我 review 完，再改这个哈；
/**
 * 讯飞星火 client
 * <p>
 * author: fansili
 * time: 2024/3/11 10:19
 */
@Slf4j
public class XingHuoChatClient implements ChatModel, StreamingChatModel {

    private XingHuoApi xingHuoApi;

    private XingHuoOptions xingHuoOptions;

    public final RetryTemplate retryTemplate = RetryTemplate.builder()
            // 最大重试次数 10
            .maxAttempts(3)
            .retryOn(ChatException.class)
            // 最大重试5次，第一次间隔3000ms，第二次3000ms * 2，第三次3000ms * 3，以此类推，最大间隔3 * 60000ms
            .exponentialBackoff(Duration.ofMillis(3000), 2, Duration.ofMillis(3 * 60000))
            .withListener(new RetryListener() {
                @Override
                public <T extends Object, E extends Throwable> void onError(RetryContext context,
                                                                            RetryCallback<T, E> callback, Throwable throwable) {
                    System.err.println("正在重试... " + ExceptionUtil.getMessage(throwable));
                    log.warn("重试异常:" + context.getRetryCount(), throwable);
                }

                ;
            })
            .build();

    public XingHuoChatClient(XingHuoApi xingHuoApi) {
        this.xingHuoApi = xingHuoApi;
    }

    public XingHuoChatClient(XingHuoApi xingHuoApi, XingHuoOptions xingHuoOptions) {
        this.xingHuoApi = xingHuoApi;
        this.xingHuoOptions = xingHuoOptions;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.retryTemplate.execute(ctx -> {
            // ctx 会有重试的信息
            // 获取 chatOptions 属性
            XingHuoOptions chatOptions = this.getChatOptions(prompt);
            // 创建 request 请求，stream模式需要供应商支持
            XingHuoChatCompletionRequest request = this.createRequest(prompt, chatOptions);
            // 调用 callWithFunctionSupport 发送请求
            ResponseEntity<XingHuoChatCompletion> response = xingHuoApi.chatCompletionEntity(request, chatOptions.getChatModel());
            // 获取结果封装 ChatResponse
            return new ChatResponse(List.of(new Generation(response.getBody().getPayload().getChoices().getText().get(0).getContent())));
        });
    }

    @Override
    public ChatOptions getDefaultOptions() {
        // TODO 芋艿：需要跟进下
        throw new UnsupportedOperationException();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // 获取 chatOptions 属性
        XingHuoOptions chatOptions = this.getChatOptions(prompt);
        // 创建 request 请求，stream模式需要供应商支持
        XingHuoChatCompletionRequest request = this.createRequest(prompt, chatOptions);
        // 发送请求
        Flux<XingHuoChatCompletion> response = this.xingHuoApi.chatCompletionStream(request, chatOptions.getChatModel());
        return response.map(res -> {
            String content = res.getPayload().getChoices().getText().stream()
                    .map(item -> item.getContent()).collect(Collectors.joining());
            return new ChatResponse(List.of(new Generation(content)));
        });
    }

    private XingHuoOptions getChatOptions(Prompt prompt) {
        // 两个都为null 则没有配置文件
        if (xingHuoOptions == null && prompt.getOptions() == null) {
            throw new ChatException("ChatOptions 未配置参数!");
        }
        // 优先使用 Prompt 里面的 ChatOptions
        ChatOptions options = xingHuoOptions;
        if (prompt.getOptions() != null) {
            options = (ChatOptions) prompt.getOptions();
        }
        // Prompt 里面是一个 ChatOptions，用户可以随意传入，这里做一下判断
        if (!(options instanceof XingHuoOptions)) {
            throw new ChatException("Prompt 传入的不是 XingHuoOptions!");
        }
        return (XingHuoOptions) options;
    }

    private XingHuoChatCompletionRequest createRequest(Prompt prompt, XingHuoOptions xingHuoOptions) {
        // 创建 header
        XingHuoChatCompletionRequest.Header header = new XingHuoChatCompletionRequest.Header().setApp_id(xingHuoApi.getAppId());
        // 创建 params
        XingHuoChatCompletionRequest.Parameter.Chat chatParameter = new XingHuoChatCompletionRequest.Parameter.Chat();
        BeanUtil.copyProperties(xingHuoOptions, chatParameter);
        chatParameter.setDomain(xingHuoOptions.getChatModel().getModel());
        XingHuoChatCompletionRequest.Parameter parameter = new XingHuoChatCompletionRequest.Parameter().setChat(chatParameter);
        // 创建 payload text 信息
        List<XingHuoChatCompletionRequest.Payload.Message.Text> texts = prompt.getInstructions().stream().map(message -> {
            XingHuoChatCompletionRequest.Payload.Message.Text text = new XingHuoChatCompletionRequest.Payload.Message.Text();
            text.setContent(message.getContent());
            text.setRole(message.getMessageType().getValue());
            return text;
        }).collect(Collectors.toList());
        // 创建 payload
        XingHuoChatCompletionRequest.Payload payload = new XingHuoChatCompletionRequest.Payload()
                .setMessage(new XingHuoChatCompletionRequest.Payload.Message().setText(texts));
        // 创建 request
        return new XingHuoChatCompletionRequest()
                .setHeader(header)
                .setParameter(parameter)
                .setPayload(payload);
    }
}
