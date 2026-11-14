package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.baidu.BaiduTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.deepseek.DeepSeekTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.tencent.TencentTranscriptionModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.tongyi.TongYiTranscriptionModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;

import static org.junit.jupiter.api.Assertions.*;

// TODO @AI：能不能把多个 fatory 的单测整合起来；这样有点怪哈；/Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-ai/src/test/java/cn/iocoder/yudao/module/ai/framework/ai/core/model/AiModelFactoryImplAsrTest.java
///Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-ai/src/test/java/cn/iocoder/yudao/module/ai/framework/ai/core/model/AiModelFactoryImplSpeechTest.java
///Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-ai/src/test/java/cn/iocoder/yudao/module/ai/framework/ai/core/model/AiModelFactoryImplOcrTest.java
///Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-ai/src/test/java/cn/iocoder/yudao/module/ai/framework/ai/core/model/AiModelFactoryImplImageTest.java
/**
 * {@link AiModelFactoryImpl} 语音识别相关的单元测试类：按平台创建、按配置缓存
 *
 * 只创建对象，不发起请求
 *
 * @author 芋道源码
 */
public class AiModelFactoryImplAsrTest {

    private static final String URL = "http://127.0.0.1:9";

    private final AiModelFactoryImpl modelFactory = new AiModelFactoryImpl();

    @Test
    public void testGetOrCreateTranscriptionModel() {
        TranscriptionModel model = modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.DEEPSEEK, "asr-factory-key-1", URL, "deepseek-asr");
        assertInstanceOf(DeepSeekTranscriptionModel.class, model);
        assertSame(model, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.DEEPSEEK, "asr-factory-key-1", URL, "deepseek-asr"));
        assertInstanceOf(TongYiTranscriptionModel.class, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.ALIBABA, "asr-factory-key-1", URL, "qwen3-asr-flash"));
        assertInstanceOf(TencentTranscriptionModel.class, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.TENCENT, "AKIDasr|asrKey", null, "16k_zh"));
        assertInstanceOf(BaiduTranscriptionModel.class, modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.BAIDU, "asr-factory-key-1", URL, "1537"));
        // 模型不正确：腾讯云不是 16k 引擎、百度不是 dev_pid
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.TENCENT, "AKIDasr|asrKey", null, "8k_zh"));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.BAIDU, "asr-factory-key-1", URL, "baidu-asr"));
        // 不支持的平台
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTranscriptionModel(
                AiPlatformEnum.OPENAI, "sk", URL, "whisper-1"));
    }

}
