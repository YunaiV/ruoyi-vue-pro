package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiChatMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AiModelServiceImpl} 文字识别相关的单元测试类
 *
 * 重点：获取 OcrModel 前，校验模型类型、模型与 API 密钥的平台一致、平台支持文字识别；密钥格式错误转成业务异常
 *
 * @author 芋道源码
 */
public class AiModelServiceImplOcrTest extends BaseMockitoUnitTest {

    private static final Long MODEL_ID = 1L;
    private static final Long KEY_ID = 2L;
    private static final String API_KEY = "AKIDtest|secretTest";
    private static final String URL = "http://127.0.0.1";

    @InjectMocks
    private AiModelServiceImpl modelService;

    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private AiChatMapper modelMapper;
    @Mock
    private AiModelFactory modelFactory;

    // ========== getOcrModel ==========

    @Test
    public void testGetOcrModel_success() {
        // mock 数据
        mockModelAndApiKey(AiPlatformEnum.TENCENT, "GeneralAccurateOCR", AiModelTypeEnum.OCR, AiPlatformEnum.TENCENT);
        // mock 方法
        OcrModel ocrModel = mock(OcrModel.class);
        when(modelFactory.getOrCreateOcrModel(AiPlatformEnum.TENCENT, API_KEY, URL, "GeneralAccurateOCR"))
                .thenReturn(ocrModel);

        // 调用，并断言
        assertSame(ocrModel, modelService.getOcrModel(MODEL_ID));
        // 断言：获取时不写库
        verify(modelMapper, never()).updateForSave(any(AiModelDO.class));
    }

    @Test
    public void testGetOcrModel_typeError() {
        // mock 数据
        mockModelAndApiKey(AiPlatformEnum.ALIBABA, "qwen-plus", AiModelTypeEnum.CHAT, AiPlatformEnum.ALIBABA);

        // 调用，并断言异常：不创建 OcrModel
        assertServiceException(() -> modelService.getOcrModel(MODEL_ID), MODEL_USE_TYPE_ERROR);
        verifyNoInteractions(modelFactory);
    }

    @Test
    public void testGetOcrModel_platformMismatch() {
        // mock 数据
        mockModelAndApiKey(AiPlatformEnum.TENCENT, "GeneralAccurateOCR", AiModelTypeEnum.OCR, AiPlatformEnum.BAIDU);

        // 调用，并断言异常：不创建 OcrModel
        assertServiceException(() -> modelService.getOcrModel(MODEL_ID), OCR_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.TENCENT.getPlatform(), AiPlatformEnum.BAIDU.getPlatform());
        verifyNoInteractions(modelFactory);
    }

    @Test
    public void testGetOcrModel_platformNotSupported() {
        // mock 数据
        mockModelAndApiKey(AiPlatformEnum.OPENAI, "gpt-4o", AiModelTypeEnum.OCR, AiPlatformEnum.OPENAI);

        // 调用，并断言异常：不创建 OcrModel
        assertServiceException(() -> modelService.getOcrModel(MODEL_ID), OCR_MODEL_PLATFORM_NOT_SUPPORTED,
                AiPlatformEnum.OPENAI.getPlatform());
        verifyNoInteractions(modelFactory);
    }

    @Test
    public void testGetOcrModel_configInvalid() {
        // mock 数据
        mockModelAndApiKey(AiPlatformEnum.TENCENT, "NotExistsOCR", AiModelTypeEnum.OCR, AiPlatformEnum.TENCENT);
        OcrException ex = new OcrException("TENCENT", OcrException.Type.INVALID_CONFIG, "不支持的服务代码：NotExistsOCR");
        // mock 方法：工厂构造时校验失败
        when(modelFactory.getOrCreateOcrModel(AiPlatformEnum.TENCENT, API_KEY, URL, "NotExistsOCR")).thenThrow(ex);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getOcrModel(MODEL_ID), OCR_CONFIG_INVALID, ex.getMessage());
    }

    @Test
    public void testGetOcrModel_modelDisabled() {
        // mock 数据：模型禁用
        when(modelMapper.selectById(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(KEY_ID)
                .setType(AiModelTypeEnum.OCR.getType()).setStatus(CommonStatusEnum.DISABLE.getStatus()));

        // 调用，并断言异常：不查密钥、不创建 OcrModel
        assertServiceException(() -> modelService.getOcrModel(MODEL_ID), MODEL_DISABLE);
        verifyNoInteractions(apiKeyService, modelFactory);
    }

    // ========== getOcrModelList ==========

    @Test
    public void testGetOcrModelList() {
        // mock 数据：可用、密钥禁用、平台不一致、平台不支持、密钥不存在
        AiModelDO ok = buildModel(10L, 100L, AiPlatformEnum.BAIDU);
        AiModelDO keyDisabled = buildModel(11L, 101L, AiPlatformEnum.TENCENT);
        AiModelDO mismatch = buildModel(12L, 102L, AiPlatformEnum.BYTEDANCE);
        AiModelDO notSupported = buildModel(13L, 103L, AiPlatformEnum.OPENAI);
        AiModelDO keyNotExists = buildModel(14L, 999L, AiPlatformEnum.ALIBABA);
        when(modelMapper.selectListByStatusAndType(CommonStatusEnum.ENABLE.getStatus(), AiModelTypeEnum.OCR.getType(), null))
                .thenReturn(Arrays.asList(ok, keyDisabled, mismatch, notSupported, keyNotExists));
        when(apiKeyService.getApiKeyList()).thenReturn(Arrays.asList(
                buildApiKey(100L, AiPlatformEnum.BAIDU, CommonStatusEnum.ENABLE),
                buildApiKey(101L, AiPlatformEnum.TENCENT, CommonStatusEnum.DISABLE),
                buildApiKey(102L, AiPlatformEnum.TENCENT, CommonStatusEnum.ENABLE),
                buildApiKey(103L, AiPlatformEnum.OPENAI, CommonStatusEnum.ENABLE)));

        // 调用
        List<AiModelDO> list = modelService.getOcrModelList();
        // 断言：只保留可用的
        assertEquals(Collections.singletonList(ok), list);
        verifyNoInteractions(modelFactory);
    }

    // ========== createModel、updateModel ==========

    @Test
    public void testCreateModel_ocrClearChatFields() {
        // mock 方法
        when(apiKeyService.validateApiKey(KEY_ID)).thenReturn(buildApiKey(KEY_ID, AiPlatformEnum.BAIDU, CommonStatusEnum.ENABLE));
        when(modelFactory.getOrCreateOcrModel(AiPlatformEnum.BAIDU, API_KEY, URL, "accurate_basic"))
                .thenReturn(mock(OcrModel.class));
        // 准备参数：带上对话参数
        AiModelSaveReqVO reqVO = new AiModelSaveReqVO();
        reqVO.setKeyId(KEY_ID);
        reqVO.setName("百度高精度");
        reqVO.setModel("accurate_basic");
        reqVO.setPlatform(AiPlatformEnum.BAIDU.getPlatform());
        reqVO.setType(AiModelTypeEnum.OCR.getType());
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setTemperature(0.8);
        reqVO.setMaxTokens(4096);
        reqVO.setMaxContexts(10);

        // 调用
        modelService.createModel(reqVO);
        // 断言：对话参数归一为 null
        ArgumentCaptor<AiModelDO> captor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(modelMapper).insert(captor.capture());
        AiModelDO model = captor.getValue();
        assertEquals("accurate_basic", model.getModel());
        assertNull(model.getTemperature());
        assertNull(model.getMaxTokens());
        assertNull(model.getMaxContexts());
        assertNull(model.getInputModalities());
        assertNull(model.getReasoningConfig());
    }

    @Test
    public void testCreateModel_ocrConfigInvalid() {
        // mock 方法：工厂构造时校验失败
        when(apiKeyService.validateApiKey(KEY_ID)).thenReturn(buildApiKey(KEY_ID, AiPlatformEnum.ALIBABA, CommonStatusEnum.ENABLE));
        OcrException ex = new OcrException("ALIBABA", OcrException.Type.INVALID_CONFIG, "模型 qwen-plus 不是 Qwen-OCR 模型");
        when(modelFactory.getOrCreateOcrModel(AiPlatformEnum.ALIBABA, API_KEY, URL, "qwen-plus")).thenThrow(ex);
        // 准备参数
        AiModelSaveReqVO reqVO = new AiModelSaveReqVO();
        reqVO.setKeyId(KEY_ID);
        reqVO.setName("通义");
        reqVO.setModel("qwen-plus");
        reqVO.setPlatform(AiPlatformEnum.ALIBABA.getPlatform());
        reqVO.setType(AiModelTypeEnum.OCR.getType());
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用，并断言异常：不插入
        assertServiceException(() -> modelService.createModel(reqVO), OCR_CONFIG_INVALID, ex.getMessage());
        verify(modelMapper, never()).insert(any(AiModelDO.class));
    }

    @Test
    public void testUpdateModel_ocrClearChatFields() {
        // mock 数据
        when(modelMapper.selectById(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID));
        // mock 方法
        when(apiKeyService.validateApiKey(KEY_ID)).thenReturn(buildApiKey(KEY_ID, AiPlatformEnum.BYTEDANCE, CommonStatusEnum.ENABLE));
        when(modelFactory.getOrCreateOcrModel(AiPlatformEnum.BYTEDANCE, API_KEY, URL, null))
                .thenReturn(mock(OcrModel.class));
        // 准备参数：带上对话参数
        AiModelSaveReqVO reqVO = new AiModelSaveReqVO();
        reqVO.setId(MODEL_ID);
        reqVO.setKeyId(KEY_ID);
        reqVO.setName("火山通用");
        reqVO.setPlatform(AiPlatformEnum.BYTEDANCE.getPlatform());
        reqVO.setType(AiModelTypeEnum.OCR.getType());
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setTemperature(0.5);
        reqVO.setMaxTokens(1024);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：对话参数归一为 null
        ArgumentCaptor<AiModelDO> captor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(modelMapper).updateForSave(captor.capture());
        assertEquals(MODEL_ID, captor.getValue().getId());
        assertNull(captor.getValue().getTemperature());
        assertNull(captor.getValue().getMaxTokens());
        verify(modelFactory).getOrCreateOcrModel(AiPlatformEnum.BYTEDANCE, API_KEY, URL, null);
    }

    private void mockModelAndApiKey(AiPlatformEnum modelPlatform, String model, AiModelTypeEnum type,
                                    AiPlatformEnum apiKeyPlatform) {
        when(modelMapper.selectById(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(KEY_ID)
                .setPlatform(modelPlatform.getPlatform()).setModel(model)
                .setType(type.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(apiKeyService.validateApiKey(KEY_ID)).thenReturn(buildApiKey(KEY_ID, apiKeyPlatform, CommonStatusEnum.ENABLE));
    }

    private static AiModelDO buildModel(Long id, Long keyId, AiPlatformEnum platform) {
        return new AiModelDO().setId(id).setKeyId(keyId).setPlatform(platform.getPlatform())
                .setType(AiModelTypeEnum.OCR.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private static AiApiKeyDO buildApiKey(Long id, AiPlatformEnum platform, CommonStatusEnum status) {
        return new AiApiKeyDO().setId(id).setPlatform(platform.getPlatform()).setApiKey(API_KEY).setUrl(URL)
                .setStatus(status.getStatus());
    }

}
