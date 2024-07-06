package cn.iocoder.yudao.framework.ai.chat;

import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatClient;
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
 * {@link XingHuoChatClient} 集成测试
 *
 * @author fansili
 */
public class XingHuoChatClientTests {

    private final XingHuoChatClient client = new XingHuoChatClient(
            "cb6415c19d6162cda07b47316fcb0416",
            "Y2JiYTIxZjA3MDMxMjNjZjQzYzVmNzdh");

    @Test
    public void testCall() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        ChatResponse response = client.call(new Prompt(messages));
        // 打印结果
        System.err.println(response);
    }

    @Test
    public void testStream() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = client.stream(new Prompt(messages));
        // 打印结果
        List<ChatResponse> responses = flux.collectList().block();
        assert responses != null;
        responses.forEach(System.err::println);
    }

}
