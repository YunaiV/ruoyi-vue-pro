package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiChatMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import com.volcengine.ark.runtime.service.ArkService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ai.image.ImageModel;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.IMAGE_MODEL_PLATFORM_MISMATCH;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * {@link AiModelServiceImpl} 绘图相关的单元测试类
 *
 * 重点：获取绘图客户端（含豆包 Seedream 的 ArkService）前，校验模型的平台与 API 密钥的平台一致；并使用模型标识创建客户端
 *
 * @author 芋道源码
 */
public class AiModelServiceImplImageTest extends BaseMockitoUnitTest {

    private static final Long MODEL_ID = 1L;
    private static final Long KEY_ID = 2L;
    private static final String API_KEY = "sk-test";
    private static final String URL = "http://127.0.0.1";

    @InjectMocks
    private AiModelServiceImpl modelService;

    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private AiChatMapper modelMapper;
    @Mock
    private AiModelFactory modelFactory;

    // ========== getImageModel ==========

    @Test
    public void testGetImageModel_success() {
        // mock 数据：Stability AI 模型与密钥
        mockModelAndApiKey(AiPlatformEnum.STABLE_DIFFUSION, "stable-diffusion-xl-1024-v1-0", AiPlatformEnum.STABLE_DIFFUSION);
        ImageModel imageModel = mock(ImageModel.class);
        when(modelFactory.getOrCreateImageModel(AiPlatformEnum.STABLE_DIFFUSION, API_KEY, URL,
                "stable-diffusion-xl-1024-v1-0")).thenReturn(imageModel);

        // 调用，并断言：使用模型标识创建客户端
        assertSame(imageModel, modelService.getImageModel(MODEL_ID));
    }

    @Test
    public void testGetImageModel_platformMismatch() {
        // mock 数据：模型的平台为 OpenAI，API 密钥的平台为硅基流动
        mockModelAndApiKey(AiPlatformEnum.OPENAI, "dall-e-3", AiPlatformEnum.SILICON_FLOW);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getImageModel(MODEL_ID), IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.OPENAI.getPlatform(), AiPlatformEnum.SILICON_FLOW.getPlatform());
        // 断言：不创建客户端
        verifyNoInteractions(modelFactory);
    }

    // ========== getMidjourneyApi ==========

    @Test
    public void testGetMidjourneyApi_success() {
        // mock 数据
        mockModelAndApiKey(AiPlatformEnum.MIDJOURNEY, "midjourney", AiPlatformEnum.MIDJOURNEY);
        MidjourneyApi midjourneyApi = mock(MidjourneyApi.class);
        when(modelFactory.getOrCreateMidjourneyApi(API_KEY, URL)).thenReturn(midjourneyApi);

        // 调用，并断言
        assertSame(midjourneyApi, modelService.getMidjourneyApi(MODEL_ID));
    }

    @Test
    public void testGetMidjourneyApi_platformMismatch() {
        // mock 数据：模型的平台为 Midjourney，API 密钥的平台为 OpenAI
        mockModelAndApiKey(AiPlatformEnum.MIDJOURNEY, "midjourney", AiPlatformEnum.OPENAI);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getMidjourneyApi(MODEL_ID), IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.MIDJOURNEY.getPlatform(), AiPlatformEnum.OPENAI.getPlatform());
        // 断言：不创建客户端
        verifyNoInteractions(modelFactory);
    }

    @Test
    public void testGetMidjourneyApiByApiKey_success() {
        // 准备参数：绘图记录绑定的 Midjourney 密钥
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(KEY_ID).setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform())
                .setApiKey(API_KEY).setUrl(URL);
        // mock 方法
        MidjourneyApi midjourneyApi = mock(MidjourneyApi.class);
        when(modelFactory.getOrCreateMidjourneyApi(API_KEY, URL)).thenReturn(midjourneyApi);

        // 调用，并断言
        assertSame(midjourneyApi, modelService.getMidjourneyApi(apiKey));
    }

    @Test
    public void testGetMidjourneyApiByApiKey_platformMismatch() {
        // 准备参数：绑定的密钥后来被改成了 OpenAI
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(KEY_ID).setPlatform(AiPlatformEnum.OPENAI.getPlatform())
                .setApiKey(API_KEY).setUrl(URL);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getMidjourneyApi(apiKey), IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.MIDJOURNEY.getPlatform(), AiPlatformEnum.OPENAI.getPlatform());
        // 断言：不创建客户端
        verifyNoInteractions(modelFactory);
    }

    // ========== getArkService ==========

    @Test
    public void testGetArkService_success() {
        // mock 数据：豆包 Seedream 模型与密钥
        mockModelAndApiKey(AiPlatformEnum.DOU_BAO, "doubao-seedream-4-5-251128", AiPlatformEnum.DOU_BAO);
        ArkService arkService = mock(ArkService.class);
        when(modelFactory.getOrCreateArkService(API_KEY, URL)).thenReturn(arkService);

        // 调用，并断言
        assertSame(arkService, modelService.getArkService(MODEL_ID));
    }

    @Test
    public void testGetArkService_platformMismatch() {
        // mock 数据：模型的平台为豆包，API 密钥的平台为 OpenAI
        mockModelAndApiKey(AiPlatformEnum.DOU_BAO, "doubao-seedream-4-5-251128", AiPlatformEnum.OPENAI);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getArkService(MODEL_ID), IMAGE_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.DOU_BAO.getPlatform(), AiPlatformEnum.OPENAI.getPlatform());
        // 断言：不创建客户端
        verifyNoInteractions(modelFactory);
    }

    private void mockModelAndApiKey(AiPlatformEnum modelPlatform, String model, AiPlatformEnum apiKeyPlatform) {
        when(modelMapper.selectById(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(KEY_ID)
                .setPlatform(modelPlatform.getPlatform()).setModel(model)
                .setType(AiModelTypeEnum.IMAGE.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(apiKeyService.validateApiKey(KEY_ID)).thenReturn(new AiApiKeyDO().setId(KEY_ID)
                .setPlatform(apiKeyPlatform.getPlatform()).setApiKey(API_KEY).setUrl(URL)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
    }

}
