package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.baidu.BaiduOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.siliconflow.SiliconFlowOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tencent.TencentOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tongyi.TongYiOcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.volcengine.VolcEngineOcrModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiModelFactoryImpl} 文字识别相关的单元测试类：按平台创建、按配置缓存、非法平台与密钥格式
 *
 * 只创建对象，不发起请求
 *
 * @author 芋道源码
 */
public class AiModelFactoryImplOcrTest {

    private static final String URL = "http://127.0.0.1:9";

    private final AiModelFactoryImpl modelFactory = new AiModelFactoryImpl();

    @Test
    public void testGetOrCreateOcrModel() {
        assertInstanceOf(TongYiOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.ALIBABA, "ocr-factory-key-1", null, "qwen-vl-ocr"));
        assertInstanceOf(SiliconFlowOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.SILICONFLOW, "ocr-factory-key-1", URL, "deepseek-ai/DeepSeek-OCR"));
        assertInstanceOf(TencentOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.TENCENT, "AKIDfactory|factoryKey", null, null));
        assertInstanceOf(BaiduOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.BAIDU, "bce-v3/ALTAK-factory/factoryKey", URL, "accurate_basic"));
        assertInstanceOf(VolcEngineOcrModel.class, modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.BYTEDANCE, "AKfactory|SKfactory", null, null));
    }

    @Test
    public void testGetOrCreateOcrModel_cache() {
        // 相同配置复用实例
        OcrModel model = modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", URL, null);
        assertSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", URL, null));
        // 密钥、地址、模型任一变化，都是新实例，不复用旧凭证
        assertNotSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret-2", URL, null));
        assertNotSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", "http://127.0.0.1:10", null));
        assertNotSame(model, modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, "factory-id|factory-secret", URL, "accurate_basic"));
    }

    @Test
    public void testGetOrCreateOcrModel_unsupported() {
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.OPENAI, "sk", URL, "gpt-4o"));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.DEEPSEEK, "sk", URL, "deepseek-chat"));
    }

    @Test
    public void testGetOrCreateOcrModel_invalidConfig() {
        // 组合密钥不是两段：不发请求，直接拒绝，且消息不回显密钥
        OcrException ex = assertThrows(OcrException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.TENCENT, "only-one-secret-part", null, null));
        assertEquals(OcrException.Type.INVALID_CREDENTIAL, ex.getType());
        assertFalse(ex.getMessage().contains("only-one-secret-part"));
        // 非 OCR 模型标识
        ex = assertThrows(OcrException.class, () -> modelFactory.getOrCreateOcrModel(
                AiPlatformEnum.ALIBABA, "ocr-factory-key-2", null, "qwen-plus"));
        assertEquals(OcrException.Type.INVALID_CONFIG, ex.getType());
    }

}
