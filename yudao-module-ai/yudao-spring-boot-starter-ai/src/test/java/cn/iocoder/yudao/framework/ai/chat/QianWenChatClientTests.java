package cn.iocoder.yudao.framework.ai.chat;

import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenApi;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenOptions;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsRequest;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Scanner;
import java.util.function.Consumer;

/**
 * author: fansili
 * time: 2024/3/13 21:37
 */
public class QianWenChatClientTests {

    private QianWenChatClient qianWenChatClient;

    @Before
    public void setup() {
        QianWenApi qianWenApi = new QianWenApi(
                "",
                "",
                "",
                null
        );
        qianWenChatClient = new QianWenChatClient(
                qianWenApi,
                new QianWenOptions()
                        .setAppId("5f14955f201a44eb8dbe0c57250a32ce")
        );
    }

    @Test
    public void callTest() {
        ChatResponse call = qianWenChatClient.call(new Prompt("Java语言怎么样？"));
        System.err.println(call.getResult());
    }

    @Test
    public void streamTest() {
        Flux<ChatResponse> flux = qianWenChatClient.stream(new Prompt("Java语言怎么样？"));
        flux.subscribe(new Consumer<ChatResponse>() {
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
