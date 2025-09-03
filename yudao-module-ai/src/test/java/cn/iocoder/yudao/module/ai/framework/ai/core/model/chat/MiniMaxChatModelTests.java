package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link MiniMaxChatModel} 的集成测试
 *
 * @author 芋道源码
 */
public class MiniMaxChatModelTests {

    private final MiniMaxChatModel chatModel = new MiniMaxChatModel(
            new MiniMaxApi("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJHcm91cE5hbWUiOiLnjovmlofmlowiLCJVc2VyTmFtZSI6IueOi-aWh-aWjCIsIkFjY291bnQiOiIiLCJTdWJqZWN0SUQiOiIxODk3Mjg3MjQ5NDU2ODA4MzQ2IiwiUGhvbmUiOiIxNTYwMTY5MTM5OSIsIkdyb3VwSUQiOiIxODk3Mjg3MjQ5NDQ4NDE5NzM4IiwiUGFnZU5hbWUiOiIiLCJNYWlsIjoiIiwiQ3JlYXRlVGltZSI6IjIwMjUtMDMtMTEgMTI6NTI6MDIiLCJUb2tlblR5cGUiOjEsImlzcyI6Im1pbmltYXgifQ.aAuB7gWW_oA4IYhh-CF7c9MfWWxKN49B_HK-DYjXaDwwffhiG-H1571z1WQhp9QytWG-DqgLejneeSxkiq1wQIe3FsEP2wz4BmGBct31LehbJu8ehLxg_vg75Uod1nFAHbm5mZz6JSVLNIlSo87Xr3UtSzJhAXlapEkcqlA4YOzOpKrZ8l5_OJPTORTCmHWZYgJcRS-faNiH62ZnUEHUozesTFhubJHo5GfJCw_edlnmfSUocERV1BjWvenhZ9My-aYXNktcW9WaSj9l6gayV7A0Ium_PL55T9ln1PcI8gayiVUKJGJDoqNyF1AF9_aF9NOKtTnQzwNqnZdlTYH6hw"), // 密钥
            MiniMaxChatOptions.builder()
                    .model(MiniMaxApi.ChatModel.ABAB_6_5_G_Chat.getValue()) // 模型
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

    // TODO @芋艿：暂时没解析 reasoning_content 结果，需要等官方修复
    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        MiniMaxChatOptions options = MiniMaxChatOptions.builder()
                .model("MiniMax-M1")
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult().getOutput());
        }).then().block();
    }

}
