package cn.iocoder.yudao.framework.ai.chat;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.models.yiyan.YiYanChatClient;
import org.springframework.ai.models.yiyan.YiYanChatModel;
import org.springframework.ai.models.yiyan.YiYanOptions;
import org.springframework.ai.models.yiyan.api.YiYanApi;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * chat 文心一言
 * <p>
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
        YiYanOptions yiYanOptions = new YiYanOptions();
        yiYanOptions.setMaxOutputTokens(2048);
        yiYanOptions.setTopP(0.6f);
        yiYanOptions.setTemperature(0.85f);
        yiYanChatClient = new YiYanChatClient(
                yiYanApi,
                yiYanOptions
        );
    }

    @Test
    public void callTest() {

        // tip: 百度的message 有特殊规则(最后一个message为当前请求的信息，前面的message为历史对话信息)
        // tip: 地址 https://cloud.baidu.com/doc/WENXINWORKSHOP/s/jlil56u11
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景，所有问题都采用文言文回答。"));
        messages.add(new UserMessage("长沙怎么样？"));

        ChatResponse call = yiYanChatClient.call(new Prompt(messages));
        System.err.println(call.getResult());
    }

    @Test
    public void streamTest() {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景，所有问题都采用文言文回答。"));
        messages.add(new UserMessage("长沙怎么样？"));

        Flux<ChatResponse> fluxResponse = yiYanChatClient.stream(new Prompt(messages));
        fluxResponse.subscribe(chatResponse -> System.err.print(chatResponse.getResult().getOutput().getContent()));
        // 阻止退出
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
