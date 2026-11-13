package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;
import org.springframework.util.Assert;

/**
 * OCR 结果：整张图片的纯文本
 *
 * 厂商成功返回但图片中没有文字时，{@link #getOutput()} 为空字符串（不是 null）。
 *
 * @author 芋道源码
 */
public class OcrResult implements ModelResult<String> {

    private final String text;

    public OcrResult(String text) {
        Assert.notNull(text, "text 不能为 null");
        this.text = text;
    }

    @Override
    public String getOutput() {
        return text;
    }

    @Override
    public ResultMetadata getMetadata() {
        return null; // 首版无，与 Spring AI ImageGeneration(Image) 构造时一致
    }

}
