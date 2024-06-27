package cn.iocoder.yudao.framework.ai.chat;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoApi;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

// TODO 芋艿：整理单测
/**
 * 讯飞星火 tests
 * <p>
 * author: fansili
 * time: 2024/3/11 11:00
 */
public class XingHuoChatClientTests {

    private XingHuoChatClient xingHuoChatClient;

    @Before
    public void setup() {
        // 初始化 xingHuoChatClient
        xingHuoChatClient = new XingHuoChatClient(
                new XingHuoApi(
                        "13c8cca6",
                        "cb6415c19d6162cda07b47316fcb0416",
                        "Y2JiYTIxZjA3MDMxMjNjZjQzYzVmNzdh"
                ),
                new XingHuoOptions().setChatModel(XingHuoChatModel.XING_HUO_3_5)
        );
    }

    @Test
    public void callTest() {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("长沙怎么样？"));

        ChatResponse call = xingHuoChatClient.call(new Prompt(messages));
        System.err.println(call.getResult());
    }

    @Test
    public void streamTest() {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("长沙怎么样？"));

        Flux<ChatResponse> stream = xingHuoChatClient.stream(new Prompt(messages));
        stream.subscribe(new Consumer<ChatResponse>() {
            @Override
            public void accept(ChatResponse chatResponse) {
                System.err.print(chatResponse.getResult().getOutput().getContent());
            }
        });
        // 阻止退出
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
