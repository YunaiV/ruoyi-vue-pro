package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.hutool.system.SystemUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.ai.util.AiUtils.validateApiKey;

/**
 * {@link OpenAiChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class OpenAIChatModelTests {

    private static final String BASE_URL = SystemUtil.get("OPENAI_BASE_URL",
            "https://api.teamorouter.com/v1");
    private static final String API_KEY = SystemUtil.get("OPENAI_API_KEY",
            "sk-xxxx"); // 按需改成你的 OpenAI API Key
    private static final String MODEL = SystemUtil.get("OPENAI_MODEL",
            "gpt-5.5");

    private final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .options(OpenAiChatOptions.builder()
                    .baseUrl(BASE_URL)
                    .apiKey(API_KEY)
                    .model(MODEL)
                    .temperature(0.7)
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
        messages.add(new UserMessage("帮我推理下，怎么实现一个用户中心！"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

    // TODO @芋艿：无法触发思考的字段返回，需要 response api：https://github.com/spring-projects/spring-ai/issues/2962
    @Test
    @Disabled
    public void testStream_thinking() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .model(MODEL)
                .reasoningEffort("low") // https://help.openai.com/zh-hant/articles/5072518-controlling-the-length-of-openai-model-responses
                .maxCompletionTokens(1024)
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
