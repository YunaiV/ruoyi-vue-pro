package cn.iocoder.yudao.module.ai.service.image;

import lombok.Data;
import org.springframework.ai.image.ImageOptions;

/**
 * @author fansili
 * @time 2024/6/5 10:34
 * @since 1.0
 */
@Data
public class MidjourneyImageOptions implements ImageOptions {
    /**
     * 模型
     */
    private String model;
    /**
     * 宽度
     */
    private Integer width;
    /**
     * 高度
     */
    private Integer height;
    /**
     * 版本
     */
    private String version;
    /**
     * 参数
     */
    private String state;

    @Override
    public Integer getN() {
        return 0;
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
        return "";
    }
}
