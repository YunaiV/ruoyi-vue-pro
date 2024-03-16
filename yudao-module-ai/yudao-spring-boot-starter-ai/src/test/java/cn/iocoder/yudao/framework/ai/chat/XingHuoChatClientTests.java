package cn.iocoder.yudao.framework.ai.chat;

import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoApi;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoOptions;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Scanner;
import java.util.function.Consumer;

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
                new XingHuoOptions().setDomain(XingHuoChatModel.XING_HUO_3_5)
        );
    }

    @Test
    public void callTest() {
        ChatResponse call = xingHuoChatClient.call(new Prompt("java和go那个性能更好!"));
        System.err.println(call.getResult());
    }

    @Test
    public void streamTest() {
        Flux<ChatResponse> stream = xingHuoChatClient.stream(new Prompt("java和go那个性能更好!"));
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
