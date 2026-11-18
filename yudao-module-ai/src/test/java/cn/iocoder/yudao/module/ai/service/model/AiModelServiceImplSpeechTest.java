package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AiModelServiceImpl} 语音合成相关的单元测试类
 *
 * @author 芋道源码
 */
public class AiModelServiceImplSpeechTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AiModelServiceImpl modelService;

    @Mock
    private AiModelFactory modelFactory;

    @Test
    public void testGetTextToSpeechModel_success() {
        // 准备参数
        AiApiKeyDO apiKey = randomPojo(AiApiKeyDO.class, o -> o.setPlatform(AiPlatformEnum.MINIMAX.getPlatform()));
        // mock modelFactory 的方法
        AiSpeechModel speechModel = mock(AiSpeechModel.class);
        when(modelFactory.getOrCreateTextToSpeechModel(eq(AiPlatformEnum.MINIMAX), eq(apiKey.getApiKey()),
                eq(apiKey.getUrl()), eq("speech-2.8-hd"))).thenReturn(speechModel);

        // 调用，并断言：按密钥的平台、地址创建，模型标识绑定在返回的对象上
        assertSame(speechModel, modelService.getTextToSpeechModel(apiKey, "speech-2.8-hd"));
    }

    @Test
    public void testGetTextToSpeechModel_platformInvalid() {
        // 准备参数
        AiApiKeyDO apiKey = randomPojo(AiApiKeyDO.class, o -> o.setPlatform("unknown"));

        // 调用，并断言：平台不存在时，不创建客户端
        assertThrows(IllegalArgumentException.class, () -> modelService.getTextToSpeechModel(apiKey, "tts"));
        verifyNoInteractions(modelFactory);
    }

    @Test
    public void testGetTencentTtsApi() {
        // 准备参数
        AiApiKeyDO apiKey = randomPojo(AiApiKeyDO.class, o -> o.setPlatform(AiPlatformEnum.TENCENT.getPlatform()));
        // mock modelFactory 的方法
        TencentTtsApi tencentTtsApi = mock(TencentTtsApi.class);
        when(modelFactory.getOrCreateTencentTtsApi(eq(apiKey.getApiKey()), eq(apiKey.getUrl()))).thenReturn(tencentTtsApi);

        // 调用，并断言
        assertSame(tencentTtsApi, modelService.getTencentTtsApi(apiKey));
    }

}
