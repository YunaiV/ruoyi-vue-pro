package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankOptions;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * {@link DashScopeChatModel} 集成测试类
 *
 * @author fansili
 */
public class TongYiChatModelTests {

    private final DashScopeChatModel chatModel = DashScopeChatModel.builder()
            .dashScopeApi(DashScopeApi.builder()
                    .apiKey("sk-47aa124781be4bfb95244cc62f63f7d0")
                    .build())
            .defaultOptions(DashScopeChatOptions.builder()
//                    .withModel("qwen1.5-72b-chat") // 模型
                    .withModel("qwen3-235b-a22b-thinking-2507") // 模型
//                    .withModel("deepseek-r1") // 模型（deepseek-r1）
//                    .withModel("deepseek-v3") // 模型（deepseek-v3）
//                    .withModel("deepseek-r1-distill-qwen-1.5b") // 模型（deepseek-r1-distill-qwen-1.5b）
//                    .withEnableThinking(true)
                    .build())
            .build();

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
//        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("帮我推理下，怎么实现一个用户中心！"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult().getOutput());
        }).then().block();
    }

    @Test
    @Disabled
    public void testStream_thinking() {
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel("qwen3-235b-a22b-thinking-2507")
//                .withModel("qwen-max-2025-01-25")
                .withEnableThinking(true) // 必须设置，否则会报错
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(response.getResult().getOutput());
        }).then().block();
    }

    @Test
    @Disabled
    public void testRerank() {
        // 准备环境
        RerankModel rerankModel = new DashScopeRerankModel(
                DashScopeApi.builder()
                        .apiKey("sk-47aa124781be4bfb95244cc62f63f7d0")
                        .build());
        // 准备参数
        String query = "spring";
        Document document01 = new Document("abc");
        Document document02 = new Document("sapring");
        RerankOptions options = DashScopeRerankOptions.builder()
                .withTopN(1)
                .withModel("gte-rerank-v2")
                .build();
        RerankRequest rerankRequest = new RerankRequest(
                query,
                asList(document01, document02),
                options);

        // 调用
        RerankResponse call = rerankModel.call(rerankRequest);
        // 打印结果
        System.out.println(JsonUtils.toJsonPrettyString(call));
    }

}
