package cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.image.ImageOptions;

/**
 * Stable Diffusion WebUI (AUTOMATIC1111) 文生图请求参数
 *
 * <p>对应 {@code POST /sdapi/v1/txt2img} 请求体。
 * 参数说明参考 <a href="https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki/API">SD WebUI API</a>。</p>
 *
 * @author deepay
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StableDiffusionWebUiImageOptions implements ImageOptions {

    /**
     * 模型名称（override_settings.sd_model_checkpoint），留空使用服务器当前加载的模型
     */
    @JsonProperty("override_settings")
    private OverrideSettings overrideSettings;

    /**
     * 负面提示词
     */
    @JsonProperty("negative_prompt")
    private String negativePrompt;

    /**
     * 生成图片宽度（像素），默认 512
     */
    @JsonProperty("width")
    @Builder.Default
    private Integer width = 512;

    /**
     * 生成图片高度（像素），默认 512
     */
    @JsonProperty("height")
    @Builder.Default
    private Integer height = 512;

    /**
     * 推理步数，越高质量越好但耗时越长，默认 20
     */
    @JsonProperty("steps")
    @Builder.Default
    private Integer steps = 20;

    /**
     * CFG Scale：提示词遵循程度，范围 1~30，默认 7
     */
    @JsonProperty("cfg_scale")
    @Builder.Default
    private Float cfgScale = 7.0F;

    /**
     * 采样器名称，如 Euler a、DPM++ 2M Karras 等
     */
    @JsonProperty("sampler_name")
    @Builder.Default
    private String samplerName = "Euler a";

    /**
     * 随机种子，-1 表示随机
     */
    @JsonProperty("seed")
    @Builder.Default
    private Long seed = -1L;

    /**
     * 批量生成数量（每次请求的图片数量），默认 1
     */
    @JsonProperty("batch_size")
    @Builder.Default
    private Integer batchSize = 1;

    // ========== Spring AI ImageOptions 接口实现 ==========

    @Override
    public Integer getN() {
        return batchSize;
    }

    @Override
    public String getModel() {
        return overrideSettings != null ? overrideSettings.getSdModelCheckpoint() : null;
    }

    @Override
    public String getResponseFormat() {
        // SD WebUI 始终返回 base64
        return "b64_json";
    }

    @Override
    public String getStyle() {
        return null;
    }

    // ========== 内嵌类 ==========

    /**
     * override_settings 字段，用于在请求中指定模型
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OverrideSettings {

        /**
         * 模型权重名称（不含扩展名），如 {@code v1-5-pruned-emaonly}
         */
        @JsonProperty("sd_model_checkpoint")
        private String sdModelCheckpoint;

    }

}
