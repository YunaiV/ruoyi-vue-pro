package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baidu.BaiduSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax.MiniMaxSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.TencentSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiSpeechModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiModelFactoryImpl} 语音合成相关的单元测试类：按平台创建、按配置缓存、模型绑定在实例上
 *
 * 只创建对象，不发起请求
 *
 * @author 芋道源码
 */
public class AiModelFactoryImplSpeechTest {

    private static final String URL = "http://127.0.0.1:9";

    private final AiModelFactoryImpl modelFactory = new AiModelFactoryImpl();

    @Test
    public void testGetOrCreateTextToSpeechModel() {
        assertInstanceOf(TongYiSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.ALIBABA, "speech-factory-key-1", URL, "qwen3-tts-flash"));
        assertInstanceOf(MiniMaxSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-1", URL, "speech-2.8-hd"));
        assertInstanceOf(DouBaoSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.BYTEDANCE, "speech-factory-key-1", URL, "seed-tts-2.0"));
        assertInstanceOf(BaiduSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.BAIDU, "speech-factory-key-1", URL, "baidu-tts"));
        // 腾讯云：原生为异步任务，由 TencentSpeechModel 统一为“提交并得到音频”
        assertInstanceOf(TencentSpeechModel.class, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.TENCENT, "AKIDspeech|speechKey", null, "tencent-tts"));

        // 相同配置复用实例；不同模型是不同实例，模型绑定在实例上
        AbstractAiSpeechModel hd = (AbstractAiSpeechModel) modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-2", URL, "speech-2.8-hd");
        assertSame(hd, modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-2", URL, "speech-2.8-hd"));
        AiSpeechModel turbo = modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.MINIMAX, "speech-factory-key-2", URL, "speech-2.8-turbo");
        assertNotSame(hd, turbo);
        assertEquals("speech-2.8-hd", hd.getModel());
        assertEquals("speech-2.8-hd", hd.getDefaultOptions().getModel());
        // 请求不能切换已绑定的模型（在发请求前拒绝）
        assertThrows(IllegalArgumentException.class, () -> hd.call(new TextToSpeechPrompt("你好",
                TextToSpeechOptions.builder().model("speech-2.8-turbo").voice("v").build())));
    }

    @Test
    public void testGetOrCreateTextToSpeechModel_unsupported() {
        // 腾讯云的密钥格式不正确：创建时拒绝
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.TENCENT, "bad-key", null, "tencent-tts"));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.OPENAI, "sk", URL, "tts-1"));
        // 不在支持列表的模型
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTextToSpeechModel(
                AiPlatformEnum.ALIBABA, "speech-factory-key-3", URL, "cosyvoice-v2"));
    }

    @Test
    public void testGetOrCreateTencentTtsApi() {
        TencentTtsApi api = modelFactory.getOrCreateTencentTtsApi("AKIDfactory|factoryKey", null);
        assertSame(api, modelFactory.getOrCreateTencentTtsApi("AKIDfactory|factoryKey", null));
        assertThrows(IllegalArgumentException.class, () -> modelFactory.getOrCreateTencentTtsApi("bad-key", null));
    }

}
