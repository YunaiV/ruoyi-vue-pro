package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.ai.autoconfigure.openai.OpenAiImageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ai 自动配置
 *
 * @author fansili
 * @time 2024/4/12 16:29
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "yudao.ai")
public class YudaoAiProperties {

    private XingHuoProperties xinghuo;
    private OpenAiImageProperties openAiImage;
    private MidjourneyProperties midjourney;
    private SunoProperties suno;

    @Data
    @Accessors(chain = true)
    public static class ChatProperties {

        private boolean enable = false;

        private AiPlatformEnum aiPlatform;

        private Float temperature;

        private Float topP;

        private Integer topK;
        /**
         * 用于限制模型生成token的数量，max_tokens设置的是生成上限，并不表示一定会生成这么多的token数量
         */
        private Integer maxTokens;
    }

    @Data
    public static class XingHuoProperties extends ChatProperties {

        private String appId;
        private String appKey;
        private String secretKey;
        private XingHuoChatModel model;

    }

    @Data
    public static class MidjourneyProperties {

        private String enable;
        private String apiKey;
        private String baseUrl;
        private String notifyUrl;

    }

    @Data
    public static class SunoProperties {

        private boolean enable = false;

        /**
         * API 服务的基本地址
         */
        private String baseUrl;

    }

}
