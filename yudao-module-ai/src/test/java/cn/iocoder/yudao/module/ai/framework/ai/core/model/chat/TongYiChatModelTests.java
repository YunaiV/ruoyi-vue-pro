package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DashScopeChatModel} 集成测试类
 *
 * @author fansili
 */
public class TongYiChatModelTests {

    private final DashScopeChatModel chatModel = new DashScopeChatModel(
            new DashScopeApi("sk-7d903764249848cfa912733146da12d1"),
            DashScopeChatOptions.builder()
                    .withModel("qwen1.5-72b-chat") // 模型
//                    .withModel("deepseek-r1") // 模型（deepseek-r1）
//                    .withModel("deepseek-v3") // 模型（deepseek-v3）
//                    .withModel("deepseek-r1-distill-qwen-1.5b") // 模型（deepseek-r1-distill-qwen-1.5b）
                    .build());

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
