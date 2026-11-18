package cn.iocoder.yudao.module.ai.service.video;

import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.AiVideoGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.AiVideoUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.dal.mysql.video.AiVideoMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoStatusEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.*;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiVideoUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;

import java.util.*;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiVideoServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiVideoServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long MODEL_ID = 10L;
    private static final Long API_KEY_ID = 20L;
    private static final Long VIDEO_ID = 100L;
    private static final String VERTEX_URL = "https://us-central1-aiplatform.googleapis.com/v1/projects/demo-project/locations/us-central1";

    @Spy
    @InjectMocks
    private AiVideoServiceImpl videoService;

    @Mock
    private AiVideoMapper videoMapper;
    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private FileApi fileApi;

    // ========== generateVideo ==========

    @Test
    public void testGenerateVideo_success() {
        // mock 方法
        mockVideoModel(AiPlatformEnum.MINIMAX.getPlatform());
        mockInsert();
        doReturn(submitter("task-1")).when(videoService).buildVideoSubmitter(any(), any());
        // 准备参数：图生只保留首帧，尾帧、参考图忽略
        Map<String, List<String>> inputUrls = new HashMap<>();
        inputUrls.put("first_frame", Collections.singletonList("https://files.example.com/a.png"));
        inputUrls.put("last_frame", Collections.singletonList("https://files.example.com/b.png"));
        inputUrls.put("reference_image", Arrays.asList("https://files.example.com/c.png", " "));
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType())
                .setMode(AiVideoModeEnum.IMAGE.getMode()).setModelId(MODEL_ID).setPrompt(" 奔跑的猫 ")
                .setDuration(6).setInputUrls(inputUrls);

        // 调用
        Long id = videoService.generateVideo(USER_ID, reqVO);
        // 断言：插入记录
        assertEquals(VIDEO_ID, id);
        AiVideoDO video = captureInsert();
        assertEquals(USER_ID, video.getUserId());
        assertEquals(AiVideoStatusEnum.PENDING.getStatus(), video.getStatus());
        assertEquals(API_KEY_ID, video.getApiKeyId());
        assertEquals("MiniMax-H3", video.getModel());
        assertEquals("奔跑的猫", video.getPrompt());
        assertEquals(6, video.getDuration());
        assertEquals(Collections.singleton("first_frame"), video.getInputUrls().keySet());
        assertFalse(video.getPublicStatus());
        // 断言：写入 task_id，进入生成中
        AiVideoDO update = captureUpdateByIdAndStatus(AiVideoStatusEnum.PENDING.getStatus());
        assertEquals("task-1", update.getTaskId());
        assertEquals(AiVideoStatusEnum.IN_PROGRESS.getStatus(), update.getStatus());
    }

    @Test
    public void testGenerateVideo_defaultTextMode() {
        // mock 方法
        mockVideoModel(AiPlatformEnum.MINIMAX.getPlatform());
        mockInsert();
        doReturn(submitter("task-1")).when(videoService).buildVideoSubmitter(any(), any());
        // 准备参数：未传方式时为文生，输入文件全部忽略
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType())
                .setModelId(MODEL_ID).setPrompt("猫")
                .setInputUrls(Collections.singletonMap("first_frame", Collections.singletonList("https://files.example.com/a.png")));

        // 调用
        videoService.generateVideo(USER_ID, reqVO);
        // 断言
        AiVideoDO video = captureInsert();
        assertEquals(AiVideoModeEnum.TEXT.getMode(), video.getMode());
        assertNull(video.getInputUrls());
    }

    @Test
    public void testGenerateVideo_modelTypeError() {
        // mock 方法：对话模型
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID)
                .setType(AiModelTypeEnum.CHAT.getType()).setPlatform(AiPlatformEnum.MINIMAX.getPlatform()));
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType()).setModelId(MODEL_ID);

        // 调用，并断言异常：不插入
        assertServiceException(() -> videoService.generateVideo(USER_ID, reqVO), VIDEO_MODEL_TYPE_ERROR);
        verify(videoMapper, never()).insert(any(AiVideoDO.class));
    }

    @Test
    public void testGenerateVideo_platformMismatch() {
        // mock 方法：模型为 MiniMax，密钥为阿里
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(API_KEY_ID)
                .setType(AiModelTypeEnum.VIDEO.getType()).setPlatform(AiPlatformEnum.MINIMAX.getPlatform()));
        when(apiKeyService.validateApiKey(API_KEY_ID)).thenReturn(new AiApiKeyDO().setId(API_KEY_ID)
                .setPlatform(AiPlatformEnum.ALIBABA.getPlatform()));
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType()).setModelId(MODEL_ID);

        // 调用，并断言异常
        assertServiceException(() -> videoService.generateVideo(USER_ID, reqVO), VIDEO_MODEL_PLATFORM_MISMATCH,
                AiPlatformEnum.MINIMAX.getPlatform(), AiPlatformEnum.ALIBABA.getPlatform());
        verify(videoMapper, never()).insert(any(AiVideoDO.class));
    }

    @Test
    public void testGenerateVideo_avatarPlatformInvalid() {
        // mock 方法：数字人口播使用 MiniMax 的模型
        mockVideoModel(AiPlatformEnum.MINIMAX.getPlatform());
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.AVATAR.getType()).setModelId(MODEL_ID);

        // 调用，并断言异常：不插入
        assertServiceException(() -> videoService.generateVideo(USER_ID, reqVO), VIDEO_PARAMS_INVALID,
                "数字人口播、动作模仿目前只支持字节跳动（即梦）的模型");
        verify(videoMapper, never()).insert(any(AiVideoDO.class));
    }

    @Test
    public void testGenerateVideo_avatarIgnoreParams() {
        // mock 方法
        mockVideoModel(AiPlatformEnum.BYTEDANCE.getPlatform());
        mockInsert();
        doReturn(submitter("task-1")).when(videoService).buildVideoSubmitter(any(), any());
        // 准备参数：数字人口播传了分辨率、比例、时长
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.AVATAR.getType()).setModelId(MODEL_ID)
                .setResolution("720p").setRatio("16:9").setDuration(5);

        // 调用
        videoService.generateVideo(USER_ID, reqVO);
        // 断言：不保存生成方式、分辨率、比例、时长
        AiVideoDO video = captureInsert();
        assertNull(video.getMode());
        assertNull(video.getResolution());
        assertNull(video.getRatio());
        assertNull(video.getDuration());
    }

    @Test
    public void testGenerateVideo_paramsInvalid() {
        // mock 方法：本地校验不通过
        mockVideoModel(AiPlatformEnum.MINIMAX.getPlatform());
        doThrow(exception(VIDEO_PARAMS_INVALID, "尾帧必须与首帧一起使用")).when(videoService).buildVideoSubmitter(any(), any());
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType()).setModelId(MODEL_ID);

        // 调用，并断言异常：不插入、不提交
        assertServiceException(() -> videoService.generateVideo(USER_ID, reqVO), VIDEO_PARAMS_INVALID, "尾帧必须与首帧一起使用");
        verify(videoMapper, never()).insert(any(AiVideoDO.class));
    }

    @Test
    public void testGenerateVideo_submitOutcomeUnknown() {
        // mock 方法：提交超时，结果未知
        mockVideoModel(AiPlatformEnum.MINIMAX.getPlatform());
        mockInsert();
        doReturn(submitter(new AiVideoException("请求超时", true))).when(videoService).buildVideoSubmitter(any(), any());
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType())
                .setModelId(MODEL_ID).setPrompt("猫");

        // 调用：不抛出
        videoService.generateVideo(USER_ID, reqVO);
        // 断言：置为已失败，提示不要重复提交
        AiVideoDO update = captureUpdateByIdAndStatus(AiVideoStatusEnum.PENDING.getStatus());
        assertEquals(AiVideoStatusEnum.FAIL.getStatus(), update.getStatus());
        assertTrue(update.getErrorMessage().contains("请勿重复提交"));
        assertNull(update.getTaskId());
    }

    @Test
    public void testGenerateVideo_submitRejected() {
        // mock 方法：服务商明确拒绝
        mockVideoModel(AiPlatformEnum.MINIMAX.getPlatform());
        mockInsert();
        doReturn(submitter(new AiVideoException("余额不足"))).when(videoService).buildVideoSubmitter(any(), any());
        AiVideoGenerateReqVO reqVO = new AiVideoGenerateReqVO().setType(AiVideoTypeEnum.GENERATE.getType())
                .setModelId(MODEL_ID).setPrompt("猫");

        // 调用
        videoService.generateVideo(USER_ID, reqVO);
        // 断言：原因原样展示
        AiVideoDO update = captureUpdateByIdAndStatus(AiVideoStatusEnum.PENDING.getStatus());
        assertEquals(AiVideoStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("余额不足", update.getErrorMessage());
    }

    // ========== buildVideoSubmitter ==========

    @Test
    public void testBuildVideoSubmitter_veoText() {
        AiVideoDO video = veoVideo("veo-3.1-fast-generate-001");

        assertNotNull(videoService.buildVideoSubmitter(video, veoApiKey(VERTEX_URL)));
    }

    @Test
    public void testBuildVideoSubmitter_veoUrlInvalid() {
        AiVideoDO video = veoVideo("veo-3.1-fast-generate-001");

        assertServiceException(() -> videoService.buildVideoSubmitter(video, veoApiKey("https://generativelanguage.googleapis.com")),
                VIDEO_PARAMS_INVALID, "Veo 需要在 API 密钥的地址中填写 Vertex AI 地址，例如 "
                        + "https://us-central1-aiplatform.googleapis.com/v1/projects/{项目 ID}/locations/us-central1");
    }

    @Test
    public void testBuildVideoSubmitter_geminiNotVeo() {
        AiVideoDO video = veoVideo("gemini-2.5-pro");

        assertServiceException(() -> videoService.buildVideoSubmitter(video, veoApiKey(VERTEX_URL)),
                VIDEO_PARAMS_INVALID, "Gemini 平台的视频生成只支持 Veo 模型（veo- 开头）");
    }

    @Test
    public void testBuildVideoSubmitter_veoDurationInvalid() {
        AiVideoDO video = veoVideo("veo-3.1-fast-generate-001").setDuration(5);

        // 由客户端的 buildRequestBody 校验：不支持的时长直接拒绝
        ServiceException exception = assertThrows(ServiceException.class,
                () -> videoService.buildVideoSubmitter(video, veoApiKey(VERTEX_URL)));
        assertEquals(VIDEO_PARAMS_INVALID.getCode(), exception.getCode());
    }

    @Test
    public void testBuildVideoSubmitter_referenceUnused() {
        // 可灵：不提交参考素材
        AiVideoDO kling = new AiVideoDO().setType(AiVideoTypeEnum.GENERATE.getType()).setMode(AiVideoModeEnum.REFERENCE.getMode())
                .setPlatform(AiPlatformEnum.KLING.getPlatform()).setModel("kling-2.6").setPrompt("海边的日落").setDuration(5)
                .setInputUrls(Map.of("reference_image", List.of("https://files.example.com/a.png")));
        assertServiceException(() -> videoService.buildVideoSubmitter(kling, new AiApiKeyDO()),
                VIDEO_PARAMS_INVALID, "模型(kling-2.6)不支持参考图");
        // Veo：只提交参考图
        AiVideoDO veo = veoVideo("veo-3.1-fast-generate-001").setMode(AiVideoModeEnum.REFERENCE.getMode())
                .setInputUrls(Map.of("reference_audio", List.of("https://files.example.com/v.mp3")));
        assertServiceException(() -> videoService.buildVideoSubmitter(veo, veoApiKey(VERTEX_URL)),
                VIDEO_PARAMS_INVALID, "模型(veo-3.1-fast-generate-001)不支持参考音频");
        // 海螺：只提交 1 张主体参考图
        when(fileApi.presignGetUrlQuietly(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        AiVideoDO hailuo = new AiVideoDO().setType(AiVideoTypeEnum.GENERATE.getType()).setMode(AiVideoModeEnum.REFERENCE.getMode())
                .setPlatform(AiPlatformEnum.MINIMAX.getPlatform()).setModel("MiniMax-Hailuo-02").setPrompt("海边的日落")
                .setInputUrls(Map.of("reference_image", List.of("https://files.example.com/a.png", "https://files.example.com/b.png")));
        assertServiceException(() -> videoService.buildVideoSubmitter(hailuo, new AiApiKeyDO()),
                VIDEO_PARAMS_INVALID, "模型(MiniMax-Hailuo-02)的主体参考图最多 1 张");
    }

    // ========== filterInputUrls ==========

    @Test
    public void testFilterInputUrls_avatar() {
        // 准备参数
        Map<String, List<String>> inputUrls = new HashMap<>();
        inputUrls.put("image", Collections.singletonList("https://files.example.com/a.png"));
        inputUrls.put("audio", Collections.singletonList("https://files.example.com/a.mp3"));
        inputUrls.put("video", Collections.singletonList("https://files.example.com/a.mp4"));
        inputUrls.put("first_frame", Collections.singletonList("https://files.example.com/b.png"));

        // 调用
        Map<String, List<String>> result = AiVideoUtils.filterInputUrls(AiVideoTypeEnum.AVATAR.getType(), null, inputUrls);
        // 断言：只保留人物图片、驱动音频
        assertEquals(new HashSet<>(Arrays.asList("image", "audio")), result.keySet());
    }

    @Test
    public void testFilterInputUrls_motion() {
        // 准备参数
        Map<String, List<String>> inputUrls = new HashMap<>();
        inputUrls.put("image", Collections.singletonList("https://files.example.com/a.png"));
        inputUrls.put("audio", Collections.singletonList("https://files.example.com/a.mp3"));
        inputUrls.put("video", Collections.singletonList("https://files.example.com/a.mp4"));

        // 调用
        Map<String, List<String>> result = AiVideoUtils.filterInputUrls(AiVideoTypeEnum.MOTION.getType(), null, inputUrls);
        // 断言：只保留人物图片、动作视频
        assertEquals(new HashSet<>(Arrays.asList("image", "video")), result.keySet());
    }

    // ========== fetchVideo ==========

    @Test
    public void testFetchVideo_running() {
        // mock 方法
        AiVideoDO video = mockRunningVideo(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        doReturn(buildResult(AiVideoTaskStatus.RUNNING)).when(videoService).getVideoTask(eq(video), any());

        // 调用
        assertEquals(0, videoService.fetchVideo());
        // 断言：不更新
        verify(videoMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    public void testFetchVideo_failed() {
        // mock 方法
        AiVideoDO video = mockRunningVideo(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        AiVideoTaskResult result = buildResult(AiVideoTaskStatus.FAILED);
        result.setErrorMessage("内容审核未通过");
        doReturn(result).when(videoService).getVideoTask(eq(video), any());

        // 调用
        assertEquals(1, videoService.fetchVideo());
        // 断言
        AiVideoDO update = captureUpdateByIdAndStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(AiVideoStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("内容审核未通过", update.getErrorMessage());
        assertNotNull(update.getFinishTime());
    }

    @Test
    public void testFetchVideo_successAndTransfer() {
        // mock 方法
        AiVideoDO video = mockRunningVideo(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        AiVideoTaskResult result = buildResult(AiVideoTaskStatus.SUCCEEDED);
        result.getVideos().add(new AiVideoOutput().setUrl("https://cdn.example.com/1.mp4?sign=1").setDurationSeconds(6.04));
        doReturn(result).when(videoService).getVideoTask(eq(video), any());
        when(videoMapper.updateByIdAndStatus(any(), any(), any())).thenReturn(1);
        byte[] content = new byte[]{1, 2, 3};
        when(fileApi.createFile(content)).thenReturn("https://files.example.com/1.mp4");

        // 调用
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes("https://cdn.example.com/1.mp4?sign=1")).thenReturn(content);
            assertEquals(1, videoService.fetchVideo());
        }
        // 断言：先进入转存中，再更新为已完成
        ArgumentCaptor<AiVideoDO> captor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoMapper).updateByIdAndStatus(eq(VIDEO_ID), eq(AiVideoStatusEnum.IN_PROGRESS.getStatus()), captor.capture());
        assertEquals(AiVideoStatusEnum.SAVING.getStatus(), captor.getValue().getStatus());
        verify(videoMapper).updateByIdAndStatus(eq(VIDEO_ID), eq(AiVideoStatusEnum.SAVING.getStatus()), captor.capture());
        AiVideoDO update = captor.getValue();
        assertEquals(AiVideoStatusEnum.SUCCESS.getStatus(), update.getStatus());
        assertEquals("https://files.example.com/1.mp4", update.getVideoUrl());
        assertEquals(6, update.getDuration()); // 未指定时长，取服务商返回值
        assertEquals(100, update.getProgress());
    }

    @Test
    public void testFetchVideo_transferFail() {
        // mock 方法：转存中的任务，下载失败
        AiVideoDO video = mockRunningVideo(AiVideoStatusEnum.SAVING.getStatus());
        AiVideoTaskResult result = buildResult(AiVideoTaskStatus.SUCCEEDED);
        result.getVideos().add(new AiVideoOutput().setUrl("https://cdn.example.com/1.mp4"));
        doReturn(result).when(videoService).getVideoTask(eq(video), any());

        // 调用
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(anyString())).thenThrow(new RuntimeException("下载失败"));
            assertEquals(0, videoService.fetchVideo());
        }
        // 断言：保持转存中，不重新提交
        verify(videoMapper, never()).updateByIdAndStatus(any(), any(), any());
        verify(videoService, never()).buildVideoSubmitter(any(), any());
    }

    @Test
    public void testFetchVideo_apiKeyDeleted() {
        // mock 方法：密钥已删除
        AiVideoDO video = new AiVideoDO().setId(VIDEO_ID).setApiKeyId(API_KEY_ID).setTaskId("task-1")
                .setStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        when(videoMapper.selectListByStatus(anyCollection(), anyInt())).thenReturn(Collections.singletonList(video));

        // 调用
        assertEquals(1, videoService.fetchVideo());
        // 断言
        AiVideoDO update = captureUpdateByIdAndStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(AiVideoStatusEnum.FAIL.getStatus(), update.getStatus());
        verify(videoService, never()).getVideoTask(any(), any());
    }

    // ========== timeoutVideo ==========

    @Test
    public void testTimeoutVideo() {
        // mock 方法
        when(videoMapper.selectListByStatusAndCreateTimeLt(anyCollection(), any())).thenReturn(Collections.singletonList(
                new AiVideoDO().setId(VIDEO_ID).setStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus())));

        // 调用
        assertEquals(1, videoService.timeoutVideo());
        // 断言
        AiVideoDO update = captureUpdateByIdAndStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus());
        assertEquals(AiVideoStatusEnum.FAIL.getStatus(), update.getStatus());
        assertEquals("任务超时", update.getErrorMessage());
    }

    // ========== 可见、修改、删除 ==========

    @Test
    public void testGetVideoDetail_otherPrivate() {
        // mock 方法：他人的未公开作品
        when(videoMapper.selectById(VIDEO_ID)).thenReturn(new AiVideoDO().setId(VIDEO_ID).setUserId(OTHER_USER_ID)
                .setStatus(AiVideoStatusEnum.SUCCESS.getStatus()).setPublicStatus(false));

        // 调用，并断言异常
        assertServiceException(() -> videoService.getVideoDetail(VIDEO_ID, USER_ID), VIDEO_NOT_EXISTS);
    }

    @Test
    public void testGetVideoDetail_otherPublic() {
        // mock 方法：他人已公开、已完成
        AiVideoDO video = new AiVideoDO().setId(VIDEO_ID).setUserId(OTHER_USER_ID)
                .setStatus(AiVideoStatusEnum.SUCCESS.getStatus()).setPublicStatus(true);
        when(videoMapper.selectById(VIDEO_ID)).thenReturn(video);

        // 调用，并断言
        assertSame(video, videoService.getVideoDetail(VIDEO_ID, USER_ID));
    }

    @Test
    public void testUpdateVideo_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> videoService.updateVideo(new AiVideoUpdateReqVO().setId(VIDEO_ID).setPublicStatus(true)),
                VIDEO_NOT_EXISTS);
        verify(videoMapper, never()).updateById(any(AiVideoDO.class));
    }

    @Test
    public void testUpdateVideo_notSuccess() {
        // mock 方法
        when(videoMapper.selectById(VIDEO_ID)).thenReturn(new AiVideoDO().setId(VIDEO_ID).setUserId(USER_ID)
                .setStatus(AiVideoStatusEnum.FAIL.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> videoService.updateVideo(new AiVideoUpdateReqVO().setId(VIDEO_ID).setPublicStatus(true)),
                VIDEO_STATUS_NOT_SUCCESS);
    }

    @Test
    public void testUpdateVideo_success() {
        // mock 方法：管理员公开，不校验作者
        when(videoMapper.selectById(VIDEO_ID)).thenReturn(new AiVideoDO().setId(VIDEO_ID).setUserId(OTHER_USER_ID)
                .setStatus(AiVideoStatusEnum.SUCCESS.getStatus()));

        // 调用
        videoService.updateVideo(new AiVideoUpdateReqVO().setId(VIDEO_ID).setPublicStatus(true));
        // 断言：只更新是否公开
        ArgumentCaptor<AiVideoDO> captor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoMapper).updateById(captor.capture());
        assertEquals(VIDEO_ID, captor.getValue().getId());
        assertTrue(captor.getValue().getPublicStatus());
        assertNull(captor.getValue().getStatus());
    }

    @Test
    public void testDeleteVideoMy_running() {
        // mock 方法
        when(videoMapper.selectById(VIDEO_ID)).thenReturn(new AiVideoDO().setId(VIDEO_ID).setUserId(USER_ID)
                .setStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> videoService.deleteVideoMy(VIDEO_ID, USER_ID), VIDEO_STATUS_NOT_FINISHED);
        verify(videoMapper, never()).deleteById(any(Long.class));
    }

    // ========== 工具方法 ==========

    private void mockVideoModel(String platform) {
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setKeyId(API_KEY_ID)
                .setType(AiModelTypeEnum.VIDEO.getType()).setPlatform(platform).setModel("MiniMax-H3"));
        when(apiKeyService.validateApiKey(API_KEY_ID)).thenReturn(new AiApiKeyDO().setId(API_KEY_ID).setPlatform(platform));
    }

    private void mockInsert() {
        doAnswer(invocation -> {
            invocation.<AiVideoDO>getArgument(0).setId(VIDEO_ID);
            return 1;
        }).when(videoMapper).insert(any(AiVideoDO.class));
    }

    private AiVideoDO mockRunningVideo(Integer status) {
        AiVideoDO video = new AiVideoDO().setId(VIDEO_ID).setApiKeyId(API_KEY_ID).setTaskId("task-1")
                .setPlatform(AiPlatformEnum.MINIMAX.getPlatform()).setModel("MiniMax-H3").setStatus(status);
        when(videoMapper.selectListByStatus(anyCollection(), anyInt())).thenReturn(Collections.singletonList(video));
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(new AiApiKeyDO().setId(API_KEY_ID));
        return video;
    }

    private static Supplier<AiVideoTaskRef> submitter(String taskId) {
        return () -> new AiVideoTaskRef("minimax-v2", "MiniMax-H3", taskId, null, null);
    }

    private static Supplier<AiVideoTaskRef> submitter(AiVideoException exception) {
        return () -> {
            throw exception;
        };
    }

    private static AiVideoDO veoVideo(String model) {
        return new AiVideoDO().setType(AiVideoTypeEnum.GENERATE.getType()).setMode(AiVideoModeEnum.TEXT.getMode())
                .setPlatform(AiPlatformEnum.GEMINI.getPlatform()).setModel(model).setPrompt("海边的日落")
                .setRatio("16:9").setDuration(8).setResolution("720p");
    }

    private static AiApiKeyDO veoApiKey(String url) {
        return new AiApiKeyDO().setId(1L).setPlatform(AiPlatformEnum.GEMINI.getPlatform()).setApiKey("ya29.test-token").setUrl(url);
    }

    private static AiVideoTaskResult buildResult(AiVideoTaskStatus status) {
        AiVideoTaskResult result = new AiVideoTaskResult();
        result.setTaskId("task-1");
        result.setStatus(status);
        return result;
    }

    private AiVideoDO captureInsert() {
        ArgumentCaptor<AiVideoDO> captor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoMapper).insert(captor.capture());
        return captor.getValue();
    }

    private AiVideoDO captureUpdateByIdAndStatus(Integer whereStatus) {
        ArgumentCaptor<AiVideoDO> captor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoMapper).updateByIdAndStatus(eq(VIDEO_ID), eq(whereStatus), captor.capture());
        return captor.getValue();
    }

}
