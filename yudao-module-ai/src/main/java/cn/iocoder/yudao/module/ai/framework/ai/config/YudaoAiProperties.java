package cn.iocoder.yudao.module.ai.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 芋道 AI 配置类
 *
 * @author fansili
 * @since 1.0
 */
@ConfigurationProperties(prefix = "yudao.ai")
@Data
public class YudaoAiProperties {

    /**
     * 谷歌 Gemini
     */
    private Gemini gemini;

    /**
     * 字节豆包
     */
    private DouBao doubao;

    /**
     * 腾讯混元
     */
    private HunYuan hunyuan;

    /**
     * 硅基流动
     */
    private SiliconFlow siliconflow;

    /**
     * 讯飞星火
     */
    private XingHuo xinghuo;

    /**
     * 百川
     */
    private BaiChuan baichuan;

    /**
     * Midjourney 绘图
     */
    private Midjourney midjourney;

    /**
     * Suno 音乐
     */
    @SuppressWarnings("SpellCheckingInspection")
    private Suno suno;

    /**
     * 网络搜索
     */
    private WebSearch webSearch;

    @Data
    public static class Gemini {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class DouBao {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class HunYuan {

        private String enable;
        private String baseUrl;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class SiliconFlow {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class XingHuo {

        private String enable;
        private String appId;
        private String appKey;
        private String secretKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class BaiChuan {

        private String enable;
        private String apiKey;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class Midjourney {

        private String enable;
        private String baseUrl;

        private String apiKey;
        private String notifyUrl;

    }

    @Data
    public static class Suno {

        private boolean enable;

        private String baseUrl;

    }

    @Data
    public static class WebSearch {

        private boolean enable;

        private String apiKey;

    }

}
