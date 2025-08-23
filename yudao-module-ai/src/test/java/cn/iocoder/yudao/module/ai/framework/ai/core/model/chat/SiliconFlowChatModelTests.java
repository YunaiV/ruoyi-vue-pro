package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
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
 * {@link SiliconFlowChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class SiliconFlowChatModelTests {

    private final DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
            .deepSeekApi(DeepSeekApi.builder()
                    .baseUrl(SiliconFlowApiConstants.DEFAULT_BASE_URL)
                    .apiKey("sk-epsakfenqnyzoxhmbucsxlhkdqlcbnimslqoivkshalvdozz") // apiKey
                    .build())
            .defaultOptions(DeepSeekChatOptions.builder()
                    .model(SiliconFlowApiConstants.MODEL_DEFAULT) // 模型
//                    .model("deepseek-ai/DeepSeek-R1") // 模型（deepseek-ai/DeepSeek-R1）可用赠费
//                    .model("Pro/deepseek-ai/DeepSeek-R1") // 模型（Pro/deepseek-ai/DeepSeek-R1）需要付费
                    .temperature(0.7)
                    .build())
            .build();

    private final SiliconFlowChatModel chatModel = new SiliconFlowChatModel(openAiChatModel);

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

    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model("deepseek-ai/DeepSeek-R1")
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
