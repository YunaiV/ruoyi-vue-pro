package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.util.AiUtils;
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
import java.util.Objects;

import static cn.iocoder.yudao.module.ai.util.AiUtils.validateApiKey;
import static java.util.Arrays.asList;

/**
 * {@link DashScopeChatModel} 集成测试类
 *
 * @author fansili
 */
public class TongYiChatModelTests {

    private static final String API_KEY = SystemUtil.get("DASHSCOPE_API_KEY",
            "sk-xxxx"); // 按需改成你的 DashScope API Key
    private static final String MODEL = SystemUtil.get("DASHSCOPE_MODEL",
            "qwen3.7-plus");

    private final DashScopeChatModel chatModel = DashScopeChatModel.builder()
            .dashScopeApi(DashScopeApi.builder()
                    .apiKey(API_KEY)
                    .build())
            .defaultOptions(DashScopeChatOptions.builder()
                    .multiModel(AiUtils.TONG_YI_MULTI_MODELS.contains(MODEL)) // 多模态模型需要设置为 true，可见 https://help.aliyun.com/zh/model-studio/error-code#error-url
                    .model(MODEL) // 模型
//                    .model("deepseek-r1") // 模型（deepseek-r1）
//                    .model("deepseek-v3") // 模型（deepseek-v3）
//                    .model("deepseek-r1-distill-qwen-1.5b") // 模型（deepseek-r1-distill-qwen-1.5b）
//                    .enableThinking(true)
                    .build())
            .build();

    @Test
    @Disabled
    public void testCall() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        ChatResponse response = chatModel.call(new Prompt(messages));
        // 打印结果
        System.out.println(response);
        System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
    }

    @Test
    @Disabled
    public void testStream() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
//        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("帮我推理下，怎么实现一个用户中心！"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

    @Test
    @Disabled
    public void testStream_thinking() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .model(MODEL).multiModel(AiUtils.TONG_YI_MULTI_MODELS.contains(MODEL))
//                .withModel("qwen-max-2025-01-25")
                .enableThinking(true) // 必须设置，否则会报错
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
//            System.out.println(response);
            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

    @Test
    @Disabled
    public void testRerank() {
        validateApiKey(API_KEY);
        // 准备环境
        RerankModel rerankModel = new DashScopeRerankModel(
                DashScopeApi.builder()
                        .apiKey(API_KEY)
                        .build());
        // 准备参数
        String query = "spring";
        Document document01 = new Document("abc");
        Document document02 = new Document("sapring");
        RerankOptions options = DashScopeRerankOptions.builder()
                .topN(1)
                .model("gte-rerank-v2")
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
