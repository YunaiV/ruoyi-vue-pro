package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.qianfan.QianFanChatOptions;
import org.springframework.ai.qianfan.api.QianFanApi;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

// TODO @芋艿：百度千帆 API 提供了 V2 版本，目前 Spring AI 不兼容，可关键 <https://github.com/spring-projects/spring-ai/issues/2179> 进展
/**
 * {@link QianFanChatModel} 的集成测试
 *
 * @author fansili
 */
public class YiYanChatModelTests {

    private final QianFanChatModel chatModel = new QianFanChatModel(
            new QianFanApi("qS8k8dYr2nXunagK4SSU8Xjj", "pHGbx51ql2f0hOyabQvSZezahVC3hh3e"), // 密钥
            QianFanChatOptions.builder()
                    .model(QianFanApi.ChatModel.ERNIE_4_0_8K_Preview.getValue())
                    .build()
    );

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        // TODO @芋艿：文心一言，只要带上 system message 就报错，已经各种测试，很莫名！
//        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
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
        // TODO @芋艿：文心一言，只要带上 system message 就报错，已经各种测试，很莫名！
//        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(System.out::println).then().block();
    }

}
