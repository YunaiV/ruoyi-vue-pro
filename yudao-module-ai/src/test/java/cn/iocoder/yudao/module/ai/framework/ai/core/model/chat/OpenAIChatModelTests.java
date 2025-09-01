package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import com.azure.ai.openai.models.ReasoningEffortValue;
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
 * {@link OpenAiChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class OpenAIChatModelTests {

    private final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .baseUrl("https://api.holdai.top")
                    .apiKey("sk-z5joyRoV1iFEnh2SAi8QPNrIZTXyQSyxTmD5CoNDQbFixK2l") // apiKey
                    .build())
            .defaultOptions(OpenAiChatOptions.builder()
                    .model("gpt-5-nano-2025-08-07") // 模型
//                    .model(OpenAiApi.ChatModel.O1) // 模型
                    .temperature(0.7)
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
        System.out.println(response.getResult().getOutput());
    }

    @Test
    @Disabled
    public void testStream() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("帮我推理下，怎么实现一个用户中心！"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult().getOutput());
        }).then().block();
    }

    // TODO @芋艿：无法触发思考的字段返回，需要 response api：https://github.com/spring-projects/spring-ai/issues/2962
    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-5")
//                .model(OpenAiApi.ChatModel.O4_MINI)
//                .model("o3-pro")
                .reasoningEffort(ReasoningEffortValue.LOW.getValue())
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
