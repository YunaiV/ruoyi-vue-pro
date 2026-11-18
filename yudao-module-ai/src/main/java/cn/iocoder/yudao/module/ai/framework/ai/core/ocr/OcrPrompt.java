package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import org.springframework.ai.content.Media;
import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;
import org.springframework.util.Assert;

/**
 * OCR 请求：单张图片
 *
 * 图片由服务端用已校验的字节构造：{@code new Media(mimeType, new ByteArrayResource(bytes))}，不接受 URL。
 * 首版没有请求级参数，{@link #getOptions()} 返回 null（与 Spring AI ImagePrompt(List) 构造时一致）；
 * 模型标识 / 服务代码在构造 {@link OcrModel} 时绑定，请求里不能覆盖。
 *
 * @author 芋道源码
 */
public class OcrPrompt implements ModelRequest<Media> {

    private final Media image;

    public OcrPrompt(Media image) {
        Assert.notNull(image, "image 不能为空");
        this.image = image;
    }

    @Override
    public Media getInstructions() {
        return image;
    }

    @Override
    public ModelOptions getOptions() {
        return null;
    }

}
