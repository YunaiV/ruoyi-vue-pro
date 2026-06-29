package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
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
import java.util.Objects;

import static cn.iocoder.yudao.module.ai.util.AiUtils.validateApiKey;

/**
 * {@link HunYuanChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class HunYuanChatModelTests {

    private static final String API_KEY = SystemUtil.get("HUNYUAN_API_KEY",
            "sk-xxxx");
    private static final String MODEL = SystemUtil.get("HUNYUAN_MODEL",
            HunYuanChatModel.MODEL_DEFAULT);

    private final DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
            .deepSeekApi(DeepSeekApi.builder()
                    .baseUrl(HunYuanChatModel.BASE_URL)
                    .completionsPath(HunYuanChatModel.COMPLETE_PATH)
                    .apiKey(API_KEY) // apiKey
                    .build())
            .options(DeepSeekChatOptions.builder()
                    .model(MODEL) // 模型
                    .temperature(0.7)
                    .build())
            .build();

    private final HunYuanChatModel chatModel = new HunYuanChatModel(openAiChatModel);

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
        flux.doOnNext(System.out::println).then().block();
    }

    @Test
    @Disabled
    public void testStream_thinking() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model(MODEL)
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
