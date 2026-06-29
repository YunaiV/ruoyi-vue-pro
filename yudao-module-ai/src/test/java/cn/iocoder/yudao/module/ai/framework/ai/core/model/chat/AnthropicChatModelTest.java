package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.hutool.system.SystemUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.ai.util.AiUtils.validateApiKey;

/**
 * {@link AnthropicChatModel} 集成测试类
 *
 * @author 芋道源码
 */
public class AnthropicChatModelTest {

    private static final String BASE_URL = "https://api.teamorouter.com";
    private static final String API_KEY = SystemUtil.get("ANTHROPIC_API_KEY",
            "sk-xxxx"); // 按需改成你的 Anthropic API Key
    private static final String MODEL = "claude-sonnet-4-6";

    private final AnthropicChatModel chatModel = AnthropicChatModel.builder()
            .options(AnthropicChatOptions.builder()
                    .baseUrl(BASE_URL)
                    .apiKey(API_KEY)
                    .model(MODEL)
                    .temperature(0.7)
                    .maxTokens(4096)
                    .build())
            .build();

    @Test
    @Disabled
    public void testCall() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        ChatResponse response = chatModel.call(new Prompt(messages));
        // 打印结果
        System.out.println(response);
        System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
    }

    @Test
    @Disabled
    public void testStream() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

    @Test
    @Disabled
    public void testStream_thinking() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("thkinking 下，1+1 为什么等于 2 "));
        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .model(MODEL)
                .thinkingEnabled(1024) // https://platform.claude.com/docs/en/build-with-claude/extended-thinking
                .maxTokens(4096)
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

}
