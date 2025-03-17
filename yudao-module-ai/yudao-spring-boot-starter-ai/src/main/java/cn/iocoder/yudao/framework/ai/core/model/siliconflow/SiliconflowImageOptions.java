package cn.iocoder.yudao.framework.ai.core.model.siliconflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.openai.OpenAiImageOptions;

/**
 * 硅基流动画图能力
 *
 * @author zzt
 */
@Data
public class SiliconflowImageOptions implements ImageOptions {

    @JsonProperty("model")
    private String model;

    @JsonProperty("negative_prompt")
    private String negativePrompt;

    /**
     * The number of images to generate. Must be between 1 and 4.
     */
    @JsonProperty("image_size")
    private String imageSize;

    /**
     * The number of images to generate. Must be between 1 and 4.
     */
    @JsonProperty("batch_size")
    private Integer batchSize = 1;

    /**
     * number of inference steps
     */
    @JsonProperty("num_inference_steps")
    private Integer numInferenceSteps = 25;


    /**
     * This value is used to control the degree of match between the generated image and the given prompt. The higher the value, the more the generated image will tend to strictly match the text prompt. The lower the value, the more creative and diverse the generated image will be, potentially containing more unexpected elements.
     *
     * Required range: 0 <= x <= 20
     */
    @JsonProperty("guidance_scale")
    private Float guidanceScale = 0.75F;

    /**
     * 如果想要每次都生成固定的图片，可以把seed设置为固定值。
     *
     */
    @JsonProperty("seed")
    private Integer seed =  (int)(Math.random() * 1_000_000_000);

    /**
     * The image that needs to be uploaded should be converted into base64 format.
     */
    @JsonProperty("image")
    private String image;


    /**
     * 宽
     */
    private Integer width;


    /**
     * 高
     */
    private Integer height;

    public void setHeight(Integer height) {
        this.height = height;
        if (this.width != null && this.height != null) {
            this.imageSize = this.width + "x" + this.height;
        }
    }

    public void setWidth(Integer width) {
        this.width = width;
        if (this.width != null && this.height != null) {
            this.imageSize = this.width + "x" + this.height;
        }
    }

    /**
     * 硅基流动
     * @return
     */
    public static SiliconflowImageOptions.Builder builder() {
        return new SiliconflowImageOptions.Builder();
    }

    @Override
    public String toString() {

        return "SiliconflowImageOptions{" + "model='" + getModel() + '\'' + ", batch_size=" + batchSize + ", imageSize=" + imageSize + ", negativePrompt='"
                + negativePrompt + '\'' + '}';
    }

    @Override
    public Integer getN() {
        return null;
    }

    @Override
    public String getResponseFormat() {
        return null;
    }

    @Override
    public String getStyle() {
        return null;
    }

    public static class Builder extends OpenAiImageOptions{

        private final SiliconflowImageOptions options;

        private Builder() {
            this.options = new SiliconflowImageOptions();
        }

        public SiliconflowImageOptions.Builder model(String model) {
            this.options.setModel(model);
            return this;
        }

        public SiliconflowImageOptions.Builder withBatchSize(Integer batchSize) {
            options.setBatchSize(batchSize);
            return this;
        }

        public SiliconflowImageOptions.Builder withModel(String model) {
            options.setModel(model);
            return this;
        }

        public SiliconflowImageOptions.Builder withWidth(Integer width) {
            options.setWidth(width);
            return this;
        }

        public SiliconflowImageOptions.Builder withHeight(Integer height) {
            options.setHeight(height);
            return this;
        }

        public SiliconflowImageOptions.Builder withSeed(Integer seed) {
            options.setSeed(seed);
            return this;
        }

        public SiliconflowImageOptions.Builder withNegativePrompt(String negativePrompt) {
            options.setNegativePrompt(negativePrompt);
            return this;
        }

        public SiliconflowImageOptions build() {
            return options;
        }

    }
}
