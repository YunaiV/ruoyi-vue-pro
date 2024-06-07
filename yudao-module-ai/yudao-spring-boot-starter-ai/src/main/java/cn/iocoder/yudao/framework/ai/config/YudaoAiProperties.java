package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanChatModel;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageStyleEnum;
import lombok.Data;
import lombok.experimental.Accessors;
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

    private QianWenProperties qianwen;
    private XingHuoProperties xinghuo;
    private YiYanProperties yiyan;
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
    @Accessors(chain = true)
    public static class QianWenProperties extends ChatProperties {

        /**
         * api key
         */
        private String apiKey;
        /**
         * model
         */
        private YiYanChatModel model;

    }

    @Data
    @Accessors(chain = true)
    public static class XingHuoProperties extends ChatProperties {
        private String appId;
        private String appKey;
        private String secretKey;
        private XingHuoChatModel model;
    }

    @Data
    @Accessors(chain = true)
    public static class YiYanProperties extends ChatProperties {
        /**
         * appKey
         */
        private String appKey;
        /**
         * secretKey
         */
        private String secretKey;
        /**
         * 模型
         */
        private YiYanChatModel model = YiYanChatModel.ERNIE4_3_5_8K;
        /**
         * token 刷新时间(默认 86400 = 24小时)
         */
        private int refreshTokenSecondTime = 86400;
    }

    @Data
    @Accessors(chain = true)
    public static class OpenAiImageProperties {

        private boolean enable = false;

        /**
         * api key
         */
        private String apiKey;
        /**
         * 模型
         */
        private OpenAiImageModelEnum model = OpenAiImageModelEnum.DALL_E_2;
        /**
         * 风格
         */
        private OpenAiImageStyleEnum style = OpenAiImageStyleEnum.VIVID;

    }

    @Data
    @Accessors(chain = true)
    public static class MidjourneyProperties {
        private boolean enable = false;

        /**
         * socket 链接地址
         */
        private String wssUrl = "wss://gateway.discord.gg";
        /**
         * token
         */
        private String token;
        /**
         * 服务id
         */
        private String guildId;
        /**
         * 频道id
         */
        private String channelId;
    }

    @Data
    public static class SunoProperties {

        private boolean enable = false;
        /**
         * suno-api 服务的基本地址
         */
        private String baseUrl;

    }

}
