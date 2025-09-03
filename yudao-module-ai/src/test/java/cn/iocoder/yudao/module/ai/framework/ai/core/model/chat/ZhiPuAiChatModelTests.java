package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ZhiPuAiChatModel} 的集成测试
 *
 * @author 芋道源码
 */
public class ZhiPuAiChatModelTests {

    private final ZhiPuAiChatModel chatModel = new ZhiPuAiChatModel(
            new ZhiPuAiApi("2f35fb6ca4ea41fab898729b7fac086c.6ESSfPcCkxaKEUlR"), // 密钥
            ZhiPuAiChatOptions.builder()
                    .model(ZhiPuAiApi.ChatModel.GLM_4.getName()) // 模型
                    .build()
    );

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

    // TODO @芋艿：暂时没解析 reasoning_content 结果，需要等官方修复
    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model("GLM-4.5")
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
