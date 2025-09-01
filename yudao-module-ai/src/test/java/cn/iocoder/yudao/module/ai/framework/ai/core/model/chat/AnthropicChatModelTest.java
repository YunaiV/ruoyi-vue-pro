package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AnthropicChatModel} 集成测试类
 *
 * @author 芋道源码
 */
public class AnthropicChatModelTest {

    private final AnthropicChatModel chatModel = AnthropicChatModel.builder()
            .anthropicApi(AnthropicApi.builder()
                    .apiKey("sk-muubv7cXeLw0Etgs743f365cD5Ea44429946Fa7e672d8942")
                    .baseUrl("https://aihubmix.com")
                    .build())
            .defaultOptions(AnthropicChatOptions.builder()
                    .model(AnthropicApi.ChatModel.CLAUDE_SONNET_4)
                    .temperature(0.7)
                    .maxTokens(4096)
                    .build())
            .build();

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        ChatResponse response = chatModel.call(new Prompt(messages));
        // 打印结果
        System.out.println(response);
    }

    @Test
    @Disabled
    public void testStream() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(System.out::println).then().block();
    }

    // TODO @芋艿：需要等 spring ai 升级：https://github.com/spring-projects/spring-ai/pull/2800
    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("thkinking 下，1+1 为什么等于 2 "));
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(AnthropicApi.ChatModel.CLAUDE_SONNET_4)
                .thinking(AnthropicApi.ThinkingType.ENABLED, 3096)
                .temperature(1D)
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult());
        }).then().block();
    }

}
