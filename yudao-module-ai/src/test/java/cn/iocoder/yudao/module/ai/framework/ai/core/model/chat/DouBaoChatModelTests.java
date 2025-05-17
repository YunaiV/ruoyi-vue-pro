package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DouBaoChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class DouBaoChatModelTests {

    private final OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .baseUrl(DouBaoChatModel.BASE_URL)
                    .apiKey("5c1b5747-26d2-4ebd-a4e0-dd0e8d8b4272") // apiKey
                    .build())
            .defaultOptions(OpenAiChatOptions.builder()
                    .model("doubao-1-5-lite-32k-250115") // 模型（doubao）
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

    // TODO @芋艿：因为使用的是 v1 api，导致 deepseek-r1-250120 不返回 think 过程，后续需要优化
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

}
