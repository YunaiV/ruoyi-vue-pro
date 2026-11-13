package cn.iocoder.yudao.module.ai.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 芋道 AI 配置类
 *
 * 配置项按「提供方 + 能力」分层，与 Spring AI 的 spring.ai.{provider}.{capability} 保持一致，例如说 yudao.ai.bytedance.chat。
 * 提供方与 {@link cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum} 一一对应；能力与模型类型对应。
 * 凭据放在能力层，因为同一提供方的不同能力，密钥可能不同（例如说腾讯混元的 API Key、腾讯文字识别的 SecretId|SecretKey）
 *
 * @author fansili
 * @since 1.0
 */
@ConfigurationProperties(prefix = "yudao.ai")
@Data
public class YudaoAiProperties {

    /**
     * 谷歌：Gemini
     */
    private ChatProvider gemini;

    /**
     * 字节跳动：豆包
     */
    private ChatProvider bytedance;

    /**
     * 腾讯：混元
     */
    private ChatProvider tencent;

    /**
     * 硅基流动
     */
    private ChatProvider siliconflow;

    /**
     * 科大讯飞：星火
     */
    private ChatProvider iflytek;

    /**
     * 百川智能
     */
    private ChatProvider baichuan;

    /**
     * 百度：文心一言
     */
    private ChatProvider baidu;

    /**
     * 智谱
     */
    private ChatProvider zhipu;

    /**
     * MiniMax
     */
    private ChatProvider minimax;

    /**
     * 月之暗面
     */
    private ChatProvider moonshot;

    /**
     * 阶跃星辰
     */
    private ChatProvider stepfun;

    /**
     * 小米 MiMo
     */
    private ChatProvider mimo;

    /**
     * Grok
     */
    private ChatProvider grok;

    /**
     * Midjourney
     */
    private Midjourney midjourney;

    /**
     * 网络搜索
     */
    private WebSearch webSearch;

    /**
     * 提供对话能力的提供方
     */
    @Data
    public static class ChatProvider {

        /**
         * 对话
         */
        private Chat chat;

    }

    /**
     * 对话能力的配置
     */
    @Data
    public static class Chat {

        private String enable;
        /**
         * API 地址，为空时使用各平台的默认地址（部分平台支持自定义）
         */
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class Midjourney {

        /**
         * 绘图
         */
        private MidjourneyImage image;

    }

    @Data
    public static class MidjourneyImage {

        private String enable;
        private String baseUrl;

        private String apiKey;
        private String notifyUrl;

    }

    @Data
    public static class WebSearch {

        private boolean enable;

        private String apiKey;

    }

}
