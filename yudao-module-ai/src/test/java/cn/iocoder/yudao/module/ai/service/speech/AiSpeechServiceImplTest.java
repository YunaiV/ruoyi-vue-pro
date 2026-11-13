package cn.iocoder.yudao.module.ai.service.speech;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.speech.AiSpeechDO;
import cn.iocoder.yudao.module.ai.dal.mysql.speech.AiSpeechMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.speech.AiSpeechStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baidu.BaiduSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax.MiniMaxSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.TencentSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiSpeechModel;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiSpeechServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(AiSpeechServiceImpl.class)
public class AiSpeechServiceImplTest extends BaseDbUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long MODEL_ID = 10L;
    private static final Long KEY_ID = 20L;

    private static final byte[] MP3 = buildMp3();

    @Resource
    private AiSpeechServiceImpl speechService;

    @Resource
    private AiSpeechMapper speechMapper;

    @MockitoBean
    private AiModelService modelService;
    @MockitoBean
    private AiApiKeyService apiKeyService;
    @MockitoBean
    private FileApi fileApi;

    @AfterEach
    public void after() {
        TenantContextHolder.clear();
    }

    // ========== 生成：同步合成 ==========

    @Test
    public void testGenerateSpeech_sync_success() {
        // mock 方法（模型、密钥）
        AiSpeechModel speechModel = mockSpeechModel(AiPlatformEnum.MINIMAX, "speech-2.8-hd");
        when(speechModel.synthesize(any(), any(), any(), any(), any())).thenReturn(MP3);
        // mock 方法（文件）
        when(fileApi.createFile(eq(MP3), startsWith("speech-"))).thenReturn("https://file/speech.mp3");
        // 准备参数
        AiSpeechGenerateReqVO reqVO = buildGenerateReqVO("你好", "male-qn-qingse", "mp3", 1.2);

        // 调用
        Long id = speechService.generateSpeech(USER_ID, reqVO);
        // 断言：记录
        AiSpeechDO speech = speechMapper.selectById(id);
        assertPojoEquals(reqVO, speech);
        assertEquals(USER_ID, speech.getUserId());
        assertEquals(AiPlatformEnum.MINIMAX.getPlatform(), speech.getPlatform());
        assertEquals("speech-2.8-hd", speech.getModel());
        assertEquals(KEY_ID, speech.getApiKeyId());
        assertEquals(AiSpeechStatusEnum.SUCCESS.getStatus(), speech.getStatus());
        assertNull(speech.getTaskId());
        assertEquals("https://file/speech.mp3", speech.getAudioUrl());
        assertEquals(MP3.length, speech.getAudioSize());
        assertNull(speech.getErrorMessage());
        assertNotNull(speech.getFinishTime());
        // 断言：先由模型校验参数，只调用一次服务商，文件名带扩展名
        verify(speechModel).validateOptions(eq("你好"), eq("male-qn-qingse"), eq("mp3"), eq(1.2));
        verify(speechModel, times(1)).synthesize(eq("你好"), eq("male-qn-qingse"), eq("mp3"), eq(1.2), any());
        verify(fileApi).createFile(eq(MP3), eq("speech-" + id + ".mp3"));
    }

    @Test
    public void testGenerateSpeech_sync_fail() {
        // mock 方法：服务商失败，错误信息中带 Bearer 凭证
        AiSpeechModel speechModel = mockSpeechModel(AiPlatformEnum.BYTEDANCE, "seed-tts-2.0");
        when(speechModel.synthesize(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("豆包语音合成失败：Authorization Bearer abc-secret"));

        // 调用
        Long id = speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "zh_female_vv_uranus_bigtts", "mp3", null));
        // 断言：已失败，错误已脱敏；没有转存；语速为空（即 1 倍）
        AiSpeechDO speech = speechMapper.selectById(id);
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), speech.getStatus());
        assertTrue(speech.getErrorMessage().startsWith("豆包语音合成失败"));
        assertFalse(speech.getErrorMessage().contains("abc-secret"));
        assertEquals("mp3", speech.getFormat());
        assertNull(speech.getSpeed());
        assertNotNull(speech.getFinishTime());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGenerateSpeech_sync_invalidAudio() {
        // mock 方法：服务商返回的不是 mp3
        AiSpeechModel speechModel = mockSpeechModel(AiPlatformEnum.BAIDU, "baidu-tts");
        when(speechModel.synthesize(any(), any(), any(), any(), any()))
                .thenReturn("{\"err\":1}".getBytes(StandardCharsets.UTF_8));

        // 调用
        Long id = speechService.generateSpeech(USER_ID, buildGenerateReqVO("你好", "5003", "mp3", null));
        // 断言：校验音频后才转存
        AiSpeechDO speech = speechMapper.selectById(id);
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), speech.getStatus());
        assertTrue(speech.getErrorMessage().contains("不是 mp3 格式"));
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGenerateSpeech_invalidOptions() {
        // 通义：超过 600 字、不支持 mp3、不支持调节语速。使用真实的模型校验，不发请求
        AiSpeechModel tongYi = mockSpeechModel(AiPlatformEnum.ALIBABA, "qwen3-tts-flash",
                spy(new TongYiSpeechModel(null, "test-key", "qwen3-tts-flash")));
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO(StrUtil.repeat("字", 601), "Cherry", "wav", null)), "朗读文本长度(601)超过上限(600)");
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "Cherry", "mp3", null)), "不支持的音频格式(mp3)");
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "Cherry", "wav", 1.2)), "该模型不支持调节语速，只能为 1");
        // MiniMax：语速超过两位小数、超出范围
        AiSpeechModel miniMax = mockSpeechModel(AiPlatformEnum.MINIMAX, "speech-2.8-hd",
                spy(new MiniMaxSpeechModel(null, "test-key", "speech-2.8-hd")));
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "v", "mp3", 1.234)), "语速(1.234)最多保留 2 位小数");
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "v", "mp3", 2.5)), "语速(2.5)超出范围 [0.5, 2]");
        // 百度：按 GBK 字节计数，不能编码的字符直接拒绝
        AiSpeechModel baidu = mockSpeechModel(AiPlatformEnum.BAIDU, "baidu-tts",
                spy(new BaiduSpeechModel(null, "test-key", "baidu-tts")));
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO(StrUtil.repeat("字", 512), "5003", "mp3", null)), "朗读文本按 GBK 编码为 1024 字节");
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好😀", "5003", "mp3", null)), "GBK 无法表示的字符");
        // 腾讯云：音色必须是整数
        AiSpeechModel tencent = mockSpeechModel(AiPlatformEnum.TENCENT, "tencent-tts",
                spy(new TencentSpeechModel(new TencentTtsApi("test-id|test-key", null))));
        assertOptionInvalid(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "zhiyu", "mp3", null)), "必须是正整数 VoiceType");

        // 断言：都在付费调用前拒绝，没有调用服务商，也没有插入记录
        for (AiSpeechModel speechModel : Arrays.asList(tongYi, miniMax, baidu, tencent)) {
            verify(speechModel, never()).synthesize(any(), any(), any(), any(), any());
        }
        assertEquals(0, speechMapper.selectCount());
    }

    @Test
    public void testGenerateSpeech_modelInvalid() {
        // 类型不是语音
        AiModelDO model = buildModel(MODEL_ID, AiPlatformEnum.MINIMAX, "speech-2.8-hd", KEY_ID)
                .setType(AiModelTypeEnum.CHAT.getType());
        when(modelService.validateModel(eq(MODEL_ID))).thenReturn(model);
        assertServiceException(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "v", "mp3", null)), MODEL_USE_TYPE_ERROR);
        // 平台不支持语音合成：创建模型时报错
        AiApiKeyDO openAiKey = mockModelAndApiKey(AiPlatformEnum.OPENAI, "tts-1");
        when(modelService.getTextToSpeechModel(same(openAiKey), eq("tts-1")))
                .thenThrow(new IllegalArgumentException("平台(OPENAI)不支持语音合成"));
        assertServiceException(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "v", "mp3", null)), SPEECH_MODEL_NOT_SUPPORTED, "tts-1", "平台(OPENAI)不支持语音合成");
        // 模型与密钥的平台不一致
        when(modelService.validateModel(eq(MODEL_ID)))
                .thenReturn(buildModel(MODEL_ID, AiPlatformEnum.BYTEDANCE, "seed-tts-2.0", KEY_ID));
        when(apiKeyService.validateApiKey(eq(KEY_ID))).thenReturn(buildApiKey(KEY_ID, AiPlatformEnum.ALIBABA));
        assertServiceException(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "v", "mp3", null)), SPEECH_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.BYTEDANCE.getPlatform(), AiPlatformEnum.ALIBABA.getPlatform());
        // 凭证格式不正确：创建客户端时报错，不插入记录
        AiApiKeyDO apiKey = mockModelAndApiKey(AiPlatformEnum.TENCENT, "tencent-tts");
        when(modelService.getTextToSpeechModel(same(apiKey), eq("tencent-tts")))
                .thenThrow(new IllegalArgumentException("腾讯云语音合成的密钥格式不正确，应为 SecretId|SecretKey"));
        assertServiceException(() -> speechService.generateSpeech(USER_ID,
                buildGenerateReqVO("你好", "101001", "mp3", null)), SPEECH_MODEL_NOT_SUPPORTED,
                "tencent-tts", "腾讯云语音合成的密钥格式不正确，应为 SecretId|SecretKey");

        // 断言：没有插入记录
        assertEquals(0, speechMapper.selectCount());
    }

    // ========== 生成：异步任务（腾讯云） ==========

    @Test
    public void testGenerateSpeech_task_success() {
        // mock 方法：提交后交出任务编号，再等待到结果；等待前任务编号已经保存
        AtomicReference<String> savedTaskId = new AtomicReference<>();
        AiSpeechModel speechModel = mockTaskSpeechModel(invocation -> {
            savedTaskId.set(speechMapper.selectList().get(0).getTaskId());
            return MP3;
        });
        when(fileApi.createFile(eq(MP3), startsWith("speech-"))).thenReturn("https://file/t.mp3");

        // 调用
        Long id = speechService.generateSpeech(USER_ID, buildGenerateReqVO("长文本", "101001", "mp3", 1.2));
        // 断言：与同步合成一样直接完成；只提交一次
        AiSpeechDO speech = speechMapper.selectById(id);
        assertEquals("task-1", savedTaskId.get());
        assertEquals(AiSpeechStatusEnum.SUCCESS.getStatus(), speech.getStatus());
        assertEquals("task-1", speech.getTaskId());
        assertEquals(KEY_ID, speech.getApiKeyId());
        assertEquals("https://file/t.mp3", speech.getAudioUrl());
        verify(speechModel, times(1)).synthesize(eq("长文本"), eq("101001"), eq("mp3"), eq(1.2), any());
        verify(modelService, never()).getTencentTtsApi(any());
    }

    @Test
    public void testGenerateSpeech_task_fail() {
        // mock 方法：任务失败
        mockTaskSpeechModel(invocation -> {
            throw new IllegalStateException("VoiceType 不存在");
        });

        // 调用
        Long id = speechService.generateSpeech(USER_ID, buildGenerateReqVO("长文本", "101001", "mp3", null));
        // 断言：已失败，保留任务编号
        AiSpeechDO speech = speechMapper.selectById(id);
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), speech.getStatus());
        assertEquals("VoiceType 不存在", speech.getErrorMessage());
        assertEquals("task-1", speech.getTaskId());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGenerateSpeech_task_waitTimeout() {
        // mock 方法：时限内仍在处理中
        AiSpeechModel speechModel = mockTaskSpeechModel(invocation -> null);

        // 调用
        Long id = speechService.generateSpeech(USER_ID, buildGenerateReqVO("长文本", "101001", "mp3", null));
        // 断言：保持生成中、保留任务编号，之后按任务编号继续查询；不重新提交
        AiSpeechDO speech = speechMapper.selectById(id);
        assertEquals(AiSpeechStatusEnum.IN_PROGRESS.getStatus(), speech.getStatus());
        assertEquals("task-1", speech.getTaskId());
        verify(speechModel, times(1)).synthesize(any(), any(), any(), any(), any());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGenerateSpeech_task_submitFail() {
        // mock 方法：提交时网络异常，没有拿到任务编号
        AiSpeechModel speechModel = mockSpeechModel(AiPlatformEnum.TENCENT, "tencent-tts");
        when(speechModel.synthesize(any(), any(), any(), any(), any())).thenThrow(new IllegalStateException(
                "提交腾讯云语音合成任务失败：网络异常（SocketTimeoutException），任务是否已创建未知，请勿自动重新提交"));

        // 调用
        Long id = speechService.generateSpeech(USER_ID, buildGenerateReqVO("长文本", "101001", "mp3", null));
        // 断言：已失败，没有任务编号，不重新提交
        AiSpeechDO speech = speechMapper.selectById(id);
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), speech.getStatus());
        assertNull(speech.getTaskId());
        assertTrue(speech.getErrorMessage().contains("请勿自动重新提交"));
        verify(speechModel, times(1)).synthesize(any(), any(), any(), any(), any());
    }

    @Test
    public void testGetSpeechMy_task_success() {
        // mock 数据：已提交、已过等待时限的任务，提交后模型的密钥已经换成别的
        AiSpeechDO speech = insertTaskSpeech(USER_ID, LocalDateTime.now().minusMinutes(5));
        // mock 方法：按提交时绑定的密钥查询；密钥只停用也照常查询
        AiApiKeyDO apiKey = buildApiKey(KEY_ID, AiPlatformEnum.TENCENT).setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(apiKeyService.getApiKey(eq(KEY_ID))).thenReturn(apiKey);
        TencentTtsApi tencentTtsApi = mock(TencentTtsApi.class);
        when(modelService.getTencentTtsApi(same(apiKey))).thenReturn(tencentTtsApi);
        when(tencentTtsApi.getTask(eq("task-1"), eq("mp3"))).thenReturn(new TencentTtsApi.TtsTaskResult(
                "task-1", TencentTtsApi.TtsTaskStatus.SUCCESS, MP3, null, "req-2"));
        when(fileApi.createFile(eq(MP3), eq("speech-" + speech.getId() + ".mp3"))).thenReturn("https://file/t.mp3");

        // 调用
        AiSpeechDO result = speechService.getSpeechMy(speech.getId(), USER_ID);
        // 断言：已完成；只查询，不重新提交
        assertEquals(AiSpeechStatusEnum.SUCCESS.getStatus(), result.getStatus());
        assertEquals("https://file/t.mp3", result.getAudioUrl());
        assertEquals(MP3.length, result.getAudioSize());
        verify(tencentTtsApi, times(1)).getTask(any(), any());
        verify(tencentTtsApi, never()).createTask(any(), any(), any(), any());
        verify(modelService, never()).validateModel(any());
    }

    @Test
    public void testGetSpeechMy_task_waiting() {
        // mock 数据：刚提交，提交的线程还在等待结果
        AiSpeechDO speech = insertTaskSpeech(USER_ID, LocalDateTime.now());

        // 调用
        AiSpeechDO result = speechService.getSpeechMy(speech.getId(), USER_ID);
        // 断言：不重复查询
        assertEquals(AiSpeechStatusEnum.IN_PROGRESS.getStatus(), result.getStatus());
        verifyNoInteractions(apiKeyService);
        verify(modelService, never()).getTencentTtsApi(any());
    }

    @Test
    public void testSyncSpeech_task() {
        // mock 数据：运行中、失败、查询异常、密钥已删除、密钥平台已变更
        LocalDateTime createTime = LocalDateTime.now().minusMinutes(20);
        AiSpeechDO running = insertTaskSpeech(USER_ID, createTime, "task-1", KEY_ID);
        AiSpeechDO failed = insertTaskSpeech(USER_ID, createTime, "task-2", KEY_ID);
        AiSpeechDO error = insertTaskSpeech(USER_ID, createTime, "task-3", KEY_ID);
        AiSpeechDO deleted = insertTaskSpeech(USER_ID, createTime, "task-4", 30L);
        AiSpeechDO changed = insertTaskSpeech(USER_ID, createTime, "task-5", 40L);
        // mock 方法
        AiApiKeyDO apiKey = buildApiKey(KEY_ID, AiPlatformEnum.TENCENT);
        when(apiKeyService.getApiKey(eq(KEY_ID))).thenReturn(apiKey);
        when(apiKeyService.getApiKey(eq(40L))).thenReturn(buildApiKey(40L, AiPlatformEnum.ALIBABA));
        TencentTtsApi tencentTtsApi = mock(TencentTtsApi.class);
        when(modelService.getTencentTtsApi(same(apiKey))).thenReturn(tencentTtsApi);
        when(tencentTtsApi.getTask(eq("task-1"), any())).thenReturn(new TencentTtsApi.TtsTaskResult(
                "task-1", TencentTtsApi.TtsTaskStatus.RUNNING, null, null, "req"));
        when(tencentTtsApi.getTask(eq("task-2"), any())).thenReturn(new TencentTtsApi.TtsTaskResult(
                "task-2", TencentTtsApi.TtsTaskStatus.FAILED, null, "VoiceType 不存在", "req"));
        when(tencentTtsApi.getTask(eq("task-3"), any())).thenThrow(new IllegalStateException("查询失败"));

        // 调用
        int count = speechService.syncSpeech();
        // 断言：失败、密钥已删除、平台已变更的结束；运行中、查询异常的保持生成中，下轮重试
        assertEquals(3, count);
        assertEquals(AiSpeechStatusEnum.IN_PROGRESS.getStatus(), speechMapper.selectById(running.getId()).getStatus());
        assertEquals(AiSpeechStatusEnum.IN_PROGRESS.getStatus(), speechMapper.selectById(error.getId()).getStatus());
        AiSpeechDO failedResult = speechMapper.selectById(failed.getId());
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), failedResult.getStatus());
        assertEquals("VoiceType 不存在", failedResult.getErrorMessage());
        assertEquals("渠道密钥已删除，无法查询任务结果", speechMapper.selectById(deleted.getId()).getErrorMessage());
        assertEquals("渠道密钥的平台已变更，无法查询任务结果", speechMapper.selectById(changed.getId()).getErrorMessage());
        verify(tencentTtsApi, never()).createTask(any(), any(), any(), any());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testSyncSpeech_timeout() {
        // mock 数据：同步合成超过 10 分钟；异步任务 2 小时（未超时）、25 小时（超时）
        AiSpeechDO sync = insertSpeech(USER_ID, AiSpeechStatusEnum.IN_PROGRESS, LocalDateTime.now().minusMinutes(11));
        AiSpeechDO syncFresh = insertSpeech(USER_ID, AiSpeechStatusEnum.IN_PROGRESS, LocalDateTime.now().minusMinutes(5));
        AiSpeechDO task = insertTaskSpeech(USER_ID, LocalDateTime.now().minusHours(2));
        AiSpeechDO taskOld = insertTaskSpeech(USER_ID, LocalDateTime.now().minusHours(25));

        // 调用
        int count = speechService.timeoutSpeech();
        // 断言
        assertEquals(2, count);
        assertEquals("生成超时：超过 10 分钟未完成", speechMapper.selectById(sync.getId()).getErrorMessage());
        assertEquals(AiSpeechStatusEnum.IN_PROGRESS.getStatus(), speechMapper.selectById(syncFresh.getId()).getStatus());
        assertEquals(AiSpeechStatusEnum.IN_PROGRESS.getStatus(), speechMapper.selectById(task.getId()).getStatus());
        assertEquals("生成超时：超过 24 小时未完成", speechMapper.selectById(taskOld.getId()).getErrorMessage());
    }

    @Test
    public void testExecuteSpeech_lateResultNotOverwrite() {
        // mock 数据：已经超时置为已失败
        AiSpeechDO speech = insertSpeech(USER_ID, AiSpeechStatusEnum.FAIL, LocalDateTime.now());
        AiSpeechModel speechModel = mock(AiSpeechModel.class);
        when(speechModel.synthesize(any(), any(), any(), any(), any())).thenReturn(MP3);
        when(fileApi.createFile(any(byte[].class), anyString())).thenReturn("https://file/late.mp3");

        // 调用：迟到的结果
        speechService.executeSpeech(null, speech, speechModel);
        // 断言：终态不被覆盖
        AiSpeechDO result = speechMapper.selectById(speech.getId());
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), result.getStatus());
        assertNull(result.getAudioUrl());
    }

    @Test
    public void testExecuteSpeech_bindTenant() {
        // mock 数据
        AiSpeechDO speech = insertSpeech(USER_ID, AiSpeechStatusEnum.IN_PROGRESS, LocalDateTime.now());
        AtomicReference<Long> tenantId = new AtomicReference<>();
        AiSpeechModel speechModel = mock(AiSpeechModel.class);
        when(speechModel.synthesize(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            tenantId.set(TenantContextHolder.getTenantId());
            return MP3;
        });
        when(fileApi.createFile(any(byte[].class), anyString())).thenReturn("https://file/a.mp3");
        TenantContextHolder.setTenantId(100L);

        // 调用：后台执行时使用提交时的租户，不依赖当前线程的上下文
        speechService.executeSpeech(200L, speech, speechModel);
        // 断言
        assertEquals(200L, tenantId.get());
        assertEquals(100L, TenantContextHolder.getTenantId());
    }

    // ========== 我的语音 ==========

    @Test
    public void testGetSpeechMyPage() {
        // mock 数据
        AiSpeechDO mine1 = insertSpeech(USER_ID, AiSpeechStatusEnum.SUCCESS, LocalDateTime.now());
        insertSpeech(OTHER_USER_ID, AiSpeechStatusEnum.SUCCESS, LocalDateTime.now());
        AiSpeechDO mine2 = insertSpeech(USER_ID, AiSpeechStatusEnum.FAIL, LocalDateTime.now());

        // 调用
        PageResult<AiSpeechDO> pageResult = speechService.getSpeechMyPage(USER_ID, new PageParam());
        // 断言：只有本人的，按编号倒序
        assertEquals(2, pageResult.getTotal());
        assertEquals(Arrays.asList(mine2.getId(), mine1.getId()), convertList(pageResult.getList(), AiSpeechDO::getId));
    }

    @Test
    public void testGetSpeechMy_notOwner() {
        // mock 数据
        AiSpeechDO speech = insertSpeech(OTHER_USER_ID, AiSpeechStatusEnum.SUCCESS, LocalDateTime.now());

        // 调用，并断言：不是本人的，按不存在处理
        assertServiceException(() -> speechService.getSpeechMy(speech.getId(), USER_ID), SPEECH_NOT_EXISTS);
        assertServiceException(() -> speechService.getSpeechMy(-1L, USER_ID), SPEECH_NOT_EXISTS);
    }

    @Test
    public void testGetSpeechMy_syncTimeout() {
        // mock 数据：同步合成被中断，超过 10 分钟仍为生成中
        AiSpeechDO speech = insertSpeech(USER_ID, AiSpeechStatusEnum.IN_PROGRESS, LocalDateTime.now().minusMinutes(11));

        // 调用
        AiSpeechDO result = speechService.getSpeechMy(speech.getId(), USER_ID);
        // 断言：读取时也会置为已失败
        assertEquals(AiSpeechStatusEnum.FAIL.getStatus(), result.getStatus());
        verifyNoInteractions(apiKeyService);
    }

    @Test
    public void testDeleteSpeechMy() {
        // mock 数据
        AiSpeechDO success = insertSpeech(USER_ID, AiSpeechStatusEnum.SUCCESS, LocalDateTime.now());
        AiSpeechDO running = insertSpeech(USER_ID, AiSpeechStatusEnum.IN_PROGRESS, LocalDateTime.now());
        AiSpeechDO other = insertSpeech(OTHER_USER_ID, AiSpeechStatusEnum.SUCCESS, LocalDateTime.now());

        // 调用，并断言：生成中的不能删除；别人的按不存在处理
        assertServiceException(() -> speechService.deleteSpeechMy(running.getId(), USER_ID), SPEECH_STATUS_NOT_FINISHED);
        assertServiceException(() -> speechService.deleteSpeechMy(other.getId(), USER_ID), SPEECH_NOT_EXISTS);
        speechService.deleteSpeechMy(success.getId(), USER_ID);
        // 断言
        assertNull(speechMapper.selectById(success.getId()));
        assertNotNull(speechMapper.selectById(running.getId()));
        assertNotNull(speechMapper.selectById(other.getId()));
    }

    // ========== 语音管理 ==========

    @Test
    public void testGetSpeechPage() {
        // mock 数据
        AiSpeechDO dbSpeech = randomSpeech(o -> o.setUserId(USER_ID).setPlatform(AiPlatformEnum.MINIMAX.getPlatform())
                .setStatus(AiSpeechStatusEnum.SUCCESS.getStatus()).setInputText("欢迎使用芋道语音")
                .setCreateTime(buildTime(2026, 11, 12)));
        speechMapper.insert(dbSpeech);
        // 测试 userId 不匹配
        speechMapper.insert(cloneIgnoreId(dbSpeech, o -> o.setUserId(OTHER_USER_ID)));
        // 测试 platform 不匹配
        speechMapper.insert(cloneIgnoreId(dbSpeech, o -> o.setPlatform(AiPlatformEnum.TENCENT.getPlatform())));
        // 测试 status 不匹配
        speechMapper.insert(cloneIgnoreId(dbSpeech, o -> o.setStatus(AiSpeechStatusEnum.FAIL.getStatus())));
        // 测试 inputText 不匹配
        speechMapper.insert(cloneIgnoreId(dbSpeech, o -> o.setInputText("你好，世界")));
        // 测试 createTime 不匹配
        speechMapper.insert(cloneIgnoreId(dbSpeech, o -> o.setCreateTime(buildTime(2026, 10, 1))));
        // 准备参数
        AiSpeechPageReqVO reqVO = new AiSpeechPageReqVO();
        reqVO.setUserId(USER_ID);
        reqVO.setPlatform(AiPlatformEnum.MINIMAX.getPlatform());
        reqVO.setStatus(AiSpeechStatusEnum.SUCCESS.getStatus());
        reqVO.setInputText("芋道");
        reqVO.setCreateTime(buildBetweenTime(2026, 11, 1, 2026, 11, 30));

        // 调用
        PageResult<AiSpeechDO> pageResult = speechService.getSpeechPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSpeech, pageResult.getList().get(0));
    }

    @Test
    public void testGetSpeech() {
        // mock 数据：别人的语音，管理端也能查看
        AiSpeechDO dbSpeech = insertSpeech(OTHER_USER_ID, AiSpeechStatusEnum.SUCCESS, LocalDateTime.now());

        // 调用，并断言
        assertPojoEquals(dbSpeech, speechService.getSpeech(dbSpeech.getId()));
        assertNull(speechService.getSpeech(-1L));
    }

    @Test
    public void testDeleteSpeech_success() {
        // mock 数据：别人的语音，管理端也能删除
        AiSpeechDO dbSpeech = insertSpeech(OTHER_USER_ID, AiSpeechStatusEnum.FAIL, LocalDateTime.now());

        // 调用
        speechService.deleteSpeech(dbSpeech.getId());
        // 断言
        assertNull(speechMapper.selectById(dbSpeech.getId()));
    }

    @Test
    public void testDeleteSpeech_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> speechService.deleteSpeech(-1L), SPEECH_NOT_EXISTS);
    }

    @Test
    public void testDeleteSpeech_inProgress() {
        // mock 数据
        AiSpeechDO dbSpeech = insertSpeech(OTHER_USER_ID, AiSpeechStatusEnum.IN_PROGRESS, LocalDateTime.now());

        // 调用，并断言异常
        assertServiceException(() -> speechService.deleteSpeech(dbSpeech.getId()), SPEECH_STATUS_NOT_FINISHED);
        assertNotNull(speechMapper.selectById(dbSpeech.getId()));
    }

    @Test
    public void testBuildErrorMessage() {
        assertEquals("语音生成失败，请稍后重试", AiSpeechServiceImpl.buildErrorMessage(new NullPointerException("x")));
        assertEquals("语音生成失败，请稍后重试", AiSpeechServiceImpl.buildErrorMessage(new IllegalStateException(" ")));
        assertEquals("失败 Bearer ***", AiSpeechServiceImpl.buildErrorMessage(new IllegalStateException("失败 Bearer abc")));
    }

    // ========== 私有方法 ==========

    private AiApiKeyDO mockModelAndApiKey(AiPlatformEnum platform, String model) {
        when(modelService.validateModel(eq(MODEL_ID))).thenReturn(buildModel(MODEL_ID, platform, model, KEY_ID));
        AiApiKeyDO apiKey = buildApiKey(KEY_ID, platform);
        when(apiKeyService.validateApiKey(eq(KEY_ID))).thenReturn(apiKey);
        return apiKey;
    }

    private AiSpeechModel mockSpeechModel(AiPlatformEnum platform, String model) {
        return mockSpeechModel(platform, model, mock(AiSpeechModel.class));
    }

    private AiSpeechModel mockSpeechModel(AiPlatformEnum platform, String model, AiSpeechModel speechModel) {
        AiApiKeyDO apiKey = mockModelAndApiKey(platform, model);
        when(modelService.getTextToSpeechModel(same(apiKey), eq(model))).thenReturn(speechModel);
        return speechModel;
    }

    /**
     * mock 腾讯云模型：提交后交出任务编号 task-1，再由 waitResult 给出等待的结果
     */
    @SuppressWarnings("unchecked")
    private AiSpeechModel mockTaskSpeechModel(Answer<byte[]> waitResult) {
        AiSpeechModel speechModel = mockSpeechModel(AiPlatformEnum.TENCENT, "tencent-tts");
        when(speechModel.synthesize(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            invocation.getArgument(4, Consumer.class).accept("task-1");
            return waitResult.answer(invocation);
        });
        return speechModel;
    }

    /**
     * 断言参数校验失败：错误码为 {@link cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants#SPEECH_OPTION_INVALID}，提示包含指定内容
     */
    private static void assertOptionInvalid(Executable executable, String message) {
        ServiceException ex = assertThrows(ServiceException.class, executable);
        assertEquals(SPEECH_OPTION_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains(message), ex.getMessage());
    }

    private static AiModelDO buildModel(Long id, AiPlatformEnum platform, String model, Long keyId) {
        return new AiModelDO().setId(id).setName(model).setPlatform(platform.getPlatform()).setModel(model)
                .setKeyId(keyId).setType(AiModelTypeEnum.VOICE.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private static AiApiKeyDO buildApiKey(Long id, AiPlatformEnum platform) {
        return new AiApiKeyDO().setId(id).setPlatform(platform.getPlatform()).setApiKey("${TEST_API_KEY}")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private static AiSpeechGenerateReqVO buildGenerateReqVO(String text, String voice, String format, Double speed) {
        AiSpeechGenerateReqVO reqVO = new AiSpeechGenerateReqVO();
        reqVO.setModelId(MODEL_ID);
        reqVO.setInputText(text);
        reqVO.setVoice(voice);
        reqVO.setFormat(format);
        reqVO.setSpeed(speed);
        return reqVO;
    }

    private AiSpeechDO insertSpeech(Long userId, AiSpeechStatusEnum status, LocalDateTime createTime) {
        AiSpeechDO speech = randomSpeech(o -> o.setUserId(userId).setPlatform(AiPlatformEnum.MINIMAX.getPlatform())
                .setStatus(status.getStatus()).setCreateTime(createTime));
        speechMapper.insert(speech);
        return speech;
    }

    private AiSpeechDO insertTaskSpeech(Long userId, LocalDateTime createTime) {
        return insertTaskSpeech(userId, createTime, "task-1", KEY_ID);
    }

    private AiSpeechDO insertTaskSpeech(Long userId, LocalDateTime createTime, String taskId, Long apiKeyId) {
        AiSpeechDO speech = randomSpeech(o -> o.setUserId(userId).setPlatform(AiPlatformEnum.TENCENT.getPlatform())
                .setApiKeyId(apiKeyId).setStatus(AiSpeechStatusEnum.IN_PROGRESS.getStatus()).setTaskId(taskId)
                .setCreateTime(createTime));
        speechMapper.insert(speech);
        return speech;
    }

    /**
     * 随机的语音记录：编号自增（分页按编号排序），格式、语速取合法值，没有任务编号与音频；其它字段由 consumer 覆盖
     */
    @SafeVarargs
    private static AiSpeechDO randomSpeech(Consumer<AiSpeechDO>... consumers) {
        Consumer<AiSpeechDO> consumer = o -> o.setId(null).setApiKeyId(KEY_ID).setFormat("mp3").setSpeed(1.0)
                .setTaskId(null).setAudioUrl(null);
        return randomPojo(AiSpeechDO.class, ArrayUtils.append(consumer, consumers));
    }

    private static byte[] buildMp3() {
        byte[] mp3 = new byte[16];
        mp3[0] = 'I';
        mp3[1] = 'D';
        mp3[2] = '3';
        return mp3;
    }

}
