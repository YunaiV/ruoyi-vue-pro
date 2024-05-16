package cn.iocoder.yudao.framework.ai.chat;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.models.tongyi.QianWenChatClient;
import org.springframework.ai.models.tongyi.QianWenChatModal;
import org.springframework.ai.models.tongyi.QianWenOptions;
import org.springframework.ai.models.tongyi.api.QianWenApi;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
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
        QianWenApi qianWenApi = new QianWenApi("sk-Zsd81gZYg7", QianWenChatModal.QWEN_72B_CHAT);
        QianWenOptions qianWenOptions = new QianWenOptions();
        qianWenOptions.setTopP(0.8F);
//        qianWenOptions.setTopK(3); TODO 芋艿：临时处理
//        qianWenOptions.setTemperature(0.6F); TODO 芋艿：临时处理
        qianWenChatClient = new QianWenChatClient(
                qianWenApi,
                qianWenOptions
        );
    }

    @Test
    public void callTest() {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的小红书文艺作者，抒写着各城市的美好文化和风景。"));
        messages.add(new UserMessage("长沙怎么样？"));

        ChatResponse call = qianWenChatClient.call(new Prompt(messages));
        System.err.println(call.getResult());
    }

    @Test
    public void streamTest() {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("长沙怎么样？"));

        Flux<ChatResponse> flux = qianWenChatClient.stream(new Prompt(messages));
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

    @Test
    public void qianwenDemoTest() throws NoApiKeyException, InputRequiredException {
        com.alibaba.dashscope.aigc.generation.Generation gen = new com.alibaba.dashscope.aigc.generation.Generation();
        MessageManager msgManager = new MessageManager(10);
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content("You are a helpful assistant.").build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content("就当前的海洋污染的情况，写一份限塑的倡议书提纲，需要有理有据地号召大家克制地使用塑料制品").build();
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        QwenParam param =
                QwenParam.builder().model("qwen-72b-chat").messages(msgManager.get())
                        .resultFormat(QwenParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        /* set the random seed, optional, default to 1234 if not set */
                        .seed(100)
                        .apiKey("sk-Zsd81gZYg7")
                        .build();
        GenerationResult result = gen.call(param);
        System.out.println(result);
        System.out.println("-----------------");
        System.out.println("-----------------");
        msgManager.add(result);
        param.setPrompt("能否缩短一些，只讲三点");
        param.setMessages(msgManager.get());
        result = gen.call(param);
        System.out.println(result);
    }
}
