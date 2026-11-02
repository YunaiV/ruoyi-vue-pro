package cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.reasoning.AiRequestBodyPatcher;
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

    /**
     * 实际传给厂商的型号：X2、X2 Flash 按接口地址区分，型号统一为 spark-x
     */
    private static final String API_MODEL = "spark-x";

    private final String apiKey;

    private final DeepSeekChatOptions options;

    /**
     * 思考补丁改写器，可为 null；X2、X2 Flash 两个内部客户端都装上
     */
    private final AiRequestBodyPatcher patcher;

    /**
     * 兼容 OpenAI 接口，进行复用
     */
    private final ChatModel chatModelX2;

    private final ChatModel chatModelX2Flash;

    private XingHuoChatModel(String apiKey, DeepSeekChatOptions options, AiRequestBodyPatcher patcher) {
        this.apiKey = apiKey;
        this.options = options;
        this.patcher = patcher;
        this.chatModelX2 = buildChatModel(BASE_URL_X2);
        this.chatModelX2Flash = buildChatModel(BASE_URL_X2_FLASH);
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
    public ChatOptions getDefaultOptions() {
        return options;
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

    private ChatModel buildChatModel(String baseUrl) {
        return DeepSeekChatModel.builder()
                .deepSeekApi(AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey), patcher)
                        .build())
                .defaultOptions(new DeepSeekChatOptions.Builder(options).model(API_MODEL).build())
                .build();
    }

    private static Prompt buildApiPrompt(Prompt prompt) {
        ChatOptions options = prompt.getOptions();
        if (!(options instanceof DeepSeekChatOptions)) {
            return prompt;
        }
        return Prompt.builder()
                .messages(prompt.getInstructions())
                .chatOptions(new DeepSeekChatOptions.Builder((DeepSeekChatOptions) options).model(API_MODEL).build())
                .build();
    }

    private static boolean isBusinessModel(String model) {
        return MODEL_X2.equals(model) || MODEL_X2_FLASH.equals(model);
    }

    public static final class Builder {

        private String apiKey;

        private DeepSeekChatOptions options;

        private AiRequestBodyPatcher patcher;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder patcher(AiRequestBodyPatcher patcher) {
            this.patcher = patcher;
            return this;
        }

        public Builder options(DeepSeekChatOptions options) {
            this.options = options;
            return this;
        }

        public XingHuoChatModel build() {
            DeepSeekChatOptions options = this.options != null ? this.options : DeepSeekChatOptions.builder().build();
            if (StrUtil.isEmpty(options.getModel())) {
                options = new DeepSeekChatOptions.Builder(options).model(MODEL_DEFAULT).build();
            }
            return new XingHuoChatModel(apiKey, options, patcher);
        }

    }

}
