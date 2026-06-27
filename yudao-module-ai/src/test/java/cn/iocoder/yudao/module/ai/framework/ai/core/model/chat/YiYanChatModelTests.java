package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.yiyan.YiYanChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
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
 * {@link YiYanChatModel} 的集成测试
 *
 * @author fansili
 */
public class YiYanChatModelTests {

    private final YiYanChatModel chatModel = new YiYanChatModel(DeepSeekChatModel.builder()
            .deepSeekApi(DeepSeekApi.builder()
                    .baseUrl(YiYanChatModel.BASE_URL)
                    .apiKey("bce-v3/xxx") // 密钥
                    .build())
            .options(DeepSeekChatOptions.builder()
                    .model(YiYanChatModel.MODEL_DEFAULT)
                    .build())
            .build());

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
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
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(System.out::println).then().block();
    }

}
