package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardVideoReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaStoryboardMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoOutput;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.video.AiVideoService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiDramaStoryboardServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiDramaStoryboardServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long STORYBOARD_ID = 200L;
    private static final Long VIDEO_MODEL_ID = 10L;
    private static final Long API_KEY_ID = 20L;

    @InjectMocks
    private AiDramaStoryboardServiceImpl storyboardService;

    @Mock
    private AiDramaStoryboardMapper storyboardMapper;
    @Mock
    private AiDramaProjectService projectService;
    @Mock
    private AiDramaScriptService scriptService;
    @Mock
    private AiDramaAssetService assetService;
    @Mock
    private AiVideoService videoService;
    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private FileApi fileApi;

    // ========== buildVideo ==========

    @Test
    public void testBuildVideo() {
        AiModelDO model = videoModel();
        // 有起始帧：图生视频
        AiVideoDO image = AiDramaStoryboardServiceImpl.buildVideo(storyboard().setFirstFrameUrl("https://files.example.com/f.png"),
                model, "提示词", "9:16");
        assertEquals(AiVideoModeEnum.IMAGE.getMode(), image.getMode());
        assertEquals(List.of("https://files.example.com/f.png"), image.getInputUrls().get("first_frame"));
        assertEquals("9:16", image.getRatio());
        assertEquals(5, image.getDuration());
        assertEquals("doubao-seedance-1-0-pro", image.getModel());
        // 没有起始帧：文生视频
        AiVideoDO text = AiDramaStoryboardServiceImpl.buildVideo(storyboard(), model, null, null);
        assertEquals(AiVideoModeEnum.TEXT.getMode(), text.getMode());
        assertNull(text.getInputUrls());
    }

    // ========== 修改 ==========

    @Test
    public void testUpdateStoryboardMy_running() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setVideoStatus(AiDramaStatusEnum.SAVING.getStatus()));
        AiDramaStoryboardSaveReqVO reqVO = new AiDramaStoryboardSaveReqVO();
        reqVO.setId(STORYBOARD_ID);

        assertServiceException(() -> storyboardService.updateStoryboardMy(USER_ID, reqVO), DRAMA_TASK_RUNNING);
    }

    @Test
    public void testUpdateStoryboardMy_otherUser() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard());
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenThrow(exception(DRAMA_PROJECT_NOT_EXISTS));
        AiDramaStoryboardSaveReqVO reqVO = new AiDramaStoryboardSaveReqVO();
        reqVO.setId(STORYBOARD_ID);

        assertServiceException(() -> storyboardService.updateStoryboardMy(USER_ID, reqVO), DRAMA_PROJECT_NOT_EXISTS);
        verify(storyboardMapper, never()).updateForSave(any(AiDramaStoryboardDO.class));
    }

    @Test
    public void testUpdateStoryboardMy_clearLocation() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setLocationId(6L));
        AiDramaStoryboardSaveReqVO reqVO = new AiDramaStoryboardSaveReqVO();
        reqVO.setId(STORYBOARD_ID);
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setAppearanceIds(Collections.emptyList());
        reqVO.setVisualAssetIds(Collections.emptyList());

        storyboardService.updateStoryboardMy(USER_ID, reqVO);

        // 断言：经编辑保存写入，场景为空（由 updateForSave 清空），不改所属项目
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateForSave(captor.capture());
        assertEquals(STORYBOARD_ID, captor.getValue().getId());
        assertNull(captor.getValue().getLocationId());
        assertNull(captor.getValue().getProjectId());
        assertEquals(Collections.emptyList(), captor.getValue().getAppearanceIds());
        verify(storyboardMapper, never()).updateById(any(AiDramaStoryboardDO.class));
    }

    // ========== 提交 ==========

    @Test
    public void testGenerateStoryboardVideoMy_success() {
        mockSubmit();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：提示词带上角色、场景、台词、画风
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        String prompt = videoCaptor.getValue().getPrompt();
        assertTrue(prompt.startsWith("列车进站"));
        assertTrue(prompt.contains("场景：车厢（深夜）"));
        assertTrue(prompt.contains("角色：林夏（短发）"));
        assertTrue(prompt.contains("台词：林夏：最后一班了"));
        assertTrue(prompt.endsWith("画风：电影感"));
        // 断言：先置为待处理（记下本次的提交编号），再以它为条件更新为生成中
        ArgumentCaptor<AiDramaStoryboardDO> pendingCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateById(pendingCaptor.capture());
        assertEquals(AiDramaStatusEnum.PENDING.getStatus(), pendingCaptor.getValue().getVideoStatus());
        assertEquals(VIDEO_MODEL_ID, pendingCaptor.getValue().getVideoModelId());
        assertNotNull(pendingCaptor.getValue().getTaskId());
        ArgumentCaptor<AiDramaStoryboardDO> progressCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                eq(pendingCaptor.getValue().getTaskId()), progressCaptor.capture());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), progressCaptor.getValue().getVideoStatus());
        assertEquals("task-1", progressCaptor.getValue().getTaskId());
    }

    @Test
    public void testGenerateStoryboardVideoMy_submitFail() {
        mockSubmit();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> {
            throw new AiVideoException("余额不足");
        });

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        ArgumentCaptor<AiDramaStoryboardDO> pendingCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateById(pendingCaptor.capture());
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                eq(pendingCaptor.getValue().getTaskId()), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("余额不足", captor.getValue().getErrorMessage());
    }

    @Test
    public void testGenerateStoryboardVideoMy_resubmittedDuringSubmit() {
        mockSubmit();
        // 准备：库中的记录；置为待处理时记下提交编号
        AtomicReference<Integer> dbStatus = new AtomicReference<>();
        AtomicReference<String> dbTaskId = new AtomicReference<>();
        List<AiDramaStoryboardDO> written = new ArrayList<>();
        when(storyboardMapper.updateById(any(AiDramaStoryboardDO.class))).thenAnswer(invocation -> {
            AiDramaStoryboardDO updateObj = invocation.getArgument(0);
            dbStatus.set(updateObj.getVideoStatus());
            dbTaskId.set(updateObj.getTaskId());
            return 1;
        });
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), any(), any(), any())).thenAnswer(invocation -> {
            if (!Objects.equals(invocation.getArgument(1), dbStatus.get())
                    || !Objects.equals(invocation.getArgument(2), dbTaskId.get())) {
                return 0;
            }
            written.add(invocation.getArgument(3));
            return 1;
        });
        // 准备：提交服务商期间，被管理员标记失败并重新提交，库中已是新一轮的待处理
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> {
            dbStatus.set(AiDramaStatusEnum.FAIL.getStatus());
            dbStatus.set(AiDramaStatusEnum.PENDING.getStatus());
            dbTaskId.set("submit-new");
            return new AiVideoTaskRef("seedance", "m", "task-old", null, null);
        });

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：旧的提交结果不覆盖新一轮
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                argThat(taskId -> !"submit-new".equals(taskId)), any());
        assertTrue(written.isEmpty());
        assertEquals(AiDramaStatusEnum.PENDING.getStatus(), dbStatus.get());
        assertEquals("submit-new", dbTaskId.get());
    }

    @Test
    public void testGenerateStoryboardVideoBatchMy_paramsInvalid() {
        AiModelDO model = videoModel();
        when(videoService.validateVideoModel(VIDEO_MODEL_ID)).thenReturn(model);
        when(videoService.validateVideoApiKey(model)).thenReturn(apiKey());
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project());
        when(storyboardMapper.selectListByProjectId(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard().setId(1L), storyboard().setId(2L).setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()),
                storyboard().setId(3L).setVideoStatus(AiDramaStatusEnum.FAIL.getStatus())));
        when(videoService.buildVideoSubmitter(any(), any())).thenThrow(exception(VIDEO_PARAMS_INVALID, "时长只支持 5、10 秒"));

        int count = storyboardService.generateStoryboardVideoBatchMy(USER_ID, videoReqVO(PROJECT_ID));

        // 断言：已完成的跳过；参数不通过的记为已失败
        assertEquals(2, count);
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper, times(2)).updateById(captor.capture());
        assertEquals(Arrays.asList(1L, 3L), captor.getAllValues().stream().map(AiDramaStoryboardDO::getId).toList());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("视频参数不正确：时长只支持 5、10 秒", captor.getValue().getErrorMessage());
    }

    @Test
    public void testResubmitStoryboardVideo_notFail() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> storyboardService.resubmitStoryboardVideo(STORYBOARD_ID), DRAMA_TASK_NOT_FAIL);
    }

    // ========== 同步 ==========

    @Test
    public void testSyncStoryboardVideo_success() {
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-1").setVideoModelId(VIDEO_MODEL_ID);
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));
        when(modelService.getModel(VIDEO_MODEL_ID)).thenReturn(videoModel());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey());
        AiVideoTaskResult result = mock(AiVideoTaskResult.class);
        when(result.isFinished()).thenReturn(true);
        when(result.isSucceeded()).thenReturn(true);
        AiVideoOutput output = mock(AiVideoOutput.class);
        when(output.getUrl()).thenReturn("https://provider.example.com/v.mp4");
        when(result.getVideos()).thenReturn(Collections.singletonList(output));
        when(videoService.getVideoTask(any(), any())).thenReturn(result);
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), any())).thenReturn(1);
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://files.example.com/v.mp4");

        int count;
        try (MockedStatic<HttpUtil> httpUtil = mockStatic(HttpUtil.class)) {
            httpUtil.when(() -> HttpUtil.downloadBytes("https://provider.example.com/v.mp4")).thenReturn(new byte[]{1});
            count = storyboardService.syncStoryboardVideo();
        }

        assertEquals(1, count);
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.SAVING.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("https://files.example.com/v.mp4", captor.getValue().getVideoUrl());
    }

    @Test
    public void testSyncStoryboardVideo_resubmittedDuringQuery() {
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-old").setVideoModelId(VIDEO_MODEL_ID);
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));
        when(modelService.getModel(VIDEO_MODEL_ID)).thenReturn(videoModel());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey());
        AiVideoTaskResult result = mock(AiVideoTaskResult.class);
        when(result.isFinished()).thenReturn(true);
        when(result.isSucceeded()).thenReturn(true);
        when(videoService.getVideoTask(any(), any())).thenReturn(result);
        // 准备：查询服务商期间，被管理员标记失败并重新提交，库中的任务编号已是 task-new：以 task-old 为条件的更新不命中
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-old"), any())).thenReturn(0);

        assertEquals(0, storyboardService.syncStoryboardVideo());

        // 断言：旧任务的结果不转存，也不再写回
        verify(fileApi, never()).createFile(any(byte[].class));
        verify(storyboardMapper, times(1)).updateByIdAndVideoStatusAndTaskId(any(), any(), any(), any());
    }

    @Test
    public void testSyncStoryboardVideo_modelDeleted() {
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-1").setVideoModelId(VIDEO_MODEL_ID);
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));

        assertEquals(1, storyboardService.syncStoryboardVideo());

        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("视频模型已删除", captor.getValue().getErrorMessage());
        verify(videoService, never()).getVideoTask(any(), any());
    }

    @Test
    public void testSyncStoryboardVideoById_notRunning() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setVideoStatus(AiDramaStatusEnum.PENDING.getStatus()));

        assertServiceException(() -> storyboardService.syncStoryboardVideo(STORYBOARD_ID), DRAMA_TASK_NOT_RUNNING);
    }

    // ========== 生成任务 ==========

    @Test
    public void testUpdateStoryboardVideoFail_notRunning() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> storyboardService.updateStoryboardVideoFail(STORYBOARD_ID, "管理员标记失败"),
                DRAMA_TASK_NOT_RUNNING);
        verify(storyboardMapper, never()).updateByIdAndVideoStatusAndTaskId(any(), any(), any(), any());
    }

    @Test
    public void testUpdateStoryboardVideoFail_success() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setVideoStatus(AiDramaStatusEnum.PENDING.getStatus()).setTaskId("submit-1"));

        storyboardService.updateStoryboardVideoFail(STORYBOARD_ID, "管理员标记失败");

        // 断言：以读取到的状态、任务编号为条件
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                eq("submit-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("管理员标记失败", captor.getValue().getErrorMessage());
    }

    @Test
    public void testTimeoutStoryboardVideo() {
        when(storyboardMapper.selectListByVideoStatusAndUpdateTimeLt(any(), any())).thenReturn(List.of(
                storyboard().setVideoStatus(AiDramaStatusEnum.SAVING.getStatus()).setTaskId("task-1")));
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.SAVING.getStatus()),
                eq("task-1"), any())).thenReturn(1);

        assertEquals(1, storyboardService.timeoutStoryboardVideo(LocalDateTime.now(), "任务超时"));

        // 断言：包含待处理、生成中、转存中；以查询到的状态、任务编号为条件
        verify(storyboardMapper).selectListByVideoStatusAndUpdateTimeLt(eq(Arrays.asList(AiDramaStatusEnum.PENDING.getStatus(),
                AiDramaStatusEnum.IN_PROGRESS.getStatus(), AiDramaStatusEnum.SAVING.getStatus())), any());
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.SAVING.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("任务超时", captor.getValue().getErrorMessage());
    }

    // ========== 工具 ==========

    private void mockSubmit() {
        AiDramaStoryboardDO storyboard = storyboard()
                .setDialogues(Collections.singletonList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了")))
                .setAppearanceIds(List.of(11L)).setLocationId(31L);
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard);
        AiModelDO model = videoModel();
        when(videoService.validateVideoModel(VIDEO_MODEL_ID)).thenReturn(model);
        when(videoService.validateVideoApiKey(model)).thenReturn(apiKey());
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project());
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Collections.singletonList(
                new AiDramaCharacterDO().setId(1L).setProjectId(PROJECT_ID).setName("林夏").setDescription("短发")));
        when(assetService.getLocation(31L)).thenReturn(new AiDramaLocationDO().setId(31L).setProjectId(PROJECT_ID)
                .setName("车厢").setDescription("深夜"));
        when(assetService.getAppearanceList(List.of(11L))).thenReturn(Collections.singletonList(
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L)));
    }

    private static AiDramaStoryboardDO storyboard() {
        return new AiDramaStoryboardDO().setId(STORYBOARD_ID).setProjectId(PROJECT_ID).setSceneNo(1).setTitle("进站")
                .setDuration(5).setVideoPrompt("列车进站");
    }

    private static AiDramaProjectDO project() {
        return new AiDramaProjectDO().setId(PROJECT_ID).setUserId(USER_ID).setAspectRatio("9:16").setStyle("电影感");
    }

    private static AiModelDO videoModel() {
        return new AiModelDO().setId(VIDEO_MODEL_ID).setType(AiModelTypeEnum.VIDEO.getType())
                .setPlatform(AiPlatformEnum.BYTEDANCE.getPlatform()).setModel("doubao-seedance-1-0-pro").setKeyId(API_KEY_ID);
    }

    private static AiApiKeyDO apiKey() {
        return new AiApiKeyDO().setId(API_KEY_ID).setPlatform(AiPlatformEnum.BYTEDANCE.getPlatform());
    }

    private static AiDramaStoryboardVideoReqVO videoReqVO(Long id) {
        AiDramaStoryboardVideoReqVO reqVO = new AiDramaStoryboardVideoReqVO();
        reqVO.setId(id);
        reqVO.setModelId(VIDEO_MODEL_ID);
        return reqVO;
    }

}
