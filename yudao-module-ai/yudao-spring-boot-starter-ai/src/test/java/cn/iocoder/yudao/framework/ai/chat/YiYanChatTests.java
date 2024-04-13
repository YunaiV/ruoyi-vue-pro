package cn.iocoder.yudao.framework.ai.chat;

import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatClient;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatModel;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanOptions;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanApi;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Scanner;

/**
 * chat 文心一言
 *
 * author: fansili
 * time: 2024/3/12 20:59
 */
public class YiYanChatTests {

    private YiYanChatClient yiYanChatClient;

    @Before
    public void setup() {
        YiYanApi yiYanApi = new YiYanApi(
                "x0cuLZ7XsaTCU08vuJWO87Lg",
                "R9mYF9dl9KASgi5RUq0FQt3wRisSnOcK",
                YiYanChatModel.ERNIE4_3_5_8K,
                86400
        );
        yiYanChatClient = new YiYanChatClient(yiYanApi, new YiYanOptions().setMax_output_tokens(2048));
    }

    @Test
    public void callTest() {
        ChatResponse call = yiYanChatClient.call(new Prompt("什么编程语言最好？"));
        System.err.println(call.getResult());
    }

    @Test
    public void streamTest() {
        Flux<ChatResponse> fluxResponse = yiYanChatClient.stream(new Prompt("用java帮我写一个快排算法？"));
        fluxResponse.subscribe(chatResponse -> System.err.print(chatResponse.getResult().getOutput().getContent()));
        // 阻止退出
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
