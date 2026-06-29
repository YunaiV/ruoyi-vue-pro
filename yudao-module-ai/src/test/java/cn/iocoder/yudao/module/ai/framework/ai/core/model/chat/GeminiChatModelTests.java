package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.ai.util.AiUtils.validateApiKey;

/**
 * {@link GoogleGenAiChatModel} 集成测试
 *
 * @author 芋道源码
 */
public class GeminiChatModelTests {

    private static final String BASE_URL = SystemUtil.get("GEMINI_BASE_URL");
    private static final String API_KEY = SystemUtil.get("GEMINI_API_KEY",
            "sk-xxxx"); // 按需改成你的 Gemini API Key
    private static final String MODEL = "gemini-3.5-flash";

    private final GoogleGenAiChatModel chatModel = GoogleGenAiChatModel.builder()
            .genAiClient(buildClient())
            .options(GoogleGenAiChatOptions.builder()
                    .model(MODEL) // 模型
                    .temperature(0.7)
                    .build())
            .build();

    private static Client buildClient() {
        Client.Builder builder = Client.builder().apiKey(API_KEY);
        if (StrUtil.isNotBlank(BASE_URL)) {
            builder.httpOptions(HttpOptions.builder().baseUrl(BASE_URL).build());
        }
        return builder.build();
    }

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
        messages.add(new SystemMessage("你是一个优质的文言文作者，用文言文描述着各城市的人文风景。"));
        messages.add(new UserMessage("1 + 1 = ？"));

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages));
        // 打印结果
        flux.doOnNext(response -> {
            System.out.println(response);
//            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

    @Test
    @Disabled
    public void testStream_thinking() {
        validateApiKey(API_KEY);
        // 准备参数
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("详细分析下，如何设计一个电商系统？"));
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(MODEL)
                .thinkingBudget(1024)
                .includeThoughts(true)
                .build();

        // 调用
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(messages, options));
        // 打印结果
        flux.doOnNext(response -> {
            System.out.println(response);
//            System.out.println(Objects.requireNonNull(response.getResult()).getOutput());
        }).then().block();
    }

}
