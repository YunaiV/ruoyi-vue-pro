package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
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
 * {@link HunYuanChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class HunYuanChatModelTests {

    private final OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .baseUrl(HunYuanChatModel.BASE_URL)
                    .apiKey("sk-bcd") // apiKey
                    .build())
            .defaultOptions(OpenAiChatOptions.builder()
                    .model(HunYuanChatModel.MODEL_DEFAULT) // 模型
                    .temperature(0.7)
                    .build())
            .build();

    private final HunYuanChatModel chatModel = new HunYuanChatModel(openAiChatModel);

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

    private final OpenAiChatModel deepSeekOpenAiChatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .baseUrl(HunYuanChatModel.DEEP_SEEK_BASE_URL)
                    .apiKey("sk-abc") // apiKey
                    .build())
            .defaultOptions(OpenAiChatOptions.builder()
//                    .model(HunYuanChatModel.DEEP_SEEK_MODEL_DEFAULT) // 模型（"deepseek-v3"）
                    .model("deepseek-r1") // 模型（"deepseek-r1"）
                    .temperature(0.7)
                    .build())
            .build();

    private final HunYuanChatModel deepSeekChatModel = new HunYuanChatModel(deepSeekOpenAiChatModel);

    @Test
    @Disabled
    public void testCall_deepseek() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        ChatResponse response = deepSeekChatModel.call(new Prompt(messages));
        // 打印结果
        System.out.println(response);
    }

    @Test
    @Disabled
    public void testStream_deekseek() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = deepSeekChatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(System.out::println).then().block();
    }


}
