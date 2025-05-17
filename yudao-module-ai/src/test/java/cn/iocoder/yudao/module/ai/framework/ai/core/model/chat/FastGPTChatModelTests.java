package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 {@link OpenAiChatModel} 集成 FastGPT 测试
 *
 * @author 芋道源码
 */
public class FastGPTChatModelTests {

    private final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .baseUrl("https://cloud.fastgpt.cn/api")
                    .apiKey("fastgpt-aqcc61kFtF8CeaglnGAfQOCIDWwjGdJVJHv6hIlMo28otFlva2aZNK") // apiKey
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
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult().getOutput());
        }).then().block();
    }

}
