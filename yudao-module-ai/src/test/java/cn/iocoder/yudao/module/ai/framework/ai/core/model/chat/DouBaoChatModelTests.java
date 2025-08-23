package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DouBaoChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class DouBaoChatModelTests {

    /**
     * 相比 OpenAIChatModel 来说，DeepSeekChatModel 可以兼容豆包的 thinking 能力！
     */
    private final DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
            .deepSeekApi(DeepSeekApi.builder()
                    .baseUrl(DouBaoChatModel.BASE_URL)
                    .completionsPath(DouBaoChatModel.COMPLETE_PATH)
                    .apiKey("5c1b5747-26d2-4ebd-a4e0-dd0e8d8b4272") // apiKey
                    .build())
            .defaultOptions(DeepSeekChatOptions.builder()
                    .model("doubao-1-5-lite-32k-250115") // 模型（doubao）
//                    .model("doubao-seed-1-6-thinking-250715") // 模型（doubao）
//                    .model("deepseek-r1-250120") // 模型（deepseek）
                    .temperature(0.7)
                    .build())
            .build();

    private final DouBaoChatModel chatModel = new DouBaoChatModel(openAiChatModel);

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
        messages.add(new UserMessage("详细推理下，帮我设计一个用户中心！"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(System.out::println).then().block();
    }

    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model("doubao-seed-1-6-thinking-250715")
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult().getOutput());
        }).then().block();
    }

}
