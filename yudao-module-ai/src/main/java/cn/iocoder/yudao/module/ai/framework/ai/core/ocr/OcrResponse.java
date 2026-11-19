package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.model.ResponseMetadata;
import org.springframework.util.Assert;

import java.util.List;

/**
 * OCR 响应：首版只有 1 个结果，不按行拆分
 *
 * @author 芋道源码
 */
public class OcrResponse implements ModelResponse<OcrResult> {

    private final List<OcrResult> results;

    public OcrResponse(OcrResult result) {
        Assert.notNull(result, "result 不能为空");
        this.results = List.of(result);
    }

    @Override
    public OcrResult getResult() {
        return results.get(0);
    }

    @Override
    public List<OcrResult> getResults() {
        return results;
    }

    @Override
    public ResponseMetadata getMetadata() {
        return null; // 首版无
    }

}
