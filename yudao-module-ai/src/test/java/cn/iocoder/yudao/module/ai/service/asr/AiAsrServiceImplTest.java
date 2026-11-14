package cn.iocoder.yudao.module.ai.service.asr;

import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.asr.AiAsrDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.asr.AiAsrMapper;
import cn.iocoder.yudao.module.ai.enums.asr.AiAsrStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiAsrServiceImpl} 的单元测试类
 *
 * 重点：成功失败都记录且失败原因不含敏感信息、模型不正确时不记录、【我的】记录隔离
 *
 * @author 芋道源码
 */
public class AiAsrServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long MODEL_ID = 10L;
    private static final Long KEY_ID = 20L;
    private static final Long ASR_ID = 100L;
    private static final String FILE_URL = "https://files.example.com/ai/asr/%E4%BC%9A%E8%AE%AE_1731.wav";
    private static final String ACCESS_URL = FILE_URL + "?sign=1";
    private static final byte[] AUDIO = new byte[]{1, 2, 3};

    @InjectMocks
    private AiAsrServiceImpl asrService;

    @Mock
    private AiAsrMapper asrMapper;
    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private FileApi fileApi;

    // ========== recognizeAsr ==========

    @Test
    public void testRecognizeAsr_success() {
        // mock 方法：按预签名地址下载，识别出文字
        TranscriptionModel transcriptionModel = mockTranscriptionModel(AiPlatformEnum.TENCENT, "16k_zh");
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                .thenReturn(new AudioTranscriptionResponse(new AudioTranscription("你好")));

        // 调用
        AiAsrDO asr = recognizeAsr();
        // 断言：下载的音频原样交给模型
        verify(transcriptionModel).call(argThat(prompt -> {
            try {
                assertArrayEquals(AUDIO, prompt.getInstructions().getContentAsByteArray());
            } catch (Exception e) {
                fail(e);
            }
            return true;
        }));
        // 断言：记录模型快照、文件地址（不带签名）、原文
        AiAsrDO inserted = captureInsert();
        assertSame(asr, inserted);
        assertEquals(USER_ID, inserted.getUserId());
        assertEquals(MODEL_ID, inserted.getModelId());
        assertEquals("16k_zh", inserted.getModelName());
        assertEquals(AiPlatformEnum.TENCENT.getPlatform(), inserted.getPlatform());
        assertEquals("16k_zh", inserted.getModel());
        assertEquals("会议_1731.wav", inserted.getFileName());
        assertEquals(FILE_URL, inserted.getAudioUrl());
        assertEquals(AiAsrStatusEnum.SUCCESS.getStatus(), inserted.getStatus());
        assertEquals("你好", inserted.getOriginalText());
        assertNull(inserted.getErrorMessage());
        assertNotNull(inserted.getDuration());
    }

    @Test
    public void testRecognizeAsr_audioInvalid() {
        // mock 方法：音频校验不通过
        TranscriptionModel transcriptionModel = mockTranscriptionModel(AiPlatformEnum.BAIDU, "1537");
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                .thenThrow(new IllegalArgumentException("音频必须是 WAV 格式"));

        // 调用
        recognizeAsr();
        // 断言：失败也记录
        AiAsrDO inserted = captureInsert();
        assertEquals(AiAsrStatusEnum.FAIL.getStatus(), inserted.getStatus());
        assertEquals(FILE_URL, inserted.getAudioUrl());
        assertNull(inserted.getOriginalText());
        assertEquals("音频不正确：音频必须是 WAV 格式", inserted.getErrorMessage());
    }

    @Test
    public void testRecognizeAsr_platformFail() {
        // mock 方法：平台返回失败，消息已由模型脱敏
        TranscriptionModel transcriptionModel = mockTranscriptionModel(AiPlatformEnum.DEEPSEEK, "deepseek-asr");
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                .thenThrow(new IllegalStateException("DeepSeek 语音识别失败（HTTP 503）：busy"));

        // 调用
        recognizeAsr();
        // 断言
        AiAsrDO inserted = captureInsert();
        assertEquals(AiAsrStatusEnum.FAIL.getStatus(), inserted.getStatus());
        assertEquals("DeepSeek 语音识别失败（HTTP 503）：busy", inserted.getErrorMessage());
    }

    @Test
    public void testRecognizeAsr_unexpectedException() {
        // mock 方法：非预期异常，消息里带有敏感信息
        TranscriptionModel transcriptionModel = mockTranscriptionModel(AiPlatformEnum.ALIBABA, "qwen3-asr-flash");
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                .thenThrow(new RuntimeException("https://dashscope.aliyuncs.com/x?key=secret-token"));

        // 调用
        recognizeAsr();
        // 断言：只给通用提示，不落库原始消息
        AiAsrDO inserted = captureInsert();
        assertEquals(AiAsrStatusEnum.FAIL.getStatus(), inserted.getStatus());
        assertEquals("识别服务异常，请稍后重试", inserted.getErrorMessage());
    }

    @Test
    public void testRecognizeAsr_typeError() {
        // mock 数据：语音生成模型
        when(modelService.validateModel(eq(MODEL_ID))).thenReturn(
                buildModel(AiPlatformEnum.DEEPSEEK, "deepseek-tts").setType(AiModelTypeEnum.VOICE.getType()));

        // 调用，并断言异常：不下载、不记录
        assertServiceException(() -> asrService.recognizeAsr(USER_ID, buildRecognizeReqVO()), MODEL_USE_TYPE_ERROR);
        verify(apiKeyService, never()).validateApiKey(any());
        verifyNoInteractions(fileApi, asrMapper);
    }

    @Test
    public void testRecognizeAsr_platformMismatch() {
        // mock 数据：模型是百度，密钥是阿里云
        when(modelService.validateModel(eq(MODEL_ID))).thenReturn(buildModel(AiPlatformEnum.BAIDU, "1537"));
        when(apiKeyService.validateApiKey(eq(KEY_ID))).thenReturn(buildApiKey(AiPlatformEnum.ALIBABA));

        // 调用，并断言异常
        assertServiceException(() -> asrService.recognizeAsr(USER_ID, buildRecognizeReqVO()), ASR_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.BAIDU.getPlatform(), AiPlatformEnum.ALIBABA.getPlatform());
        verify(modelService, never()).getTranscriptionModel(any(), any());
        verifyNoInteractions(fileApi, asrMapper);
    }

    @Test
    public void testRecognizeAsr_notSupported() {
        // mock 数据：平台不支持语音识别
        when(modelService.validateModel(eq(MODEL_ID))).thenReturn(buildModel(AiPlatformEnum.MINIMAX, "speech-asr"));
        AiApiKeyDO apiKey = buildApiKey(AiPlatformEnum.MINIMAX);
        when(apiKeyService.validateApiKey(eq(KEY_ID))).thenReturn(apiKey);
        when(modelService.getTranscriptionModel(same(apiKey), eq("speech-asr")))
                .thenThrow(new IllegalArgumentException("平台(MiniMax)不支持语音识别"));

        // 调用，并断言异常
        assertServiceException(() -> asrService.recognizeAsr(USER_ID, buildRecognizeReqVO()), ASR_MODEL_NOT_SUPPORTED,
                "speech-asr", "平台(MiniMax)不支持语音识别");
        verifyNoInteractions(fileApi, asrMapper);
    }

    // ========== getAsrMyPage、getAsrMy ==========

    @Test
    public void testGetAsrMyPage() {
        // mock 方法
        PageParam pageParam = new PageParam();
        PageResult<AiAsrDO> pageResult = new PageResult<>();
        when(asrMapper.selectPageByUserId(USER_ID, pageParam)).thenReturn(pageResult);

        // 调用，并断言
        assertSame(pageResult, asrService.getAsrMyPage(USER_ID, pageParam));
    }

    @Test
    public void testGetAsrMy_success() {
        // mock 数据
        AiAsrDO asr = mockAsr(USER_ID, AiAsrStatusEnum.SUCCESS);

        // 调用，并断言
        assertSame(asr, asrService.getAsrMy(ASR_ID, USER_ID));
    }

    @Test
    public void testGetAsrMy_otherUser() {
        // mock 数据：别人的记录
        mockAsr(OTHER_USER_ID, AiAsrStatusEnum.SUCCESS);

        // 调用，并断言异常
        assertServiceException(() -> asrService.getAsrMy(ASR_ID, USER_ID), ASR_NOT_EXISTS);
    }

    @Test
    public void testGetAsrMy_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> asrService.getAsrMy(ASR_ID, USER_ID), ASR_NOT_EXISTS);
    }

    // ========== deleteAsrMy ==========

    @Test
    public void testDeleteAsrMy_success() {
        // mock 数据
        mockAsr(USER_ID, AiAsrStatusEnum.FAIL);

        // 调用
        asrService.deleteAsrMy(ASR_ID, USER_ID);
        // 断言
        verify(asrMapper).deleteById(ASR_ID);
    }

    @Test
    public void testDeleteAsrMy_otherUser() {
        // mock 数据：别人的记录
        mockAsr(OTHER_USER_ID, AiAsrStatusEnum.SUCCESS);

        // 调用，并断言异常
        assertServiceException(() -> asrService.deleteAsrMy(ASR_ID, USER_ID), ASR_NOT_EXISTS);
        verify(asrMapper, never()).deleteById(ASR_ID);
    }

    // ========== 语音识别管理 ==========

    @Test
    public void testGetAsrPage() {
        // mock 方法
        AiAsrPageReqVO pageReqVO = new AiAsrPageReqVO();
        PageResult<AiAsrDO> pageResult = new PageResult<>();
        when(asrMapper.selectPage(pageReqVO)).thenReturn(pageResult);

        // 调用，并断言
        assertSame(pageResult, asrService.getAsrPage(pageReqVO));
    }

    @Test
    public void testGetAsr() {
        // mock 数据：别人的记录，管理端也能查看
        AiAsrDO asr = mockAsr(OTHER_USER_ID, AiAsrStatusEnum.SUCCESS);

        // 调用，并断言
        assertSame(asr, asrService.getAsr(ASR_ID));
    }

    @Test
    public void testDeleteAsr_success() {
        // mock 数据：别人的记录，管理端也能删除
        mockAsr(OTHER_USER_ID, AiAsrStatusEnum.SUCCESS);

        // 调用
        asrService.deleteAsr(ASR_ID);
        // 断言
        verify(asrMapper).deleteById(ASR_ID);
    }

    @Test
    public void testDeleteAsr_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> asrService.deleteAsr(ASR_ID), ASR_NOT_EXISTS);
        verify(asrMapper, never()).deleteById(ASR_ID);
    }

    // ========== 工具方法 ==========

    /**
     * 调用识别：文件服务返回预签名地址，按预签名地址下载出音频
     */
    private AiAsrDO recognizeAsr() {
        when(fileApi.presignGetUrlQuietly(FILE_URL)).thenReturn(ACCESS_URL);
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(ACCESS_URL)).thenReturn(AUDIO);
            return asrService.recognizeAsr(USER_ID, buildRecognizeReqVO());
        }
    }

    private static AiAsrRecognizeReqVO buildRecognizeReqVO() {
        AiAsrRecognizeReqVO reqVO = new AiAsrRecognizeReqVO();
        reqVO.setModelId(MODEL_ID);
        reqVO.setFileUrl(FILE_URL);
        return reqVO;
    }

    private TranscriptionModel mockTranscriptionModel(AiPlatformEnum platform, String model) {
        when(modelService.validateModel(eq(MODEL_ID))).thenReturn(buildModel(platform, model));
        AiApiKeyDO apiKey = buildApiKey(platform);
        when(apiKeyService.validateApiKey(eq(KEY_ID))).thenReturn(apiKey);
        TranscriptionModel transcriptionModel = mock(TranscriptionModel.class);
        when(modelService.getTranscriptionModel(same(apiKey), eq(model))).thenReturn(transcriptionModel);
        return transcriptionModel;
    }

    private AiAsrDO mockAsr(Long userId, AiAsrStatusEnum status) {
        AiAsrDO asr = new AiAsrDO().setId(ASR_ID).setUserId(userId).setStatus(status.getStatus())
                .setOriginalText("原文");
        when(asrMapper.selectById(ASR_ID)).thenReturn(asr);
        return asr;
    }

    private AiAsrDO captureInsert() {
        ArgumentCaptor<AiAsrDO> captor = ArgumentCaptor.forClass(AiAsrDO.class);
        verify(asrMapper).insert(captor.capture());
        return captor.getValue();
    }

    private static AiModelDO buildModel(AiPlatformEnum platform, String model) {
        return new AiModelDO().setId(MODEL_ID).setName(model).setPlatform(platform.getPlatform()).setModel(model)
                .setKeyId(KEY_ID).setType(AiModelTypeEnum.ASR.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private static AiApiKeyDO buildApiKey(AiPlatformEnum platform) {
        return new AiApiKeyDO().setId(KEY_ID).setPlatform(platform.getPlatform()).setApiKey("${TEST_API_KEY}")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
