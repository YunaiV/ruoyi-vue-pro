package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.gemini.GeminiChatModel;
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
 * {@link GeminiChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class GeminiChatModelTests {

    private final OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
        .openAiApi(OpenAiApi.builder()
                .baseUrl(GeminiChatModel.BASE_URL)
                .completionsPath(GeminiChatModel.COMPLETE_PATH)
                .apiKey("AIzaSyAVoBxgoFvvte820vEQMma2LKBnC98bqMQ")
                .build())
        .defaultOptions(OpenAiChatOptions.builder()
                .model(GeminiChatModel.MODEL_DEFAULT) // 模型
                .temperature(0.7)
                .build())
        .build();

    private final GeminiChatModel chatModel = new GeminiChatModel(openAiChatModel);

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

}
