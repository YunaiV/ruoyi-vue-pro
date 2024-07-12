package cn.iocoder.yudao.framework.ai.chat;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatModel;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatOptions;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
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
 * {@link TongYiChatModel} 集成测试类
 *
 * @author fansili
 */
public class TongYiChatModelTests {

    private final Generation generation = new Generation();
    private final TongYiChatModel chatModel = new TongYiChatModel(generation,
            TongYiChatOptions.builder().withModel("qwen1.5-72b-chat").build());

    static {
        Constants.apiKey = "sk-Zsd81gZYg7";
    }

    @BeforeEach
    public void before() {
        // 防止 TongYiChatModel 调用空指针
        ReflectUtil.setFieldValue(chatModel, "msgManager", new MessageManager());
    }

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
