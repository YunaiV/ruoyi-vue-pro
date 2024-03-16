package cn.iocoder.yudao.framework.ai.chatqianwen;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.ai.chat.*;
import cn.iocoder.yudao.framework.ai.chat.messages.MessageType;
import cn.iocoder.yudao.framework.ai.chat.prompt.ChatOptions;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanOptions;
import cn.iocoder.yudao.framework.ai.chatyiyan.exception.YiYanApiException;
import com.aliyun.broadscope.bailian.sdk.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
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
public class QianWenChatClient  implements ChatClient, StreamingChatClient {

    private QianWenApi qianWenApi;

    private QianWenOptions qianWenOptions;

    public QianWenChatClient(QianWenApi qianWenApi) {
        this.qianWenApi = qianWenApi;
    }

    public QianWenChatClient(QianWenApi qianWenApi, QianWenOptions qianWenOptions) {
        this.qianWenApi = qianWenApi;
        this.qianWenOptions = qianWenOptions;
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
    public ChatResponse call(Prompt prompt) {
        return this.retryTemplate.execute(ctx -> {
            // ctx 会有重试的信息
            // 创建 request 请求，stream模式需要供应商支持
            CompletionsRequest request = this.createRequest(prompt, false);
            // 调用 callWithFunctionSupport 发送请求
            ResponseEntity<CompletionsResponse> responseEntity = qianWenApi.chatCompletionEntity(request);
            // 获取结果封装 chatCompletion
            CompletionsResponse response = responseEntity.getBody();
            if (!response.isSuccess()) {
                return new ChatResponse(List.of(new Generation(String.format("failed to create completion, requestId: %s, code: %s, message: %s\n",
                        response.getRequestId(), response.getCode(), response.getMessage()))));
            }
            // 转换为 Generation 返回
            return new ChatResponse(List.of(new Generation(response.getData().getText())));
        });
    }

    private CompletionsRequest createRequest(Prompt prompt, boolean stream) {
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
        if (!(options instanceof QianWenOptions)) {
            throw new ChatException("Prompt 传入的不是 QianWenOptions!");
        }
        QianWenOptions qianWenOptions = (QianWenOptions) options;
        // 需要额外处理
        if (!stream) {
            // 如果不需要 stream 输出，那么需要将这个设置为false，不然只会输出最后几个文字
            if (qianWenOptions.getParameters() == null) {
                qianWenOptions.setParameters(new CompletionsRequest.Parameter().setIncrementalOutput(false));
            } else {
                qianWenOptions.getParameters().setIncrementalOutput(false);
            }
        } else {
            // 如果不需要 stream 输出，设置为true这样不会输出累加内容
            if (qianWenOptions.getParameters() == null) {
                qianWenOptions.setParameters(new CompletionsRequest.Parameter().setIncrementalOutput(true));
            } else {
                qianWenOptions.getParameters().setIncrementalOutput(true);
            }
        }

        // 创建request
        return new CompletionsRequest()
                // 请求唯一标识，请确保RequestId不重复。
                .setRequestId(IdUtil.getSnowflakeNextIdStr())
                // 设置 appid
                .setAppId(qianWenOptions.getAppId())
                .setMessages(prompt.getInstructions().stream().map(m -> {
                    // 转换成 千问 对于的请求message
                    if (MessageType.USER == m.getMessageType()) {
                        return new ChatUserMessage(m.getContent());
                    } else if (MessageType.SYSTEM == m.getMessageType()) {
                        return new ChatSystemMessage(m.getContent());
                    } else if (MessageType.ASSISTANT == m.getMessageType()) {
                        return new ChatAssistantMessage(m.getContent());
                    }
                    throw new ChatException(String.format("存在不能适配的消息! %s", JSONUtil.toJsonPrettyStr(m)));
                }).collect(Collectors.toList()))
                // 返回choice message结果
                .setParameters(qianWenOptions.getParameters())
                // 设置 ChatOptions 里面公共的参数
                .setTopP(options.getTopP() == null ? null : options.getTopP().doubleValue())
                // 设置输出方式
                .setStream(stream);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // ctx 会有重试的信息
        // 创建 request 请求，stream模式需要供应商支持
        CompletionsRequest request = this.createRequest(prompt, true);
        // 调用 callWithFunctionSupport 发送请求
        Flux<CompletionsResponse> response = this.qianWenApi.chatCompletionStream(request);
        return response.map(res -> {
            return new ChatResponse(List.of(new Generation(res.getData().getText())));
        });
    }
}
