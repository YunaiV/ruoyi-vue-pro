package cn.iocoder.yudao.module.ai.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * deepay AI 配置类
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

    /**
     * Runpod（海外 fallback）
     */
    private Runpod runpod;

    /**
     * vLLM（自托管 OpenAI 兼容服务）
     */
    private Vllm vllm;

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
    public static class Grok {

        private String enable;
        private String apiKey;
        private String baseUrl;

        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class Runpod {

        private String enable;

        /**
         * API Key（对应环境变量 RUNPOD_API_KEY）
         */
        private String apiKey;

        /**
         * base URL（对应环境变量 RUNPOD_BASE_URL，默认 https://api.runpod.ai）
         */
        private String baseUrl;

        /**
         * 端点 ID（对应环境变量 RUNPOD_MODEL_ID，默认 qwen3-32b-awq，用于 URL 路径）
         */
        private String endpointId;

        /**
         * 发送给模型的 model 参数（默认 Qwen/Qwen3-32B-AWQ）
         */
        private String model;

        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class Vllm {

        private String enable;

        /**
         * vLLM HTTP 服务地址（默认 http://localhost:8000）
         * 典型用法：vllm serve {model} --host 0.0.0.0 --port 8000
         */
        private String baseUrl;

        /**
         * API Key（对应 vllm serve --api-key 参数，无鉴权可留空）
         */
        private String apiKey;

        /**
         * 模型名称（与 vllm serve 指定的模型一致，如 NousResearch/Meta-Llama-3-8B-Instruct）
         */
        private String model;

        private Double temperature;
        private Integer maxTokens;
        private Double topP;

    }

    @Data
    public static class WebSearch {

        private boolean enable;

        private String apiKey;

    }

}
