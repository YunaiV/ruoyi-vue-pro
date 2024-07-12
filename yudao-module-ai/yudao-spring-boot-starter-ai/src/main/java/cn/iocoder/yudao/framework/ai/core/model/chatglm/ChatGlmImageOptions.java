package cn.iocoder.yudao.framework.ai.core.model.chatglm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import org.springframework.ai.image.ImageOptions;

/**
 * chatglm
 * api地址：https://open.bigmodel.cn/dev/api#cogview
 */
@Setter
public class ChatGlmImageOptions implements ImageOptions {

    @JsonProperty("n")
    private Integer n;

    @JsonProperty("model")
    private String model = "cogview-3";

    @JsonProperty("size_width")
    private Integer width;

    @JsonProperty("size_height")
    private Integer height;

    @JsonProperty("size")
    private String size;

    @JsonProperty("style")
    private String style;

    @JsonProperty("user_id")
    private String user;

    @JsonProperty("responseFormat")
    private String responseFormat;

    // ==== build


    public static ChatGlmImageOptions.Builder builder() {
        return new ChatGlmImageOptions.Builder();
    }

    public static class Builder {

        private final ChatGlmImageOptions options;

        private Builder() {
            this.options = new ChatGlmImageOptions();
        }

        public ChatGlmImageOptions.Builder withN(Integer n) {
            options.setN(n);
            return this;
        }

        public ChatGlmImageOptions.Builder withModel(String model) {
            options.setModel(model);
            return this;
        }

        public ChatGlmImageOptions.Builder withWidth(Integer width) {
            options.setWidth(width);
            return this;
        }

        public ChatGlmImageOptions.Builder withHeight(Integer height) {
            options.setHeight(height);
            return this;
        }

        public ChatGlmImageOptions.Builder withStyle(String style) {
            options.setStyle(style);
            return this;
        }

        public ChatGlmImageOptions.Builder withUser(String user) {
            options.setUser(user);
            return this;
        }

        public ChatGlmImageOptions build() {
            return options;
        }

    }

    // ==== get

    @Override
    public Integer getN() {
        return n;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public Integer getWidth() {
        return width;
    }

    @Override
    public Integer getHeight() {
        return height;
    }

    @Override
    public String getResponseFormat() {
        return responseFormat;
    }
}
