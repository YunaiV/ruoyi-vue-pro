package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import org.springframework.ai.model.Model;

/**
 * OCR 文字识别模型：基于 Spring AI 泛型 {@link Model} 的自定义扩展（Spring AI 1.1.8 没有官方 OCR 模型）
 *
 * 调用方式与 ImageModel 一致：{@code ocrModel.call(new OcrPrompt(media)).getResult().getOutput()}
 *
 * @author 芋道源码
 */
@FunctionalInterface
public interface OcrModel extends Model<OcrPrompt, OcrResponse> {

    /**
     * 识别单张图片中的文字
     *
     * @param prompt 请求（单张图片）
     * @return 响应（整张图片的纯文本）
     * @throws OcrException 图片不合法、鉴权失败、限流、超时、上游错误等；消息已脱敏
     */
    @Override
    OcrResponse call(OcrPrompt prompt);

}
