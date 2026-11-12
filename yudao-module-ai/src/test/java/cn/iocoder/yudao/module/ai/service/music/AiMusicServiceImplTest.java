package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiMusicServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiMusicServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long MODEL_ID = 10L;
    private static final Long API_KEY_ID = 20L;
    private static final Long MUSIC_ID = 100L;

    /**
     * 真实的 1x1 PNG（ImageIO 编码）：签名 + IHDR + IDAT + IEND
     */
    private static final byte[] PNG_CONTENT = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4XmNgYGAAAAAEAAEP0q3kAAAAAElFTkSuQmCC");
    /**
     * 合法的 MP3：空的 ID3v2 标签 + 两个 MPEG-1 Layer III 128kbps 44.1kHz 帧（每帧 417 字节）
     */
    private static final byte[] MP3_CONTENT = buildMp3();
    /**
     * 合法的 WAV：PCM 单声道 8kHz 8 位，4 字节数据
     */
    private static final byte[] WAV_CONTENT = buildWav();

    @Spy
    @InjectMocks
    private AiMusicServiceImpl musicService;

    @Mock
    private AiMusicMapper musicMapper;
    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private FileApi fileApi;

    @Mock
    private SunoApi sunoApi;

    // ========== generateMusic ==========

    @Test
    public void testGenerateMusic_inspiration() {
        // mock 方法
        mockMusicModel();
        mockInsert();
        when(sunoApi.submitMusic(any())).thenReturn("task-1");
        // 准备参数：灵感模式下的歌词、风格、名称不生效
        AiMusicGenerateReqVO reqVO = new AiMusicGenerateReqVO().setGenerateMode(AiMusicGenerateModeEnum.INSPIRATION.getMode())
                .setModelId(MODEL_ID).setPrompt("夏日海边").setLyric("忽略").setTags("忽略").setMakeInstrumental(true);

        // 调用
        Long id = musicService.generateMusic(USER_ID, reqVO);
        // 断言：占位行
        assertEquals(MUSIC_ID, id);
        AiMusicDO music = captureInsert();
        assertEquals(USER_ID, music.getUserId());
        assertEquals(AiMusicStatusEnum.PENDING.getStatus(), music.getStatus());
        assertEquals(API_KEY_ID, music.getApiKeyId());
        assertEquals("chirp-v4", music.getModel());
        assertEquals("夏日海边", music.getPrompt());
        assertNull(music.getLyric());
        assertNull(music.getTags());
        assertFalse(music.getPublicStatus());
        // 断言：提交
        ArgumentCaptor<SunoApi.MusicRequest> requestCaptor = ArgumentCaptor.forClass(SunoApi.MusicRequest.class);
        verify(sunoApi).submitMusic(requestCaptor.capture());
        assertEquals("夏日海边", requestCaptor.getValue().gptDescriptionPrompt());
        assertNull(requestCaptor.getValue().prompt());
        assertEquals("chirp-v4", requestCaptor.getValue().mv());
        assertTrue(requestCaptor.getValue().makeInstrumental());
        // 断言：写入 task_id，进入生成中
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.PENDING.getStatus());
        assertEquals("task-1", update.getTaskId());
        assertEquals(AiMusicStatusEnum.IN_PROGRESS.getStatus(), update.getStatus());
    }

    @Test
    public void testGenerateMusic_custom() {
        // mock 方法
        mockMusicModel();
        mockInsert();
        when(sunoApi.submitMusic(any())).thenReturn("task-1");
        // 准备参数
        AiMusicGenerateReqVO reqVO = new AiMusicGenerateReqVO().setGenerateMode(AiMusicGenerateModeEnum.CUSTOM.getMode())
                .setModelId(MODEL_ID).setTitle("夏日").setLyric("[Verse]").setTags("pop").setMakeInstrumental(false);

        // 调用
        musicService.generateMusic(USER_ID, reqVO);
        // 断言
        ArgumentCaptor<SunoApi.MusicRequest> requestCaptor = ArgumentCaptor.forClass(SunoApi.MusicRequest.class);
        verify(sunoApi).submitMusic(requestCaptor.capture());
        assertNull(requestCaptor.getValue().gptDescriptionPrompt());
        assertEquals("[Verse]", requestCaptor.getValue().prompt());
        assertEquals("pop", requestCaptor.getValue().tags());
        assertEquals("夏日", requestCaptor.getValue().title());
        assertNull(requestCaptor.getValue().taskId());
    }

    @Test
    public void testGenerateMusic_modelTypeError() {
        // mock 方法：对话模型
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID)
                .setType(AiModelTypeEnum.CHAT.getType()).setPlatform(AiPlatformEnum.SUNO.getPlatform()));
        // 准备参数
        AiMusicGenerateReqVO reqVO = new AiMusicGenerateReqVO().setGenerateMode(AiMusicGenerateModeEnum.INSPIRATION.getMode())
                .setModelId(MODEL_ID).setPrompt("夏日海边");

        // 调用，并断言异常
        assertServiceException(() -> musicService.generateMusic(USER_ID, reqVO), MUSIC_MODEL_TYPE_ERROR);
        verify(musicMapper, never()).insert(any(AiMusicDO.class));
    }

    @Test
    public void testGenerateMusic_submitFail() {
        // mock 方法：中转返回业务失败
        mockMusicModel();
        mockInsert();
        when(sunoApi.submitMusic(any())).thenThrow(new IllegalStateException("余额不足"));
        // 准备参数
        AiMusicGenerateReqVO reqVO = new AiMusicGenerateReqVO().setGenerateMode(AiMusicGenerateModeEnum.INSPIRATION.getMode())
                .setModelId(MODEL_ID).setPrompt("夏日海边");

        // 调用：不抛出，返回编号
        assertEquals(MUSIC_ID, musicService.generateMusic(USER_ID, reqVO));
        // 断言：置为失败，原因原样保留
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.PENDING.getStatus());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("余额不足", update.getErrorMessage());
        assertNotNull(update.getFinishTime());
    }

    @Test
    public void testGenerateMusic_networkError() {
        // mock 方法：网络异常，异常信息里有中转地址
        mockMusicModel();
        mockInsert();
        when(sunoApi.submitMusic(any())).thenThrow(new RuntimeException("Connection refused: relay.example.com"));
        // 准备参数
        AiMusicGenerateReqVO reqVO = new AiMusicGenerateReqVO().setGenerateMode(AiMusicGenerateModeEnum.INSPIRATION.getMode())
                .setModelId(MODEL_ID).setPrompt("夏日海边");

        // 调用
        musicService.generateMusic(USER_ID, reqVO);
        // 断言：只给出概括，不暴露中转地址
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.PENDING.getStatus());
        assertEquals("中转服务请求失败，请稍后重试", update.getErrorMessage());
    }

    // ========== extendMusic、concatMusic ==========

    @Test
    public void testExtendMusic_success() {
        // mock 数据：来源沿用 api_key_id，与模型当前的密钥无关
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getSunoApi(apiKey)).thenReturn(sunoApi);
        mockInsert();
        when(sunoApi.submitMusic(any())).thenReturn("task-2");
        // 准备参数
        AiMusicExtendReqVO reqVO = new AiMusicExtendReqVO().setId(ref.getId()).setExtendSeconds(60)
                .setTitle("续写").setLyric("[Verse 2]");

        // 调用
        musicService.extendMusic(USER_ID, reqVO);
        // 断言：占位行
        AiMusicDO music = captureInsert();
        assertEquals(AiMusicGenerateModeEnum.EXTEND.getMode(), music.getGenerateMode());
        assertEquals(ref.getId(), music.getRefMusicId());
        assertEquals(API_KEY_ID, music.getApiKeyId());
        assertEquals(60, music.getExtendSeconds());
        assertEquals("pop", music.getTags()); // 未传时沿用来源
        // 断言：task_id、continue_clip_id 从来源读取
        ArgumentCaptor<SunoApi.MusicRequest> requestCaptor = ArgumentCaptor.forClass(SunoApi.MusicRequest.class);
        verify(sunoApi).submitMusic(requestCaptor.capture());
        assertEquals("task-ref", requestCaptor.getValue().taskId());
        assertEquals("clip-ref", requestCaptor.getValue().continueClipId());
        assertEquals(60, requestCaptor.getValue().continueAt());
        assertEquals("chirp-v4", requestCaptor.getValue().mv());
    }

    @Test
    public void testExtendMusic_notOwner() {
        // mock 数据
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode()).setUserId(OTHER_USER_ID).setPublicStatus(true);
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        // 准备参数
        AiMusicExtendReqVO reqVO = new AiMusicExtendReqVO().setId(ref.getId()).setExtendSeconds(60).setTitle("续写");

        // 调用，并断言异常：公开的他人作品也不能续写
        assertServiceException(() -> musicService.extendMusic(USER_ID, reqVO), MUSIC_REF_NOT_AVAILABLE);
    }

    @Test
    public void testExtendMusic_notSuccess() {
        // mock 数据
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode()).setStatus(AiMusicStatusEnum.SAVING.getStatus());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        // 准备参数
        AiMusicExtendReqVO reqVO = new AiMusicExtendReqVO().setId(ref.getId()).setExtendSeconds(60).setTitle("续写");

        // 调用，并断言异常
        assertServiceException(() -> musicService.extendMusic(USER_ID, reqVO), MUSIC_REF_NOT_AVAILABLE);
    }

    @Test
    public void testExtendMusic_exceedDuration() {
        // mock 数据
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        // 准备参数
        AiMusicExtendReqVO reqVO = new AiMusicExtendReqVO().setId(ref.getId()).setExtendSeconds(121).setTitle("续写");

        // 调用，并断言异常
        assertServiceException(() -> musicService.extendMusic(USER_ID, reqVO), MUSIC_EXTEND_SECONDS_EXCEED);
        verify(musicMapper, never()).insert(any(AiMusicDO.class));
    }

    @Test
    public void testExtendMusic_apiKeyDisabled() {
        // mock 数据
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(new AiApiKeyDO().setId(API_KEY_ID)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        // 准备参数
        AiMusicExtendReqVO reqVO = new AiMusicExtendReqVO().setId(ref.getId()).setExtendSeconds(60).setTitle("续写");

        // 调用，并断言异常
        assertServiceException(() -> musicService.extendMusic(USER_ID, reqVO), MUSIC_REF_API_KEY_UNAVAILABLE);
    }

    @Test
    public void testExtendMusic_apiKeyDeleted() {
        // mock 数据
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        // 准备参数
        AiMusicExtendReqVO reqVO = new AiMusicExtendReqVO().setId(ref.getId()).setExtendSeconds(60).setTitle("续写");

        // 调用，并断言异常
        assertServiceException(() -> musicService.extendMusic(USER_ID, reqVO), MUSIC_REF_API_KEY_UNAVAILABLE);
    }

    @Test
    public void testConcatMusic_success() {
        // mock 数据
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.EXTEND.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getSunoApi(apiKey)).thenReturn(sunoApi);
        mockInsert();
        when(sunoApi.submitConcat("clip-ref")).thenReturn("task-3");

        // 调用
        musicService.concatMusic(USER_ID, new AiMusicConcatReqVO().setId(ref.getId()));
        // 断言
        AiMusicDO music = captureInsert();
        assertEquals(AiMusicGenerateModeEnum.CONCAT.getMode(), music.getGenerateMode());
        assertEquals(ref.getId(), music.getRefMusicId());
        verify(sunoApi).submitConcat("clip-ref");
    }

    @Test
    public void testConcatMusic_notExtend() {
        // mock 数据：非续写作品
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.CUSTOM.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);

        // 调用，并断言异常
        assertServiceException(() -> musicService.concatMusic(USER_ID, new AiMusicConcatReqVO().setId(ref.getId())),
                MUSIC_REF_NOT_EXTEND);
    }

    @Test
    public void testConcatMusic_unsupported() {
        // mock 数据：中转未实现拼接扩展路径
        AiMusicDO ref = buildRefMusic(AiMusicGenerateModeEnum.EXTEND.getMode());
        when(musicMapper.selectById(ref.getId())).thenReturn(ref);
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getSunoApi(apiKey)).thenReturn(sunoApi);
        mockInsert();
        when(sunoApi.submitConcat("clip-ref")).thenThrow(
                new IllegalStateException("当前中转不支持该接口（HTTP 404）：/suno/submit/concat"));

        // 调用
        musicService.concatMusic(USER_ID, new AiMusicConcatReqVO().setId(ref.getId()));
        // 断言：失败原因保留中转返回的不支持路径
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.PENDING.getStatus());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("当前中转不支持该接口（HTTP 404）：/suno/submit/concat", update.getErrorMessage());
    }

    // ========== uploadMusic ==========

    @Test
    public void testUploadMusic_success() {
        // mock 方法：下载、交给中转的都是文件服务的预签名地址
        mockMusicModel();
        mockInsert();
        when(fileApi.presignGetUrlQuietly("https://files.example.com/demo_1731.mp3")).thenReturn("https://files.example.com/demo_1731.mp3?sign=1");
        when(sunoApi.uploadAudioUrl("https://files.example.com/demo_1731.mp3?sign=1")).thenReturn("task-4");
        // 准备参数：未传名称时使用文件名
        AiMusicUploadReqVO reqVO = new AiMusicUploadReqVO().setModelId(MODEL_ID).setFileUrl("https://files.example.com/demo_1731.mp3");

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://files.example.com/demo_1731.mp3?sign=1")).thenReturn(MP3_CONTENT);
            // 调用
            musicService.uploadMusic(USER_ID, reqVO);
        }
        // 断言：占位行记录文件地址（不带签名），不再重复存文件
        AiMusicDO music = captureInsert();
        assertEquals(AiMusicGenerateModeEnum.UPLOAD.getMode(), music.getGenerateMode());
        assertEquals("demo_1731", music.getTitle());
        assertEquals("https://files.example.com/demo_1731.mp3", music.getAudioUrl());
        verify(sunoApi).uploadAudioUrl("https://files.example.com/demo_1731.mp3?sign=1");
        verify(fileApi, never()).createFile(any(byte[].class));
    }

    @Test
    public void testUploadMusic_wav() {
        // mock 方法：本地等存储不支持预签名时，使用原地址
        mockMusicModel();
        mockInsert();
        when(fileApi.presignGetUrlQuietly("https://files.example.com/a.mp3")).thenReturn("https://files.example.com/a.mp3");
        when(sunoApi.uploadAudioUrl(any())).thenReturn("task-4");
        // 准备参数：文件名后缀与内容不符时，以内容为准
        AiMusicUploadReqVO reqVO = new AiMusicUploadReqVO().setModelId(MODEL_ID).setTitle("哼唱")
                .setFileUrl("https://files.example.com/a.mp3");

        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://files.example.com/a.mp3")).thenReturn(WAV_CONTENT);
            // 调用
            musicService.uploadMusic(USER_ID, reqVO);
        }
        // 断言
        assertEquals("哼唱", captureInsert().getTitle());
        verify(sunoApi).uploadAudioUrl("https://files.example.com/a.mp3");
    }

    @Test
    public void testUploadMusic_fileInvalid() {
        // 准备参数：普通 ftyp 容器、网页、图片，后缀伪装成 mp3，都不是支持的音频
        List<byte[]> contents = List.of("\0\0\0 ftypisom\0\0\0\0".getBytes(StandardCharsets.ISO_8859_1),
                "<html><body>demo</body></html>".getBytes(StandardCharsets.UTF_8), PNG_CONTENT);
        when(fileApi.presignGetUrlQuietly("https://files.example.com/a.mp3")).thenReturn("https://files.example.com/a.mp3");

        for (byte[] content : contents) {
            AiMusicUploadReqVO reqVO = new AiMusicUploadReqVO().setModelId(MODEL_ID).setFileUrl("https://files.example.com/a.mp3");
            try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
                httpUtilMock.when(() -> HttpUtil.downloadBytes("https://files.example.com/a.mp3")).thenReturn(content);
                // 调用，并断言异常：不插入、不提交
                assertServiceException(() -> musicService.uploadMusic(USER_ID, reqVO), MUSIC_UPLOAD_FILE_INVALID);
            }
        }
        verify(musicMapper, never()).insert(any(AiMusicDO.class));
        verify(sunoApi, never()).uploadAudioUrl(any());
    }

    // ========== 可见性、修改、删除、播放 ==========

    @Test
    public void testGetMusicDetail_owner() {
        // mock 数据：本人生成中的作品
        AiMusicDO music = new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID).setPublicStatus(false)
                .setStatus(AiMusicStatusEnum.IN_PROGRESS.getStatus());
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(music);

        // 调用，并断言
        assertSame(music, musicService.getMusicDetail(MUSIC_ID, USER_ID));
    }

    @Test
    public void testGetMusicDetail_public() {
        // mock 数据：他人已公开、已完成
        AiMusicDO music = new AiMusicDO().setId(MUSIC_ID).setUserId(OTHER_USER_ID).setPublicStatus(true)
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus());
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(music);

        // 调用，并断言
        assertSame(music, musicService.getMusicDetail(MUSIC_ID, USER_ID));
    }

    @Test
    public void testGetMusicDetail_private() {
        // mock 数据：他人未公开
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(OTHER_USER_ID)
                .setPublicStatus(false).setStatus(AiMusicStatusEnum.SUCCESS.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.getMusicDetail(MUSIC_ID, USER_ID), MUSIC_NOT_EXISTS);
    }

    @Test
    public void testGetMusicDetail_publicButFailed() {
        // mock 数据：他人公开，但已失败（例如管理员公开后又失败的数据）
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(OTHER_USER_ID)
                .setPublicStatus(true).setStatus(AiMusicStatusEnum.FAIL.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.getMusicDetail(MUSIC_ID, USER_ID), MUSIC_NOT_EXISTS);
    }

    @Test
    public void testUpdateMyMusic_success() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus()));
        // 准备参数
        AiMusicUpdateMyReqVO reqVO = new AiMusicUpdateMyReqVO().setId(MUSIC_ID).setTitle("新名称")
                .setCoverUrl("https://cdn.example.com/cover.png").setPublicStatus(true);

        // 调用
        musicService.updateMyMusic(reqVO, USER_ID);
        // 断言
        ArgumentCaptor<AiMusicDO> captor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateById(captor.capture());
        assertEquals("新名称", captor.getValue().getTitle());
        assertEquals("https://cdn.example.com/cover.png", captor.getValue().getCoverUrl());
        assertTrue(captor.getValue().getPublicStatus());
        assertNull(captor.getValue().getStatus());
    }

    @Test
    public void testUpdateMyMusic_notOwner() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(OTHER_USER_ID)
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus()).setPublicStatus(true));

        // 调用，并断言异常
        assertServiceException(() -> musicService.updateMyMusic(new AiMusicUpdateMyReqVO().setId(MUSIC_ID)
                .setPublicStatus(false), USER_ID), MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).updateById(any(AiMusicDO.class));
    }

    @Test
    public void testUpdateMyMusic_notSuccess() {
        // mock 数据：未完成的不能公开
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.FAIL.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.updateMyMusic(new AiMusicUpdateMyReqVO().setId(MUSIC_ID)
                .setPublicStatus(true), USER_ID), MUSIC_STATUS_NOT_SUCCESS);
    }

    @Test
    public void testDeleteMusicMy_success() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.FAIL.getStatus()));

        // 调用
        musicService.deleteMusicMy(MUSIC_ID, USER_ID);
        // 断言
        verify(musicMapper).deleteById(MUSIC_ID);
    }

    @Test
    public void testDeleteMusicMy_notOwner() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.deleteMusicMy(MUSIC_ID, OTHER_USER_ID), MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testDeleteMusicMy_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> musicService.deleteMusicMy(MUSIC_ID, USER_ID), MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testDeleteMusicMy_running() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.SAVING.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.deleteMusicMy(MUSIC_ID, USER_ID), MUSIC_STATUS_NOT_FINISHED);
        verify(musicMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testDeleteMusic_running() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.PENDING.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.deleteMusic(MUSIC_ID), MUSIC_STATUS_NOT_FINISHED);
    }

    @Test
    public void testPlayMusic_public() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(OTHER_USER_ID)
                .setPublicStatus(true).setStatus(AiMusicStatusEnum.SUCCESS.getStatus()));

        // 调用
        musicService.playMusic(MUSIC_ID, USER_ID);
        // 断言
        verify(musicMapper).updatePlayCountIncr(MUSIC_ID);
    }

    @Test
    public void testPlayMusic_private() {
        // mock 数据
        when(musicMapper.selectById(MUSIC_ID)).thenReturn(new AiMusicDO().setId(MUSIC_ID).setUserId(OTHER_USER_ID)
                .setPublicStatus(false).setStatus(AiMusicStatusEnum.SUCCESS.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> musicService.playMusic(MUSIC_ID, USER_ID), MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).updatePlayCountIncr(any());
    }

    // ========== 同步：查询、拆分 ==========

    @Test
    public void testFetchMusic_split() {
        // mock 数据
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(List.of("task-1"))).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS",
                null, "100%", List.of(buildClip("clip-1", null), buildClip("clip-2", null)))));
        when(musicMapper.updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), any())).thenReturn(1);

        // 调用
        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(musicService);
            assertEquals(1, musicService.fetchMusic());
        }
        // 断言：占位行更新为第 1 首
        ArgumentCaptor<AiMusicDO> updateCaptor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), updateCaptor.capture());
        assertEquals("clip-1", updateCaptor.getValue().getClipId());
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), updateCaptor.getValue().getStatus());
        assertEquals("https://cdn.example.com/clip-1-large.png", updateCaptor.getValue().getCoverUrl());
        assertEquals(176.5, updateCaptor.getValue().getDuration());
        // 断言：新增第 2 首，共享 task_id，沿用占位行的用户、密钥
        AiMusicDO other = captureInsert();
        assertEquals("clip-2", other.getClipId());
        assertEquals("task-1", other.getTaskId());
        assertEquals(USER_ID, other.getUserId());
        assertEquals(API_KEY_ID, other.getApiKeyId());
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), other.getStatus());
        assertFalse(other.getPublicStatus());
    }

    @Test
    public void testFetchMusic_splitInvalidDuration() {
        // mock 数据：时长为 NaN、负数，其它字段有效
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        SunoApi.Clip nanDuration = new SunoApi.Clip("clip-1", "夏日", "complete", "https://cdn.example.com/clip-1.mp3",
                null, null, null, "chirp-v4", new SunoApi.Metadata("pop", "[Verse]", Double.NaN, null));
        SunoApi.Clip negativeDuration = new SunoApi.Clip("clip-2", "夏日", "complete", "https://cdn.example.com/clip-2.mp3",
                null, null, null, "chirp-v4", new SunoApi.Metadata("pop", "[Verse]", -1.0, null));
        when(sunoApi.fetchTasks(List.of("task-1"))).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS",
                null, "100%", List.of(nanDuration, negativeDuration))));
        when(musicMapper.updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), any())).thenReturn(1);

        // 调用
        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(musicService);
            assertEquals(1, musicService.fetchMusic());
        }
        // 断言：时长留空，两首都正常进入转存
        ArgumentCaptor<AiMusicDO> updateCaptor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), updateCaptor.capture());
        assertEquals("clip-1", updateCaptor.getValue().getClipId());
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), updateCaptor.getValue().getStatus());
        assertNull(updateCaptor.getValue().getDuration());
        AiMusicDO other = captureInsert();
        assertEquals("clip-2", other.getClipId());
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), other.getStatus());
        assertNull(other.getDuration());
    }

    @Test
    public void testFetchMusic_splitAlreadyDone() {
        // mock 数据：条件更新影响 0 行，说明已被其它执行者拆分
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS",
                null, "100%", List.of(buildClip("clip-1", null), buildClip("clip-2", null)))));
        when(musicMapper.updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), any())).thenReturn(0);

        // 调用
        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(musicService);
            musicService.fetchMusic();
        }
        // 断言：不再新增
        verify(musicMapper, never()).insert(any(AiMusicDO.class));
    }

    @Test
    public void testFetchMusic_clipError() {
        // mock 数据：第 1 首出错
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS",
                null, "100%", List.of(buildClip("clip-1", "内容违规")))));
        when(musicMapper.updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), any())).thenReturn(1);

        // 调用
        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(musicService);
            musicService.fetchMusic();
        }
        // 断言：该首直接失败
        ArgumentCaptor<AiMusicDO> updateCaptor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), updateCaptor.capture());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), updateCaptor.getValue().getStatus());
        assertEquals("内容违规", updateCaptor.getValue().getErrorMessage());
    }

    @Test
    public void testFetchMusic_taskFailure() {
        // mock 数据
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "FAILURE",
                "生成超时", null, null)));

        // 调用
        musicService.fetchMusic();
        // 断言
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("生成超时", update.getErrorMessage());
    }

    @Test
    public void testFetchMusic_inProgress() {
        // mock 数据
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "IN_PROGRESS",
                null, "62%", null)));

        // 调用
        musicService.fetchMusic();
        // 断言：只更新进度
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(62, update.getProgress());
        assertNull(update.getStatus());
    }

    @Test
    public void testFetchMusic_apiKeyDeleted() {
        // mock 数据：密钥已删除
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));

        // 调用
        musicService.fetchMusic();
        // 断言
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("渠道密钥已删除", update.getErrorMessage());
        verify(modelService, never()).getSunoApi(any());
    }

    @Test
    public void testFetchMusic_apiKeyDisabled() {
        // mock 数据：密钥停用，仍照常查询
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getSunoApi(apiKey)).thenReturn(sunoApi);

        // 调用
        musicService.fetchMusic();
        // 断言
        verify(sunoApi).fetchTasks(List.of("task-1"));
    }

    @Test
    public void testFetchMusic_fetchError() {
        // mock 数据：查询中转失败
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(any())).thenThrow(new IllegalStateException("中转返回格式不正确"));

        // 调用
        assertEquals(0, musicService.fetchMusic());
        // 断言：保持生成中，下轮重试
        verify(musicMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    public void testFetchMusic_apiKeyClientError() {
        // mock 数据：两个渠道，第 1 个渠道创建客户端失败
        AiMusicDO music1 = buildFetchMusic();
        AiMusicDO music2 = buildFetchMusic().setId(101L).setApiKeyId(21L).setTaskId("task-2");
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music1, music2));
        AiApiKeyDO apiKey1 = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.ENABLE.getStatus());
        AiApiKeyDO apiKey2 = new AiApiKeyDO().setId(21L).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey1);
        when(apiKeyService.getApiKey(21L)).thenReturn(apiKey2);
        when(modelService.getSunoApi(apiKey1)).thenThrow(new IllegalArgumentException("中转地址不正确"));
        when(modelService.getSunoApi(apiKey2)).thenReturn(sunoApi);
        when(sunoApi.fetchTasks(List.of("task-2"))).thenReturn(List.of(new SunoApi.Task("task-2", "MUSIC", "IN_PROGRESS",
                null, "62%", null)));

        // 调用
        assertEquals(1, musicService.fetchMusic());
        // 断言：第 2 个渠道照常处理，第 1 个渠道保持生成中
        AiMusicDO update = captureUpdateByIdAndStatus(101L, AiMusicStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(62, update.getProgress());
        verify(musicMapper, never()).updateByIdAndStatus(eq(MUSIC_ID), any(), any());
    }

    @Test
    public void testFetchMusic_taskError() {
        // mock 数据：同一批次，第 1 首处理异常
        AiMusicDO music1 = buildFetchMusic();
        AiMusicDO music2 = buildFetchMusic().setId(101L).setTaskId("task-2");
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music1, music2));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(List.of("task-1", "task-2"))).thenReturn(List.of(
                new SunoApi.Task("task-1", "MUSIC", "IN_PROGRESS", null, "30%", null),
                new SunoApi.Task("task-2", "MUSIC", "IN_PROGRESS", null, "62%", null)));
        when(musicMapper.updateByIdAndStatus(eq(MUSIC_ID), any(), any())).thenThrow(new RuntimeException("数据库异常"));

        // 调用
        assertEquals(1, musicService.fetchMusic());
        // 断言：第 2 首照常更新
        AiMusicDO update = captureUpdateByIdAndStatus(101L, AiMusicStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(62, update.getProgress());
    }

    @Test
    public void testFetchMusic_splitInvalidClips() {
        // mock 数据：空值、空编号、重复编号的歌曲跳过；没有音频、视频地址不是 http(s) 的歌曲
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        SunoApi.Clip noAudio = new SunoApi.Clip("clip-3", "夏日", "complete", null, null, null,
                "javascript:alert(1)", "chirp-v4", null);
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS", null, "100%",
                Arrays.asList(null, buildClip(" ", null), buildClip("clip-1", null), buildClip("clip-1", null), noAudio))));
        when(musicMapper.updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), any())).thenReturn(1);

        // 调用
        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(musicService);
            assertEquals(1, musicService.fetchMusic());
        }
        // 断言：占位行更新为第 1 首有效歌曲
        ArgumentCaptor<AiMusicDO> updateCaptor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), updateCaptor.capture());
        assertEquals("clip-1", updateCaptor.getValue().getClipId());
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), updateCaptor.getValue().getStatus());
        // 断言：重复的不再插入；没有音频的该首失败，视频地址置空
        AiMusicDO other = captureInsert();
        assertEquals("clip-3", other.getClipId());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), other.getStatus());
        assertEquals("中转未返回音频", other.getErrorMessage());
        assertNull(other.getVideoUrl());
    }

    @Test
    public void testFetchMusic_splitNoValidClip() {
        // mock 数据：成功但没有有效歌曲
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS", null, "100%",
                Arrays.asList(null, buildClip("", null)))));

        // 调用
        musicService.fetchMusic();
        // 断言：占位行失败，不拆分
        AiMusicDO update = captureUpdateByIdAndStatus(MUSIC_ID, AiMusicStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(AiMusicStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("中转返回成功，但没有有效的歌曲", update.getErrorMessage());
        verify(musicMapper, never()).updateByIdAndUnsplit(any(), any(), any());
    }

    @Test
    public void testFetchMusic_splitOtherHost() {
        // mock 数据：第 1、2 首媒体在不同的 CDN 域名，都不需要登记
        AiMusicDO music = buildFetchMusic();
        when(musicMapper.selectListForFetch(eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), anyInt())).thenReturn(List.of(music));
        mockFetchApiKey();
        SunoApi.Clip other = new SunoApi.Clip("clip-2", "夏日", "complete", "https://media.example.org/2.mp3",
                "https://media.example.org/2.png", null, "https://media.example.org/2.mp4", "chirp-v4", null);
        when(sunoApi.fetchTasks(any())).thenReturn(List.of(new SunoApi.Task("task-1", "MUSIC", "SUCCESS", null, "100%",
                List.of(buildClip("clip-1", null), other))));
        when(musicMapper.updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), any())).thenReturn(1);

        // 调用
        try (MockedStatic<SpringUtil> springUtilMock = mockStatic(SpringUtil.class)) {
            springUtilMock.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(musicService);
            musicService.fetchMusic();
        }
        // 断言：第 1 首正常进入转存
        ArgumentCaptor<AiMusicDO> updateCaptor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateByIdAndUnsplit(eq(MUSIC_ID), eq(AiMusicStatusEnum.IN_PROGRESS.getStatus()), updateCaptor.capture());
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), updateCaptor.getValue().getStatus());
        assertEquals("https://cdn.example.com/clip-1.mp3", updateCaptor.getValue().getAudioUrl());
        // 断言：第 2 首同样进入转存，是否公网地址在下载时校验
        AiMusicDO insert = captureInsert();
        assertEquals(AiMusicStatusEnum.SAVING.getStatus(), insert.getStatus());
        assertNull(insert.getErrorMessage());
        assertEquals("https://media.example.org/2.mp3", insert.getAudioUrl());
        assertEquals("https://media.example.org/2.png", insert.getCoverUrl());
        assertEquals("https://media.example.org/2.mp4", insert.getVideoUrl());
    }

    @Test
    public void testSyncMusic_fetchError() {
        // mock 方法：查询整体异常
        doThrow(new RuntimeException("查询异常")).when(musicService).fetchMusic();
        doReturn(1).when(musicService).transferMusic();
        doReturn(2).when(musicService).timeoutMusic();

        // 调用，并断言：转存、超时照常执行
        assertEquals(3, musicService.syncMusic());
        verify(musicService).transferMusic();
        verify(musicService).timeoutMusic();
    }

    // ========== 同步：转存、超时 ==========

    @Test
    public void testTransferMusic_partialFail() {
        // mock 数据：第 1 首下载失败，第 2 首成功
        AiMusicDO music1 = new AiMusicDO().setId(1L).setStatus(AiMusicStatusEnum.SAVING.getStatus())
                .setCoverUrl("https://cdn.example.com/1.png").setAudioUrl("https://cdn.example.com/1.mp3");
        AiMusicDO music2 = new AiMusicDO().setId(2L).setStatus(AiMusicStatusEnum.SAVING.getStatus())
                .setCoverUrl("https://cdn.example.com/2.png").setAudioUrl("https://cdn.example.com/2.mp3");
        when(musicMapper.selectListByStatus(eq(AiMusicStatusEnum.SAVING.getStatus()), anyInt()))
                .thenReturn(List.of(music1, music2));
        when(fileApi.createFile(PNG_CONTENT)).thenReturn("https://oss/2-cover.png");
        when(fileApi.createFile(MP3_CONTENT)).thenReturn("https://oss/2.mp3");

        // mock 静态方法：第 1 首音频下载失败，第 2 首正常
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://cdn.example.com/1.mp3"))
                    .thenThrow(new IORuntimeException("下载失败（HTTP 404）"));
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://cdn.example.com/2.png")).thenReturn(PNG_CONTENT);
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://cdn.example.com/2.mp3")).thenReturn(MP3_CONTENT);

            // 调用
            assertEquals(1, musicService.transferMusic());
        }
        // 断言：只有第 2 首完成，第 1 首保持转存中
        AiMusicDO update = captureUpdateForSave(2L);
        assertEquals(AiMusicStatusEnum.SUCCESS.getStatus(), update.getStatus());
        assertEquals("https://oss/2-cover.png", update.getCoverUrl());
        assertEquals("https://oss/2.mp3", update.getAudioUrl());
        verify(musicMapper, never()).updateForSave(eq(1L), any(), any());
        verify(musicMapper, never()).updateByIdAndStatus(eq(1L), any(), any());
    }

    @Test
    public void testTransferMusic_coverFail() {
        // mock 数据：封面下载失败，音频正常
        AiMusicDO music = new AiMusicDO().setId(1L).setStatus(AiMusicStatusEnum.SAVING.getStatus())
                .setCoverUrl("https://cdn.example.com/1.png").setAudioUrl("https://cdn.example.com/1.mp3");
        when(musicMapper.selectListByStatus(eq(AiMusicStatusEnum.SAVING.getStatus()), anyInt())).thenReturn(List.of(music));
        when(fileApi.createFile(MP3_CONTENT)).thenReturn("https://oss/1.mp3");

        // mock 静态方法：封面下载失败
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://cdn.example.com/1.mp3")).thenReturn(MP3_CONTENT);
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://cdn.example.com/1.png"))
                    .thenThrow(new IORuntimeException("下载失败（HTTP 404）"));

            // 调用
            assertEquals(1, musicService.transferMusic());
        }
        // 断言：封面置空，不阻塞完成
        AiMusicDO update = captureUpdateForSave(1L);
        assertEquals(AiMusicStatusEnum.SUCCESS.getStatus(), update.getStatus());
        assertEquals("https://oss/1.mp3", update.getAudioUrl());
        assertNull(update.getCoverUrl());
        verify(fileApi, times(1)).createFile(any(byte[].class));
    }

    @Test
    public void testTimeoutMusic() {
        // mock 数据：待处理、生成中、转存中各一条超时
        AiMusicDO music1 = new AiMusicDO().setId(1L).setStatus(AiMusicStatusEnum.PENDING.getStatus());
        AiMusicDO music2 = new AiMusicDO().setId(2L).setStatus(AiMusicStatusEnum.IN_PROGRESS.getStatus());
        AiMusicDO music3 = new AiMusicDO().setId(3L).setStatus(AiMusicStatusEnum.SAVING.getStatus())
                .setAudioUrl("https://cdn.example.com/3.mp3");
        when(musicMapper.selectListByStatusAndCreateTimeLt(any(), any())).thenReturn(List.of(music1, music2, music3));

        // 调用
        int count;
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            count = musicService.timeoutMusic();
            // 断言：只更新状态，不再下载文件
            httpUtilMock.verify(() -> HttpUtil.downloadBytes(anyString()), never());
        }
        assertEquals(3, count);
        // 断言：都按原状态条件更新为已失败
        for (AiMusicDO music : List.of(music1, music2, music3)) {
            AiMusicDO update = captureUpdateByIdAndStatus(music.getId(), music.getStatus());
            assertEquals(AiMusicStatusEnum.FAIL.getStatus(), update.getStatus());
            assertEquals("任务超时", update.getErrorMessage());
        }
        // 断言：超时时间
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(musicMapper).selectListByStatusAndCreateTimeLt(any(), timeCaptor.capture());
        assertTrue(timeCaptor.getValue().isBefore(LocalDateTime.now().minusMinutes(AiMusicServiceImpl.TIMEOUT_MINUTES - 1)));
    }

    // ========== 工具方法 ==========

    @Test
    public void testParseProgress() {
        assertEquals(62, AiMusicServiceImpl.parseProgress("62%"));
        assertEquals(99, AiMusicServiceImpl.parseProgress("100%"));
        assertEquals(1, AiMusicServiceImpl.parseProgress("0%"));
        assertEquals(45, AiMusicServiceImpl.parseProgress("45.6"));
        assertNull(AiMusicServiceImpl.parseProgress(""));
        assertNull(AiMusicServiceImpl.parseProgress("排队中"));
    }

    @Test
    public void testParseLyric() {
        AiMusicLyricRespVO respVO = AiMusicServiceImpl.parseLyric("\n**《夏日海风》**\n\n[Verse]\n海风吹过沙滩\n\n[Chorus]\n奔跑\n");
        assertEquals("夏日海风", respVO.getTitle());
        assertEquals("[Verse]\n海风吹过沙滩\n\n[Chorus]\n奔跑", respVO.getLyric());

        respVO = AiMusicServiceImpl.parseLyric("歌曲名称：夏日\n[Verse]\n歌词");
        assertEquals("夏日", respVO.getTitle());
        assertEquals("[Verse]\n歌词", respVO.getLyric());

        // 首行就是段落标记时，不当作名称
        respVO = AiMusicServiceImpl.parseLyric("[Verse]\n歌词");
        assertNull(respVO.getTitle());
        assertEquals("[Verse]\n歌词", respVO.getLyric());
    }

    private void mockMusicModel() {
        AiModelDO model = new AiModelDO().setId(MODEL_ID).setKeyId(API_KEY_ID).setModel("chirp-v4")
                .setType(AiModelTypeEnum.MUSIC.getType()).setPlatform(AiPlatformEnum.SUNO.getPlatform());
        when(modelService.validateModel(MODEL_ID)).thenReturn(model);
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(apiKeyService.validateApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getSunoApi(apiKey)).thenReturn(sunoApi);
    }

    private void mockFetchApiKey() {
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(API_KEY_ID).setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey);
        when(modelService.getSunoApi(apiKey)).thenReturn(sunoApi);
    }

    private void mockInsert() {
        doAnswer(invocation -> {
            ((AiMusicDO) invocation.getArgument(0)).setId(MUSIC_ID);
            return 1;
        }).when(musicMapper).insert(any(AiMusicDO.class));
    }

    private AiMusicDO captureInsert() {
        ArgumentCaptor<AiMusicDO> captor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).insert(captor.capture());
        return captor.getValue();
    }

    private AiMusicDO captureUpdateByIdAndStatus(Long id, Integer whereStatus) {
        ArgumentCaptor<AiMusicDO> captor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateByIdAndStatus(eq(id), eq(whereStatus), captor.capture());
        return captor.getValue();
    }

    private static byte[] buildMp3() {
        byte[] content = new byte[10 + 417 * 2];
        content[0] = 'I';
        content[1] = 'D';
        content[2] = '3';
        content[3] = 4;
        for (int i = 0; i < 2; i++) {
            int frame = 10 + 417 * i;
            content[frame] = (byte) 0xFF;
            content[frame + 1] = (byte) 0xFB; // MPEG-1 Layer III
            content[frame + 2] = (byte) 0x90; // 128kbps、44.1kHz、无填充
        }
        return content;
    }

    private static byte[] buildWav() {
        ByteBuffer buffer = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(40)
                .put("WAVE".getBytes(StandardCharsets.US_ASCII))
                .put("fmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16)
                .putShort((short) 1).putShort((short) 1).putInt(8000).putInt(8000).putShort((short) 1).putShort((short) 8)
                .put("data".getBytes(StandardCharsets.US_ASCII)).putInt(4).put(new byte[]{1, 2, 3, 4});
        return buffer.array();
    }

    private AiMusicDO captureUpdateForSave(Long id) {
        ArgumentCaptor<AiMusicDO> captor = ArgumentCaptor.forClass(AiMusicDO.class);
        verify(musicMapper).updateForSave(eq(id), eq(AiMusicStatusEnum.SAVING.getStatus()), captor.capture());
        return captor.getValue();
    }

    private static AiMusicDO buildRefMusic(Integer generateMode) {
        return new AiMusicDO().setId(50L).setUserId(USER_ID).setGenerateMode(generateMode)
                .setPlatform(AiPlatformEnum.SUNO.getPlatform()).setModelId(MODEL_ID).setModel("chirp-v4")
                .setApiKeyId(API_KEY_ID).setTitle("原曲").setTags("pop").setMakeInstrumental(false)
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus()).setDuration(120.0)
                .setTaskId("task-ref").setClipId("clip-ref");
    }

    private static AiMusicDO buildFetchMusic() {
        return new AiMusicDO().setId(MUSIC_ID).setUserId(USER_ID).setApiKeyId(API_KEY_ID)
                .setGenerateMode(AiMusicGenerateModeEnum.INSPIRATION.getMode()).setModel("chirp-v4")
                .setStatus(AiMusicStatusEnum.IN_PROGRESS.getStatus()).setProgress(1).setTaskId("task-1")
                .setPublicStatus(false).setPlayCount(0);
    }

    private static SunoApi.Clip buildClip(String id, String errorMessage) {
        com.fasterxml.jackson.databind.JsonNode errorNode = errorMessage != null
                ? com.fasterxml.jackson.databind.node.TextNode.valueOf(errorMessage) : null;
        return new SunoApi.Clip(id, "夏日", errorMessage != null ? "error" : "complete",
                errorMessage != null ? null : "https://cdn.example.com/" + id + ".mp3",
                "https://cdn.example.com/" + id + ".png", "https://cdn.example.com/" + id + "-large.png",
                "", "chirp-v4", new SunoApi.Metadata("pop", "[Verse]", 176.5, errorNode));
    }

}
