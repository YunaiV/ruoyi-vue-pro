package cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import reactor.core.publisher.Flux;

/**
 * 讯飞星火 {@link ChatModel} 实现类
 *
 * @author fansili
 */
@Slf4j
public class XingHuoChatModel implements ChatModel {

    /**
     * 星火 X2
     *
     * @see <a href="https://spark-api-open.xf-yun.com/x2/chat/completions">接口地址</a>
     */
    public static final String MODEL_X2 = "x2";

    /**
     * 星火 X2 Flash
     *
     * @see <a href="https://spark-api-open.xf-yun.com/agent/v1/chat/completions">接口地址</a>
     */
    public static final String MODEL_X2_FLASH = "x2-flash";

    private static final String BASE_URL_X2 = "https://spark-api-open.xf-yun.com/x2";

    private static final String BASE_URL_X2_FLASH = "https://spark-api-open.xf-yun.com/agent/v1";

    public static final String MODEL_DEFAULT = MODEL_X2_FLASH;

    private static String getBaseUrl(String model) {
        if (MODEL_X2_FLASH.equals(model)) {
            return BASE_URL_X2_FLASH;
        }
        return BASE_URL_X2;
    }

    private final String apiKey;

    private final DeepSeekChatOptions options;

    /**
     * 兼容 OpenAI 接口，进行复用
     */
    private final ChatModel chatModelX2;

    private final ChatModel chatModelX2Flash;

    private XingHuoChatModel(String apiKey, DeepSeekChatOptions options) {
        this.apiKey = apiKey;
        this.options = options;
        this.chatModelX2 = buildChatModel(MODEL_X2);
        this.chatModelX2Flash = buildChatModel(MODEL_X2_FLASH);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return getChatModel(prompt).call(buildApiPrompt(prompt));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return getChatModel(prompt).stream(buildApiPrompt(prompt));
    }

    @Override
    public ChatOptions getOptions() {
        return options;
    }

    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public ChatOptions getDefaultOptions() {
        return getOptions();
    }

    private ChatModel getChatModel(Prompt prompt) {
        String model = options.getModel();
        ChatOptions options = prompt.getOptions();
        if (options != null && isBusinessModel(options.getModel())) {
            model = options.getModel();
        }
        return getChatModel(model);
    }

    private ChatModel getChatModel(String model) {
        if (MODEL_X2_FLASH.equals(model)) {
            return chatModelX2Flash;
        }
        return chatModelX2;
    }

    private ChatModel buildChatModel(String model) {
        return DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(getBaseUrl(model))
                        .apiKey(apiKey)
                        .build())
                .options(options.mutate().model("spark-x").build())
                .build();
    }

    private static Prompt buildApiPrompt(Prompt prompt) {
        ChatOptions options = prompt.getOptions();
        if (options == null) {
            return prompt;
        }
        return Prompt.builder()
                .messages(prompt.getInstructions())
                .chatOptions(options.mutate().model("spark-x").build())
                .build();
    }

    private static boolean isBusinessModel(String model) {
        return MODEL_X2.equals(model) || MODEL_X2_FLASH.equals(model);
    }

    public static final class Builder {

        private String apiKey;

        private DeepSeekChatOptions options;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder options(DeepSeekChatOptions options) {
            this.options = options;
            return this;
        }

        public XingHuoChatModel build() {
            DeepSeekChatOptions options = this.options != null ? this.options : DeepSeekChatOptions.builder().build();
            if (StrUtil.isEmpty(options.getModel())) {
                options = options.mutate().model(MODEL_DEFAULT).build();
            }
            return new XingHuoChatModel(apiKey, options);
        }

    }

}
