package cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Stable Diffusion WebUI (AUTOMATIC1111) HTTP API 客户端
 *
 * <p>调用自托管 SD WebUI 服务的 {@code POST /sdapi/v1/txt2img} 端点。
 * 典型部署方式：在 RunPod / 本机以 Docker 启动 AUTOMATIC1111，
 * 然后通过该 API 客户端生成图片。</p>
 *
 * <p>SD WebUI 响应格式：
 * <pre>{@code
 * {
 *   "images": ["base64_string", ...],
 *   "parameters": { ... },
 *   "info": "..."
 * }
 * }</pre>
 * </p>
 *
 * <p>SD WebUI 官方 API 文档：
 * <a href="https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki/API">Wiki</a></p>
 *
 * @author deepay
 */
public class StableDiffusionWebUiApi {

    /**
     * 文生图端点路径
     */
    public static final String TXT2IMG_PATH = "/sdapi/v1/txt2img";

    /**
     * 图生图端点路径（含 ControlNet alwayson_scripts 透传）
     */
    public static final String IMG2IMG_PATH = "/sdapi/v1/img2img";

    /**
     * 超分辨率（extras）单张图端点路径
     */
    public static final String EXTRAS_SINGLE_PATH = "/sdapi/v1/extra-single-image";

    private final RestClient restClient;

    /**
     * 无鉴权构造函数（SD WebUI 默认不需要 API Key）
     *
     * @param baseUrl SD WebUI 服务地址，如 {@code http://103.196.86.126:15112}
     */
    public StableDiffusionWebUiApi(String baseUrl) {
        this(baseUrl, null);
    }

    /**
     * 带可选鉴权的构造函数
     *
     * @param baseUrl SD WebUI 服务地址
     * @param apiKey  若启动时设置了 {@code --api-auth user:password}，则传入；否则传 null 或空字符串
     */
    public StableDiffusionWebUiApi(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, RestClient.builder());
    }

    public StableDiffusionWebUiApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder) {
        Assert.hasText(baseUrl, "baseUrl must not be empty");
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (StringUtils.hasText(apiKey)) {
                        headers.setBearerAuth(apiKey);
                    }
                })
                .build();
    }

    /**
     * 调用文生图接口
     *
     * @param request 请求体（含 prompt 及各项采样参数）
     * @return HTTP 响应（含 base64 图片列表）
     */
    public ResponseEntity<Txt2ImgResponse> txt2img(Txt2ImgRequest request) {
        Assert.notNull(request, "request must not be null");
        return restClient.post()
                .uri(TXT2IMG_PATH)
                .body(request)
                .retrieve()
                .toEntity(Txt2ImgResponse.class);
    }

    /**
     * 调用图生图接口（img2img）
     *
     * <p>支持通过 {@link Img2ImgRequest#getAlwaysonScripts()} 透传 ControlNet 参数，
     * 实现姿势控制（Pose）和面料转换（Fabric）。</p>
     *
     * @param request 请求体（含 init_images、denoising_strength、alwayson_scripts 等）
     * @return HTTP 响应（含 base64 图片列表）
     */
    public ResponseEntity<Img2ImgResponse> img2img(Img2ImgRequest request) {
        Assert.notNull(request, "request must not be null");
        return restClient.post()
                .uri(IMG2IMG_PATH)
                .body(request)
                .retrieve()
                .toEntity(Img2ImgResponse.class);
    }

    /**
     * 调用超分辨率接口（extras-single-image）
     *
     * @param request 请求体（含 image base64、upscaler_1、upscaling_resize 等）
     * @return HTTP 响应（含超分后的 base64 图片）
     */
    public ResponseEntity<ExtraSingleImageResponse> extraSingleImage(ExtraSingleImageRequest request) {
        Assert.notNull(request, "request must not be null");
        return restClient.post()
                .uri(EXTRAS_SINGLE_PATH)
                .body(request)
                .retrieve()
                .toEntity(ExtraSingleImageResponse.class);
    }

    // ========== 请求 / 响应 DTO ==========

    /**
     * {@code /sdapi/v1/txt2img} 请求体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Txt2ImgRequest {

        /**
         * 正面提示词（必填）
         */
        @JsonProperty("prompt")
        private String prompt;

        /**
         * 负面提示词
         */
        @JsonProperty("negative_prompt")
        private String negativePrompt;

        /**
         * 图片宽度（像素）
         */
        @JsonProperty("width")
        private Integer width;

        /**
         * 图片高度（像素）
         */
        @JsonProperty("height")
        private Integer height;

        /**
         * 推理步数
         */
        @JsonProperty("steps")
        private Integer steps;

        /**
         * CFG Scale
         */
        @JsonProperty("cfg_scale")
        private Float cfgScale;

        /**
         * 采样器名称
         */
        @JsonProperty("sampler_name")
        private String samplerName;

        /**
         * 随机种子，-1 表示随机
         */
        @JsonProperty("seed")
        private Long seed;

        /**
         * 每次请求生成图片数量
         */
        @JsonProperty("batch_size")
        private Integer batchSize;

        /**
         * 覆盖服务器设置（如指定模型）
         */
        @JsonProperty("override_settings")
        private StableDiffusionWebUiImageOptions.OverrideSettings overrideSettings;

    }

    /**
     * {@code /sdapi/v1/txt2img} 响应体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Txt2ImgResponse {

        /**
         * base64 编码的图片列表（一个请求可能返回多张）
         */
        @JsonProperty("images")
        private List<String> images;

        /**
         * 实际使用的参数（服务器回填）
         */
        @JsonProperty("parameters")
        private Object parameters;

        /**
         * 生成信息（JSON 字符串，含 seed、model hash 等元数据）
         */
        @JsonProperty("info")
        private String info;

    }

    // ========== img2img ==========

    /**
     * {@code /sdapi/v1/img2img} 请求体
     *
     * <p>主要用于 ControlNet 姿势控制（Pose）和面料转换（Fabric img2img）。
     * ControlNet 参数通过 {@code alwayson_scripts.controlnet} 透传。</p>
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Img2ImgRequest {

        /**
         * 初始图片列表（base64 编码）
         */
        @JsonProperty("init_images")
        private List<String> initImages;

        /**
         * 降噪强度，0~1；值越大改动越大，默认 0.75
         */
        @JsonProperty("denoising_strength")
        private Float denoisingStrength;

        /**
         * 正向提示词
         */
        @JsonProperty("prompt")
        private String prompt;

        /**
         * 负向提示词
         */
        @JsonProperty("negative_prompt")
        private String negativePrompt;

        /**
         * 推理步数
         */
        @JsonProperty("steps")
        private Integer steps;

        /**
         * CFG Scale
         */
        @JsonProperty("cfg_scale")
        private Float cfgScale;

        /**
         * 采样器名称
         */
        @JsonProperty("sampler_name")
        private String samplerName;

        /**
         * 随机种子，-1 表示随机
         */
        @JsonProperty("seed")
        private Long seed;

        /**
         * 图片宽度（像素）
         */
        @JsonProperty("width")
        private Integer width;

        /**
         * 图片高度（像素）
         */
        @JsonProperty("height")
        private Integer height;

        /**
         * 透传脚本参数，如 ControlNet：
         * <pre>{@code
         * {
         *   "controlnet": {
         *     "args": [{ "enabled": true, "image": "<base64>", "module": "openpose", "model": "control_openpose-fp16 [9ca67cc5]" }]
         *   }
         * }
         * }</pre>
         */
        @JsonProperty("alwayson_scripts")
        private Map<String, Object> alwaysonScripts;

        /**
         * 覆盖服务器设置（如指定模型）
         */
        @JsonProperty("override_settings")
        private StableDiffusionWebUiImageOptions.OverrideSettings overrideSettings;

    }

    /**
     * {@code /sdapi/v1/img2img} 响应体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Img2ImgResponse {

        /**
         * base64 编码的图片列表
         */
        @JsonProperty("images")
        private List<String> images;

        /**
         * 实际使用的参数（服务器回填）
         */
        @JsonProperty("parameters")
        private Object parameters;

        /**
         * 生成信息（JSON 字符串，含 seed、model hash 等元数据）
         */
        @JsonProperty("info")
        private String info;

    }

    // ========== extras-single-image ==========

    /**
     * {@code /sdapi/v1/extra-single-image} 请求体
     *
     * <p>用于对单张图片执行超分辨率处理。</p>
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExtraSingleImageRequest {

        /**
         * 待超分的图片（base64 编码，不含 data URI 前缀）
         */
        @JsonProperty("image")
        private String image;

        /**
         * 超分倍数（如 2 表示 2x）
         */
        @JsonProperty("upscaling_resize")
        private Integer upscalingResize;

        /**
         * 超分模型名称，如 {@code R-ESRGAN 4x+}、{@code Lanczos}
         */
        @JsonProperty("upscaler_1")
        private String upscaler1;

        /**
         * 第二超分模型（可选）
         */
        @JsonProperty("upscaler_2")
        private String upscaler2;

        /**
         * 第二超分模型混合比例，0~1
         */
        @JsonProperty("extras_upscaler_2_visibility")
        private Float extrasUpscaler2Visibility;

    }

    /**
     * {@code /sdapi/v1/extra-single-image} 响应体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtraSingleImageResponse {

        /**
         * 超分后的图片（base64 编码）
         */
        @JsonProperty("image")
        private String image;

        /**
         * HTML 格式的处理信息
         */
        @JsonProperty("html_info")
        private String htmlInfo;

    }

}
