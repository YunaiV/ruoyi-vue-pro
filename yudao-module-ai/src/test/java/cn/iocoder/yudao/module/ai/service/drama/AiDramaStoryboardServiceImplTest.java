package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardFrameReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardImportVideoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardVideoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaVideoSubmissionResolveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaStoryboardMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaVideoSubmissionMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaVisualAssetKindEnum;
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
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    private AiDramaVideoSubmissionMapper videoSubmissionMapper;
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
        AiVideoDO image = AiDramaStoryboardServiceImpl.buildVideo(storyboard(), model, "提示词",
                "https://files.example.com/f.png", "9:16");
        assertEquals(AiVideoModeEnum.IMAGE.getMode(), image.getMode());
        assertEquals(Arrays.asList("https://files.example.com/f.png"), image.getInputUrls().get("first_frame"));
        assertEquals("9:16", image.getRatio());
        assertEquals(5, image.getDuration());
        assertEquals("doubao-seedance-1-0-pro", image.getModel());
        // 没有起始帧：文生视频
        AiVideoDO text = AiDramaStoryboardServiceImpl.buildVideo(storyboard(), model, null, null, null);
        assertEquals(AiVideoModeEnum.TEXT.getMode(), text.getMode());
        assertNull(text.getInputUrls());
    }

    // ========== 剧本已修改、道具、关键帧 ==========

    @Test
    public void testGenerateStoryboardVideoMy_scriptChanged() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard());
        AiModelDO model = videoModel();
        when(videoService.validateVideoModel(VIDEO_MODEL_ID)).thenReturn(model);
        when(videoService.validateVideoApiKey(model)).thenReturn(apiKey());
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project()
                .setStatus(AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus()));

        assertServiceException(() -> storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID)),
                DRAMA_SCRIPT_CHANGED);
        verify(storyboardMapper, never()).updateById(any(AiDramaStoryboardDO.class));
    }

    @Test
    public void testGenerateStoryboardVideoMy_propsByShotNumbers() {
        mockSubmit();
        // 准备：镜头未指定道具，按道具关联的镜头序号匹配
        when(assetService.getPropList(PROJECT_ID)).thenReturn(Arrays.asList(
                prop(41L, "车票", null).setReferenceImages(new AiDramaVisualAssetDO.ReferenceImages().setShotNumbers(Arrays.asList(1, 2))),
                prop(42L, "雨伞", null).setReferenceImages(new AiDramaVisualAssetDO.ReferenceImages().setShotNumbers(Arrays.asList(3)))));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        assertTrue(videoCaptor.getValue().getPrompt().contains("道具：车票。"));
        assertFalse(videoCaptor.getValue().getPrompt().contains("雨伞"));
    }

    @Test
    public void testGenerateStoryboardFrameMy_revise() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setDialogues(Collections.singletonList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了"))));
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project());
        when(assetService.getStoryboardFrame(STORYBOARD_ID)).thenReturn(new AiDramaVisualAssetDO().setId(61L)
                .setStoryboardId(STORYBOARD_ID).setImageUrl("https://files.example.com/k.png"));
        AiDramaStoryboardFrameReqVO reqVO = new AiDramaStoryboardFrameReqVO();
        reqVO.setId(STORYBOARD_ID);
        reqVO.setModelId(1L);
        reqVO.setRevisePrompt("人物靠左");

        storyboardService.generateStoryboardFrameMy(USER_ID, reqVO);

        // 断言：当前关键帧作为第 1 张参考图，记为上一版；提示词不带台词
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AiDramaVisualAssetDO.ReferenceImages> referenceCaptor = ArgumentCaptor.forClass(AiDramaVisualAssetDO.ReferenceImages.class);
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(assetService).generateStoryboardFrame(any(), eq(1L), promptCaptor.capture(), referenceCaptor.capture(),
                hashCaptor.capture(), eq("9:16"));
        assertTrue(promptCaptor.getValue().startsWith("以参考图1（当前画面）为基础修正构图：人物靠左"));
        assertFalse(promptCaptor.getValue().contains("台词"));
        assertEquals("https://files.example.com/k.png", referenceCaptor.getValue().getUrls().get(0));
        assertEquals("https://files.example.com/k.png", referenceCaptor.getValue().getPreviousImageUrl());
        // 断言：输入摘要按原始输入（不含修正说明、上一版关键帧）计算
        String basePrompt = promptCaptor.getValue().substring(promptCaptor.getValue().indexOf("短剧镜头的关键帧画面"));
        assertEquals(DigestUtil.sha256Hex(basePrompt + Collections.emptyList()
                + referenceCaptor.getValue().getPropsVersion()), hashCaptor.getValue());
    }

    @Test
    public void testGenerateStoryboardFrameMy_planContinuity() {
        // 准备：分镜规划了起始状态、空间锚点、0 秒出镜角色（只有林夏）与背景群演
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setImagePrompt("车门半开")
                .setCharacters(Arrays.asList(characterRef(11L), characterRef(12L)))
                .setContinuity(new AiDramaStoryboardDO.Continuity().setStartState("林夏扶着车门")
                        .setSpatialAnchor("林夏在画面左侧").setPresentCharacters(Arrays.asList("林夏"))
                        .setBackgroundExtras("两名乘客坐在后排")));
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project());
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaCharacterDO().setId(1L).setProjectId(PROJECT_ID).setName("林夏"),
                new AiDramaCharacterDO().setId(2L).setProjectId(PROJECT_ID).setName("周远")));
        when(assetService.getAppearanceList(Arrays.asList(11L, 12L))).thenReturn(Arrays.asList(
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L).setSelectedIndex(0)
                        .setImageUrls(Arrays.asList("https://files.example.com/a.png")),
                new AiDramaCharacterAppearanceDO().setId(12L).setCharacterId(2L).setSelectedIndex(0)
                        .setImageUrls(Arrays.asList("https://files.example.com/b.png"))));
        AiDramaStoryboardFrameReqVO reqVO = new AiDramaStoryboardFrameReqVO();
        reqVO.setId(STORYBOARD_ID);
        reqVO.setModelId(1L);

        storyboardService.generateStoryboardFrameMy(USER_ID, reqVO);

        // 断言：提示词带上规划字段，只画 0 秒出镜的林夏；参考图只带林夏的形象
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AiDramaVisualAssetDO.ReferenceImages> referenceCaptor = ArgumentCaptor.forClass(AiDramaVisualAssetDO.ReferenceImages.class);
        verify(assetService).generateStoryboardFrame(any(), eq(1L), promptCaptor.capture(), referenceCaptor.capture(),
                any(), eq("9:16"));
        String prompt = promptCaptor.getValue();
        assertTrue(prompt.contains("起始状态（只画此刻，不提前画出结束动作）：林夏扶着车门"));
        assertTrue(prompt.contains("空间与视线：林夏在画面左侧"));
        assertTrue(prompt.contains("0 秒出镜角色：林夏，其他角色不出现在画面中"));
        assertTrue(prompt.contains("背景群演（匿名，不画成具名角色）：两名乘客坐在后排"));
        assertTrue(prompt.contains("角色：林夏"));
        assertFalse(prompt.contains("周远"));
        assertEquals(Arrays.asList("https://files.example.com/a.png"), referenceCaptor.getValue().getUrls());
    }

    @Test
    public void testGenerateStoryboardFrameMy_reviseWithoutFrame() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard());
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project());
        AiDramaStoryboardFrameReqVO reqVO = new AiDramaStoryboardFrameReqVO();
        reqVO.setId(STORYBOARD_ID);
        reqVO.setModelId(1L);
        reqVO.setRevisePrompt("人物靠左");

        assertServiceException(() -> storyboardService.generateStoryboardFrameMy(USER_ID, reqVO), DRAMA_FIRST_FRAME_EMPTY);
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
    public void testCreateStoryboardMy_scriptOfOtherProject() {
        when(scriptService.getScript(9L)).thenReturn(new AiDramaScriptDO().setId(9L).setProjectId(PROJECT_ID + 1));
        AiDramaStoryboardSaveReqVO reqVO = new AiDramaStoryboardSaveReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setScriptId(9L);

        assertServiceException(() -> storyboardService.createStoryboardMy(USER_ID, reqVO), DRAMA_SCRIPT_NOT_EXISTS);
        verify(storyboardMapper, never()).insert(any(AiDramaStoryboardDO.class));
    }

    @Test
    public void testGetStoryboardListByScriptIdMy() {
        // 准备：项目有历史版本 9 与当前版本 10 的分镜
        when(scriptService.getScript(9L)).thenReturn(new AiDramaScriptDO().setId(9L).setProjectId(PROJECT_ID));
        when(storyboardMapper.selectListByProjectId(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard().setScriptId(9L), new AiDramaStoryboardDO().setId(STORYBOARD_ID + 1).setProjectId(PROJECT_ID)
                        .setScriptId(10L).setSceneNo(1)));

        List<AiDramaStoryboardDO> list = storyboardService.getStoryboardListByScriptIdMy(USER_ID, 9L);

        // 断言：只返回该剧本版本的分镜，并校验项目归属
        assertEquals(Arrays.asList(STORYBOARD_ID), list.stream().map(AiDramaStoryboardDO::getId).collect(Collectors.toList()));
        verify(projectService).validateProjectMy(PROJECT_ID, USER_ID);
    }

    @Test
    public void testGetStoryboardListByScriptIdMy_otherUser() {
        when(scriptService.getScript(9L)).thenReturn(new AiDramaScriptDO().setId(9L).setProjectId(PROJECT_ID));
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenThrow(exception(DRAMA_PROJECT_NOT_EXISTS));

        assertServiceException(() -> storyboardService.getStoryboardListByScriptIdMy(USER_ID, 9L), DRAMA_PROJECT_NOT_EXISTS);
        verify(storyboardMapper, never()).selectListByProjectId(any());
    }

    @Test
    public void testGetStoryboardMy_historyScript() {
        // 准备：历史剧本版本的分镜，不在项目详情中
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setScriptId(9L));

        AiDramaStoryboardDO storyboard = storyboardService.getStoryboardMy(USER_ID, STORYBOARD_ID);

        // 断言：仍能获得，用于画布核对节点绑定的镜头所属版本
        assertEquals(9L, storyboard.getScriptId());
        verify(projectService).validateProjectMy(PROJECT_ID, USER_ID);
    }

    @Test
    public void testGetStoryboardMy_notExists() {
        assertNull(storyboardService.getStoryboardMy(USER_ID, STORYBOARD_ID));
        verify(projectService, never()).validateProjectMy(any(), any());
    }

    @Test
    public void testUpdateStoryboardMy_sceneNoDuplicate() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setScriptId(9L).setSceneNo(2));
        when(storyboardMapper.selectListByProjectId(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaStoryboardDO().setId(STORYBOARD_ID - 1).setProjectId(PROJECT_ID).setScriptId(9L).setSceneNo(1),
                storyboard().setScriptId(9L).setSceneNo(2)));
        AiDramaStoryboardSaveReqVO reqVO = new AiDramaStoryboardSaveReqVO();
        reqVO.setId(STORYBOARD_ID);
        reqVO.setSceneNo(1);

        // 断言：同一剧本版本中已有序号 1 的镜头
        assertThrows(ServiceException.class, () -> storyboardService.updateStoryboardMy(USER_ID, reqVO));
        verify(storyboardMapper, never()).updateForSave(any(AiDramaStoryboardDO.class));
    }

    @Test
    public void testDeleteStoryboardMy_running() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        // 断言：镜头视频生成中不能删除
        assertServiceException(() -> storyboardService.deleteStoryboardMy(USER_ID, STORYBOARD_ID), DRAMA_TASK_RUNNING);
        verify(storyboardMapper, never()).deleteById(any(Long.class));
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
        reqVO.setCharacters(Collections.emptyList());
        reqVO.setVisualAssetIds(Collections.emptyList());

        storyboardService.updateStoryboardMy(USER_ID, reqVO);

        // 断言：经编辑保存写入，场景为空（由 updateForSave 清空），不改所属项目
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateForSave(captor.capture());
        assertEquals(STORYBOARD_ID, captor.getValue().getId());
        assertNull(captor.getValue().getLocationId());
        assertNull(captor.getValue().getProjectId());
        assertEquals(Collections.emptyList(), captor.getValue().getCharacters());
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
        assertEquals("", pendingCaptor.getValue().getVideoResolution());
        assertNotNull(pendingCaptor.getValue().getTaskId());
        ArgumentCaptor<AiDramaStoryboardDO> progressCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                eq(pendingCaptor.getValue().getTaskId()), progressCaptor.capture());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), progressCaptor.getValue().getVideoStatus());
        assertEquals("task-1", progressCaptor.getValue().getTaskId());
        // 断言：提交记录以请求编号记录参数哈希、模型快照，受理后记下上游任务编号
        ArgumentCaptor<AiDramaVideoSubmissionDO> submissionCaptor = ArgumentCaptor.forClass(AiDramaVideoSubmissionDO.class);
        verify(videoSubmissionMapper).insert(submissionCaptor.capture());
        AiDramaVideoSubmissionDO submission = submissionCaptor.getValue();
        assertEquals(pendingCaptor.getValue().getTaskId(), submission.getRequestId());
        assertEquals(AiDramaStatusEnum.PENDING.getStatus(), submission.getStatus());
        assertEquals("doubao-seedance-1-0-pro", submission.getModel());
        assertEquals(64, submission.getParametersHash().length());
        assertNotNull(submission.getPreviousVideo());
        verify(videoSubmissionMapper).updateByIdAndStatus(any(), eq(AiDramaStatusEnum.PENDING.getStatus()),
                argThat(updateObj -> "task-1".equals(updateObj.getTaskId())
                        && AiDramaStatusEnum.IN_PROGRESS.getStatus().equals(updateObj.getStatus())));
    }

    @Test
    public void testGenerateStoryboardVideoMy_sameRequestId() {
        mockSubmit();
        AtomicInteger submitCount = new AtomicInteger();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> {
            submitCount.incrementAndGet();
            return new AiVideoTaskRef("seedance", "m", "task-1", null, null);
        });
        AiDramaStoryboardVideoReqVO reqVO = videoReqVO(STORYBOARD_ID);
        reqVO.setRequestId("req-00000001");
        storyboardService.generateStoryboardVideoMy(USER_ID, reqVO);
        ArgumentCaptor<AiDramaVideoSubmissionDO> submissionCaptor = ArgumentCaptor.forClass(AiDramaVideoSubmissionDO.class);
        verify(videoSubmissionMapper).insert(submissionCaptor.capture());
        when(videoSubmissionMapper.selectByRequestId("req-00000001")).thenReturn(submissionCaptor.getValue());

        // 调用：同一请求编号、相同参数重复提交（例如前端网络重试）
        storyboardService.generateStoryboardVideoMy(USER_ID, reqVO);

        // 断言：只提交服务商一次
        assertEquals(1, submitCount.get());
        verify(videoSubmissionMapper, times(1)).insert(any(AiDramaVideoSubmissionDO.class));
    }

    @Test
    public void testGenerateStoryboardVideoMy_requestIdParamsChanged() {
        mockSubmit();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));
        when(videoSubmissionMapper.selectByRequestId("req-00000001")).thenReturn(new AiDramaVideoSubmissionDO()
                .setStoryboardId(STORYBOARD_ID).setRequestId("req-00000001").setParametersHash("other"));
        AiDramaStoryboardVideoReqVO reqVO = videoReqVO(STORYBOARD_ID);
        reqVO.setRequestId("req-00000001");

        assertServiceException(() -> storyboardService.generateStoryboardVideoMy(USER_ID, reqVO),
                DRAMA_VIDEO_SUBMISSION_PARAMS_CHANGED);
        verify(storyboardMapper, never()).updateById(any(AiDramaStoryboardDO.class));
    }

    @Test
    public void testGenerateStoryboardVideoMy_submitOutcomeUnknown() {
        mockSubmit();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> {
            throw new AiVideoException("读取超时", true);
        });
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), any(), any(), any())).thenReturn(1);

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：分镜为已失败；提交记录只记下原因，仍为待处理（结果未知），等待用户确认
        ArgumentCaptor<AiDramaVideoSubmissionDO> captor = ArgumentCaptor.forClass(AiDramaVideoSubmissionDO.class);
        verify(videoSubmissionMapper).updateByStoryboardIdAndTaskId(eq(STORYBOARD_ID), any(), captor.capture());
        assertNull(captor.getValue().getStatus());
        assertTrue(captor.getValue().getErrorMessage().startsWith("提交结果未知"));
        verify(videoSubmissionMapper, never()).updateByIdAndStatus(any(), any(), any());
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
        // 断言：明确失败，提交记录为已失败
        verify(videoSubmissionMapper).updateByIdAndStatus(any(), eq(AiDramaStatusEnum.PENDING.getStatus()),
                argThat(updateObj -> AiDramaStatusEnum.FAIL.getStatus().equals(updateObj.getStatus())
                        && "余额不足".equals(updateObj.getErrorMessage())));
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
    public void testGenerateStoryboardVideoMy_reference() {
        mockSubmit();
        mockReferenceAssets();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));
        AiDramaStoryboardVideoReqVO reqVO = videoReqVO(STORYBOARD_ID);
        reqVO.setResolution("768P");

        storyboardService.generateStoryboardVideoMy(USER_ID, reqVO);

        // 断言：没有起始帧，带上角色形象、场景、道具的图片，以及发言角色的选用样音；提示词说明每个参考素材对应谁
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        AiVideoDO video = videoCaptor.getValue();
        assertEquals(AiVideoModeEnum.REFERENCE.getMode(), video.getMode());
        assertEquals("768P", video.getResolution());
        assertEquals(Arrays.asList("https://files.example.com/a.png", "https://files.example.com/l.png", "https://files.example.com/p.png"),
                video.getInputUrls().get("reference_image"));
        assertEquals(Arrays.asList("https://files.example.com/v.mp3"), video.getInputUrls().get("reference_audio"));
        assertTrue(video.getPrompt().contains("角色：林夏（短发）"));
        assertTrue(video.getPrompt().endsWith("参考素材：参考图1为林夏的形象；参考图2为场景车厢；参考图3为道具车票；参考音频1为林夏的音色"));
        // 断言：记录分辨率，供重新提交沿用
        ArgumentCaptor<AiDramaStoryboardDO> pendingCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateById(pendingCaptor.capture());
        assertEquals("768P", pendingCaptor.getValue().getVideoResolution());
    }

    @Test
    public void testGenerateStoryboardVideoMy_referenceSpeakers() {
        mockSubmit();
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L)
                .setDialogues(Arrays.asList(new AiDramaStoryboardDO.Dialogue().setCharacterId(2L).setLine("还有人吗"),
                        new AiDramaStoryboardDO.Dialogue().setLine("车门关上了"),
                        new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了"),
                        new AiDramaStoryboardDO.Dialogue().setCharacterId(2L).setLine("走吧"))));
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaCharacterDO().setId(1L).setProjectId(PROJECT_ID).setName("林夏").setSampleAudioId(51L),
                new AiDramaCharacterDO().setId(2L).setProjectId(PROJECT_ID).setName("周远").setSampleAudioId(52L)));
        when(assetService.getAudioList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaAudioDO().setId(51L).setProjectId(PROJECT_ID).setCharacterId(1L)
                        .setType(AiDramaAudioTypeEnum.SAMPLE.getType()).setFileUrl("https://files.example.com/v1.mp3"),
                new AiDramaAudioDO().setId(52L).setProjectId(PROJECT_ID).setCharacterId(2L)
                        .setType(AiDramaAudioTypeEnum.SAMPLE.getType()).setFileUrl("https://files.example.com/v2.mp3")));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：多个发言角色，按发言顺序各带一段样音，提示词逐个说明音色对应的角色；旁白不带
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        AiVideoDO video = videoCaptor.getValue();
        assertEquals(AiVideoModeEnum.REFERENCE.getMode(), video.getMode());
        assertEquals(Arrays.asList("https://files.example.com/v2.mp3", "https://files.example.com/v1.mp3"),
                video.getInputUrls().get("reference_audio"));
        assertTrue(video.getPrompt().endsWith("参考素材：参考音频1为周远的音色；参考音频2为林夏的音色"));
    }

    @Test
    public void testGenerateStoryboardVideoMy_referenceSharedUrl() {
        mockSubmit();
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L)
                .setVisualAssetIds(Arrays.asList(41L, 42L))
                .setDialogues(Arrays.asList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了"),
                        new AiDramaStoryboardDO.Dialogue().setCharacterId(2L).setLine("走吧"))));
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaCharacterDO().setId(1L).setProjectId(PROJECT_ID).setName("林夏").setSampleAudioId(51L),
                new AiDramaCharacterDO().setId(2L).setProjectId(PROJECT_ID).setName("周远").setSampleAudioId(52L)));
        when(assetService.getVisualAssetList(Arrays.asList(41L, 42L))).thenReturn(Arrays.asList(
                prop(41L, "车票", "https://files.example.com/p.png"), prop(42L, "月票", "https://files.example.com/p.png")));
        when(assetService.getAudioList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaAudioDO().setId(51L).setProjectId(PROJECT_ID).setCharacterId(1L)
                        .setType(AiDramaAudioTypeEnum.SAMPLE.getType()).setFileUrl("https://files.example.com/v.mp3"),
                new AiDramaAudioDO().setId(52L).setProjectId(PROJECT_ID).setCharacterId(2L)
                        .setType(AiDramaAudioTypeEnum.SAMPLE.getType()).setFileUrl("https://files.example.com/v.mp3")));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：相同地址只带一次，但对应的道具、角色都保留在说明中
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        AiVideoDO video = videoCaptor.getValue();
        assertEquals(Arrays.asList("https://files.example.com/p.png"), video.getInputUrls().get("reference_image"));
        assertEquals(Arrays.asList("https://files.example.com/v.mp3"), video.getInputUrls().get("reference_audio"));
        assertTrue(video.getPrompt().endsWith("参考素材：参考图1为道具车票、道具月票；参考音频1为林夏的音色、周远的音色"));
    }

    @Test
    public void testGenerateStoryboardVideoMy_resolutionEmpty() {
        mockSubmit();
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));
        AiDramaStoryboardVideoReqVO reqVO = videoReqVO(STORYBOARD_ID);
        reqVO.setResolution("");

        storyboardService.generateStoryboardVideoMy(USER_ID, reqVO);

        // 断言：空串与不传一致，不传给模型；记为空串
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        assertNull(videoCaptor.getValue().getResolution());
        ArgumentCaptor<AiDramaStoryboardDO> pendingCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateById(pendingCaptor.capture());
        assertEquals("", pendingCaptor.getValue().getVideoResolution());
    }

    @Test
    public void testGenerateStoryboardVideoMy_referenceAudioNotSupported() {
        mockSubmit();
        mockReferenceAssets();
        when(videoService.buildVideoSubmitter(any(), any()))
                .thenThrow(exception(VIDEO_PARAMS_INVALID, "只有 Seedance 2.0 / 2.5 系列支持参考视频、参考音频"))
                .thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：不支持参考音频时，只带参考图
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService, times(2)).buildVideoSubmitter(videoCaptor.capture(), any());
        AiVideoDO video = videoCaptor.getAllValues().get(1);
        assertEquals(AiVideoModeEnum.REFERENCE.getMode(), video.getMode());
        assertEquals(3, video.getInputUrls().get("reference_image").size());
        assertNull(video.getInputUrls().get("reference_audio"));
        assertTrue(video.getPrompt().endsWith("参考图3为道具车票"));
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                any(), argThat(o -> "task-1".equals(o.getTaskId())));
    }

    @Test
    public void testGenerateStoryboardVideoMy_referenceNotSupported() {
        mockSubmit();
        mockReferenceAssets();
        when(videoService.buildVideoSubmitter(any(), any()))
                .thenThrow(exception(VIDEO_PARAMS_INVALID, "模型不支持参考图"))
                .thenThrow(exception(VIDEO_PARAMS_INVALID, "模型不支持参考图"))
                .thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：参考素材都不支持时，与原来一样按文生视频提交
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService, times(3)).buildVideoSubmitter(videoCaptor.capture(), any());
        AiVideoDO video = videoCaptor.getAllValues().get(2);
        assertEquals(AiVideoModeEnum.TEXT.getMode(), video.getMode());
        assertNull(video.getInputUrls());
        assertFalse(video.getPrompt().contains("参考素材"));
    }

    @Test
    public void testGenerateStoryboardVideoMy_firstFrame() {
        mockSubmit();
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L)
                .setContinuity(new AiDramaStoryboardDO.Continuity().setManualStartFrame(
                        new AiDramaStoryboardDO.ManualStartFrame().setUrl("https://files.example.com/f.png"))));
        when(assetService.getAppearanceList(Arrays.asList(11L))).thenReturn(Collections.singletonList(
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L).setSelectedIndex(0)
                        .setImageUrls(Arrays.asList("https://files.example.com/a.png"))));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：有起始帧时为图生视频，不带参考素材
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        assertEquals(AiVideoModeEnum.IMAGE.getMode(), videoCaptor.getValue().getMode());
        assertEquals(Arrays.asList("first_frame"), new ArrayList<>(videoCaptor.getValue().getInputUrls().keySet()));
        // 断言：提交记录记下本次提交的首帧，供查询时使用
        ArgumentCaptor<AiDramaVideoSubmissionDO> submissionCaptor = ArgumentCaptor.forClass(AiDramaVideoSubmissionDO.class);
        verify(videoSubmissionMapper).insert(submissionCaptor.capture());
        assertEquals("https://files.example.com/f.png", submissionCaptor.getValue().getFirstFrameUrl());
    }

    @Test
    public void testGenerateStoryboardVideoMy_multiShot() {
        mockSubmit();
        mockReferenceAssets();
        // 准备：连续多镜段，有手动起始帧；林夏的形象图使用镜像地址提交
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setDialogues(Collections.singletonList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了")))
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L).setVisualAssetIds(Arrays.asList(41L))
                .setContinuity(new AiDramaStoryboardDO.Continuity().setVideoReferenceMode("multi_shot")
                        .setManualStartFrame(new AiDramaStoryboardDO.ManualStartFrame().setUrl("https://files.example.com/f.png"))
                        .setVideoReferenceMirrors(ImmutableMap.of("https://files.example.com/a.png", "https://mirror.example.com/a.png"))));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：起始帧作为第 1 张参考图，与角色形象、场景、道具一起为参考生视频，不带首帧；镜像只替换提交的地址
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        AiVideoDO video = videoCaptor.getValue();
        assertEquals(AiVideoModeEnum.REFERENCE.getMode(), video.getMode());
        assertNull(video.getInputUrls().get("first_frame"));
        assertEquals(Arrays.asList("https://files.example.com/f.png", "https://mirror.example.com/a.png",
                        "https://files.example.com/l.png", "https://files.example.com/p.png"),
                video.getInputUrls().get("reference_image"));
        assertTrue(video.getPrompt().contains("参考素材：参考图1为起始画面；参考图2为林夏的形象；参考图3为场景车厢"));
    }

    @Test
    public void testGenerateStoryboardVideoMy_multiShotReferenceNotSupported() {
        mockSubmit();
        mockReferenceAssets();
        // 准备：连续多镜段，没有起始帧，林夏有选用的形象图；视频模型不支持参考素材
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setDialogues(Collections.singletonList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了")))
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L).setVisualAssetIds(Arrays.asList(41L))
                .setContinuity(new AiDramaStoryboardDO.Continuity().setVideoReferenceMode("multi_shot")));
        when(videoService.buildVideoSubmitter(any(), any())).thenThrow(exception(VIDEO_PARAMS_INVALID, "模型不支持参考图"));

        // 断言：不丢掉角色参考退回文生视频，直接拒绝，不提交
        ServiceException e = assertThrows(ServiceException.class,
                () -> storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID)));
        assertEquals("视频模型不支持参考图，不能生成连续多镜段", e.getMessage());
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService, atLeastOnce()).buildVideoSubmitter(videoCaptor.capture(), any());
        videoCaptor.getAllValues().forEach(video -> assertEquals(AiVideoModeEnum.REFERENCE.getMode(), video.getMode()));
        verify(videoSubmissionMapper, never()).insert(any(AiDramaVideoSubmissionDO.class));
    }

    @Test
    public void testGenerateStoryboardVideoMy_multiShotCharacterImageMissing() {
        mockSubmit();
        // 准备：连续多镜段，林夏的形象没有选用图
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L)
                .setContinuity(new AiDramaStoryboardDO.Continuity().setVideoReferenceMode("multi_shot")));

        // 断言：缺少角色参考时不提交
        ServiceException e = assertThrows(ServiceException.class,
                () -> storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID)));
        assertEquals("连续多镜段缺少角色参考：林夏", e.getMessage());
        verify(videoService, never()).buildVideoSubmitter(any(), any());
    }

    @Test
    public void testGenerateStoryboardVideoMy_mirrorExpired() {
        mockSubmit();
        // 准备：有起始帧（图生视频），镜像的原图不在本镜参考图中
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L)
                .setContinuity(new AiDramaStoryboardDO.Continuity()
                        .setManualStartFrame(new AiDramaStoryboardDO.ManualStartFrame().setUrl("https://files.example.com/f.png"))
                        .setVideoReferenceMirrors(ImmutableMap.of("https://files.example.com/old.png", "https://mirror.example.com/old.png"))));

        // 断言：镜像已过期时不提交
        ServiceException e = assertThrows(ServiceException.class,
                () -> storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID)));
        assertEquals("视频参考镜像已过期：原图不在本镜参考图中", e.getMessage());
        verify(videoService, never()).buildVideoSubmitter(any(), any());
    }

    @Test
    public void testGenerateStoryboardVideoMy_staleFrame() {
        mockSubmit();
        // 准备：关键帧出图成功，但输入摘要与镜头当前的输入不一致（镜头或参考图已修改）
        when(assetService.getStoryboardFrame(STORYBOARD_ID)).thenReturn(new AiDramaVisualAssetDO().setId(61L)
                .setStoryboardId(STORYBOARD_ID).setStatus(AiDramaStatusEnum.SUCCESS.getStatus())
                .setImageUrl("https://files.example.com/k.png").setSourceHash("stale"));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("seedance", "m", "task-1", null, null));

        storyboardService.generateStoryboardVideoMy(USER_ID, videoReqVO(STORYBOARD_ID));

        // 断言：过期的关键帧不作为起始帧
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        assertNotEquals(AiVideoModeEnum.IMAGE.getMode(), videoCaptor.getValue().getMode());
        assertTrue(videoCaptor.getValue().getInputUrls() == null
                || !videoCaptor.getValue().getInputUrls().containsKey("first_frame"));
        // 断言：提交记录没有首帧，查询时也不会用这张过期的关键帧
        ArgumentCaptor<AiDramaVideoSubmissionDO> submissionCaptor = ArgumentCaptor.forClass(AiDramaVideoSubmissionDO.class);
        verify(videoSubmissionMapper).insert(submissionCaptor.capture());
        assertNull(submissionCaptor.getValue().getFirstFrameUrl());
    }

    @Test
    public void testGenerateStoryboardVideoBatchMy_paramsInvalid() {
        AiModelDO model = videoModel();
        when(videoService.validateVideoModel(VIDEO_MODEL_ID)).thenReturn(model);
        when(videoService.validateVideoApiKey(model)).thenReturn(apiKey());
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
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
        assertEquals(Arrays.asList(1L, 3L), captor.getAllValues().stream().map(AiDramaStoryboardDO::getId).collect(Collectors.toList()));
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getVideoStatus());
        assertEquals("视频参数不正确：时长只支持 5、10 秒", captor.getValue().getErrorMessage());
        assertEquals("", captor.getValue().getVideoResolution());
    }

    @Test
    public void testResubmitStoryboardVideo_success() {
        mockSubmit();
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard().setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L)
                .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setVideoModelId(VIDEO_MODEL_ID).setVideoResolution("768P"));
        when(videoService.buildVideoSubmitter(any(), any())).thenReturn(() -> new AiVideoTaskRef("minimax-v2", "m", "task-1", null, null));

        storyboardService.resubmitStoryboardVideo(STORYBOARD_ID);

        // 断言：沿用上次的视频模型、分辨率
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).buildVideoSubmitter(videoCaptor.capture(), any());
        assertEquals(VIDEO_MODEL_ID, videoCaptor.getValue().getModelId());
        assertEquals("768P", videoCaptor.getValue().getResolution());
        ArgumentCaptor<AiDramaStoryboardDO> pendingCaptor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateById(pendingCaptor.capture());
        assertEquals(AiDramaStatusEnum.PENDING.getStatus(), pendingCaptor.getValue().getVideoStatus());
        assertEquals("768P", pendingCaptor.getValue().getVideoResolution());
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
        when(projectService.getProject(PROJECT_ID)).thenReturn(project());
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
        // 断言：没有截到新尾帧时置为空串，清除上一个视频的尾帧
        assertEquals("", captor.getValue().getLastFrameUrl());
    }

    @Test
    public void testSyncStoryboardVideo_multiShotQueryWithoutFirstFrame() {
        // 准备：连续多镜段有手动起始帧，提交时作为参考图
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-1").setVideoModelId(VIDEO_MODEL_ID)
                .setContinuity(new AiDramaStoryboardDO.Continuity().setVideoReferenceMode("multi_shot")
                        .setManualStartFrame(new AiDramaStoryboardDO.ManualStartFrame().setUrl("https://files.example.com/f.png")));
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));
        when(projectService.getProject(PROJECT_ID)).thenReturn(project());
        when(modelService.getModel(VIDEO_MODEL_ID)).thenReturn(videoModel());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey());
        AiVideoTaskResult result = mock(AiVideoTaskResult.class);
        when(result.isFinished()).thenReturn(false);
        when(videoService.getVideoTask(any(), any())).thenReturn(result);

        storyboardService.syncStoryboardVideo();

        // 断言：查询时与提交一致，没有首帧（可灵等平台的查询路径依赖是否有首帧）
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).getVideoTask(videoCaptor.capture(), any());
        assertNull(videoCaptor.getValue().getInputUrls());
    }

    @Test
    public void testSyncStoryboardVideo_submittedFirstFrame() {
        // 准备：按图生视频提交；提交后修改了角色、场景、道具的选图，当前关键帧已不是可用的起始帧
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-1").setVideoModelId(VIDEO_MODEL_ID);
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));
        when(projectService.getProject(PROJECT_ID)).thenReturn(project());
        when(modelService.getModel(VIDEO_MODEL_ID)).thenReturn(videoModel());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey());
        when(videoSubmissionMapper.selectByStoryboardIdAndTaskId(STORYBOARD_ID, "task-1")).thenReturn(unknownSubmission()
                .setTaskId("task-1").setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setFirstFrameUrl("https://files.example.com/k.png"));
        AiVideoTaskResult result = mock(AiVideoTaskResult.class);
        when(result.isFinished()).thenReturn(false);
        when(videoService.getVideoTask(any(), any())).thenReturn(result);

        storyboardService.syncStoryboardVideo();

        // 断言：按提交记录的首帧查询（仍为图生视频），不按当前的关键帧重新推导
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).getVideoTask(videoCaptor.capture(), any());
        assertEquals(Arrays.asList("https://files.example.com/k.png"), videoCaptor.getValue().getInputUrls().get("first_frame"));
        verify(assetService, never()).getStoryboardFrame(any());
    }

    @Test
    public void testSyncStoryboardVideo_submittedWithoutFirstFrame() {
        // 准备：按参考生视频（没有首帧）提交；提交后才设置了手动起始帧
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-1").setVideoModelId(VIDEO_MODEL_ID)
                .setContinuity(new AiDramaStoryboardDO.Continuity()
                        .setManualStartFrame(new AiDramaStoryboardDO.ManualStartFrame().setUrl("https://files.example.com/f.png")));
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));
        when(projectService.getProject(PROJECT_ID)).thenReturn(project());
        when(modelService.getModel(VIDEO_MODEL_ID)).thenReturn(videoModel());
        when(apiKeyService.getApiKey(API_KEY_ID)).thenReturn(apiKey());
        when(videoSubmissionMapper.selectByStoryboardIdAndTaskId(STORYBOARD_ID, "task-1")).thenReturn(unknownSubmission()
                .setTaskId("task-1").setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
        AiVideoTaskResult result = mock(AiVideoTaskResult.class);
        when(result.isFinished()).thenReturn(false);
        when(videoService.getVideoTask(any(), any())).thenReturn(result);

        storyboardService.syncStoryboardVideo();

        // 断言：与本次提交一致，查询时没有首帧，不使用提交后设置的起始帧
        ArgumentCaptor<AiVideoDO> videoCaptor = ArgumentCaptor.forClass(AiVideoDO.class);
        verify(videoService).getVideoTask(videoCaptor.capture(), any());
        assertNull(videoCaptor.getValue().getInputUrls());
    }

    @Test
    public void testSyncStoryboardVideo_resubmittedDuringQuery() {
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                .setTaskId("task-old").setVideoModelId(VIDEO_MODEL_ID);
        when(storyboardMapper.selectListByVideoStatus(any(), anyInt())).thenReturn(Collections.singletonList(storyboard));
        when(projectService.getProject(PROJECT_ID)).thenReturn(project());
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
        when(projectService.getProject(PROJECT_ID)).thenReturn(project());

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
        when(storyboardMapper.selectListByVideoStatusAndUpdateTimeLt(any(), any())).thenReturn(Arrays.asList(
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

    // ========== 视频提交记录 ==========

    @Test
    public void testResolveVideoSubmissionMy_success() {
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission());
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setTaskId("req-00000001"));
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.FAIL.getStatus()),
                eq("req-00000001"), any())).thenReturn(1);
        AiDramaVideoSubmissionResolveReqVO reqVO = new AiDramaVideoSubmissionResolveReqVO();
        reqVO.setId(1L);
        reqVO.setTaskId(" cgt-1 ");

        storyboardService.resolveVideoSubmissionMy(USER_ID, reqVO);

        // 断言：分镜改为生成中，记下任务编号与提交时的模型；提交记录一并更新
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.FAIL.getStatus()),
                eq("req-00000001"), captor.capture());
        assertEquals("cgt-1", captor.getValue().getTaskId());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), captor.getValue().getVideoStatus());
        assertEquals(VIDEO_MODEL_ID, captor.getValue().getVideoModelId());
        verify(videoSubmissionMapper).updateByIdAndStatus(eq(1L), eq(AiDramaStatusEnum.PENDING.getStatus()),
                argThat(updateObj -> "cgt-1".equals(updateObj.getTaskId())));
    }

    @Test
    public void testImportStoryboardVideoMy_oldSubmissionCannotResolve() {
        // 准备：结果未知的提交失败后，镜头的任务编号仍为该次请求编号
        AiDramaStoryboardDO storyboard = storyboard().setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setTaskId("req-00000001");
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard);
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.FAIL.getStatus()),
                eq("req-00000001"), any())).thenReturn(1);
        AiDramaStoryboardImportVideoReqVO importReqVO = new AiDramaStoryboardImportVideoReqVO();
        importReqVO.setId(STORYBOARD_ID);
        importReqVO.setFileUrl("https://files.example.com/import.mp4");

        // 调用：导入正常的视频
        try (MockedStatic<AiFfmpegUtils> ffmpegUtils = mockStatic(AiFfmpegUtils.class)) {
            ffmpegUtils.when(() -> AiFfmpegUtils.probeVideo(any(), any()))
                    .thenReturn(new AiFfmpegUtils.VideoInfo(5, 1080, 1920, 30, "h264"));
            storyboardService.importStoryboardVideoMy(USER_ID, importReqVO);
        }

        // 断言：导入后任务编号置为空串，之前的提交不再是镜头当前的任务
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.FAIL.getStatus()),
                eq("req-00000001"), captor.capture());
        assertEquals("", captor.getValue().getTaskId());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), captor.getValue().getVideoStatus());

        // 调用：再为旧提交填写任务编号
        storyboard.setTaskId(captor.getValue().getTaskId()).setVideoStatus(captor.getValue().getVideoStatus())
                .setVideoUrl(captor.getValue().getVideoUrl());
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission());
        AiDramaVideoSubmissionResolveReqVO resolveReqVO = new AiDramaVideoSubmissionResolveReqVO();
        resolveReqVO.setId(1L);
        resolveReqVO.setTaskId("cgt-1");

        // 断言：被拒绝，不会接管镜头、覆盖导入的视频
        assertServiceException(() -> storyboardService.resolveVideoSubmissionMy(USER_ID, resolveReqVO),
                DRAMA_VIDEO_SUBMISSION_NOT_UNKNOWN);
        verify(storyboardMapper, times(1)).updateByIdAndVideoStatusAndTaskId(any(), any(), any(), any());
        verify(videoSubmissionMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

    @Test
    public void testResolveVideoSubmissionMy_notCurrent() {
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission());
        // 准备：结果未知后又提交过一次并失败，镜头当前的任务已不是这次请求
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setTaskId("req-00000002"));
        AiDramaVideoSubmissionResolveReqVO reqVO = new AiDramaVideoSubmissionResolveReqVO();
        reqVO.setId(1L);
        reqVO.setTaskId("cgt-1");

        assertServiceException(() -> storyboardService.resolveVideoSubmissionMy(USER_ID, reqVO), DRAMA_VIDEO_SUBMISSION_NOT_UNKNOWN);
        verify(storyboardMapper, never()).updateByIdAndVideoStatusAndTaskId(any(), any(), any(), any());
    }

    @Test
    public void testResolveVideoSubmissionMy_notUnknown() {
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission().setTaskId("cgt-1")
                .setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
        AiDramaVideoSubmissionResolveReqVO reqVO = new AiDramaVideoSubmissionResolveReqVO();
        reqVO.setId(1L);
        reqVO.setTaskId("cgt-2");

        assertServiceException(() -> storyboardService.resolveVideoSubmissionMy(USER_ID, reqVO), DRAMA_VIDEO_SUBMISSION_NOT_UNKNOWN);
    }

    @Test
    public void testConfirmVideoSubmissionFailMy_submitting() {
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission());
        // 分镜仍在提交本次请求（进程中断）
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setVideoStatus(AiDramaStatusEnum.PENDING.getStatus()).setTaskId("req-00000001"));

        storyboardService.confirmVideoSubmissionFailMy(USER_ID, 1L);

        // 断言：分镜、提交记录都置为已失败
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.PENDING.getStatus()),
                eq("req-00000001"), argThat(updateObj -> AiDramaStatusEnum.FAIL.getStatus().equals(updateObj.getVideoStatus())));
        verify(videoSubmissionMapper).updateByIdAndStatus(eq(1L), eq(AiDramaStatusEnum.PENDING.getStatus()),
                argThat(updateObj -> AiDramaStatusEnum.FAIL.getStatus().equals(updateObj.getStatus())));
    }

    @Test
    public void testUseVideoSubmissionMy_previous() {
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission().setStatus(AiDramaStatusEnum.FAIL.getStatus())
                .setPreviousVideo(new AiDramaVideoSubmissionDO.VideoSnapshot().setTaskId("task-old")
                        .setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()).setVideoUrl("https://files.example.com/old.mp4")));
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setTaskId("task-new"));
        when(storyboardMapper.updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), any(), any(), any())).thenReturn(1);

        storyboardService.useVideoSubmissionMy(USER_ID, 1L, true);

        // 断言：恢复为提交前的视频，以读取到的状态、任务编号为条件
        ArgumentCaptor<AiDramaStoryboardDO> captor = ArgumentCaptor.forClass(AiDramaStoryboardDO.class);
        verify(storyboardMapper).updateByIdAndVideoStatusAndTaskId(eq(STORYBOARD_ID), eq(AiDramaStatusEnum.FAIL.getStatus()),
                eq("task-new"), captor.capture());
        assertEquals("https://files.example.com/old.mp4", captor.getValue().getVideoUrl());
        assertEquals("task-old", captor.getValue().getTaskId());
        assertEquals("", captor.getValue().getLastFrameUrl());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), captor.getValue().getVideoStatus());
    }

    @Test
    public void testUseVideoSubmissionMy_noVideo() {
        when(videoSubmissionMapper.selectById(1L)).thenReturn(unknownSubmission());
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard());

        assertServiceException(() -> storyboardService.useVideoSubmissionMy(USER_ID, 1L, false), DRAMA_VIDEO_SUBMISSION_NO_VIDEO);
    }

    // ========== 摄影规则 ==========

    @Test
    public void testBuildPhotographyText() {
        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("colorTone", "冷蓝");
        rules.put("lighting", ImmutableMap.of("direction", "侧逆光"));
        rules.put("characters", Arrays.asList(ImmutableMap.of("name", "林夏")));
        rules.put("sceneSummary", "不写入");

        assertEquals("光线：侧逆光；人物：林夏；色调：冷蓝", AiDramaStoryboardServiceImpl.buildPhotographyText(rules));
        assertNull(AiDramaStoryboardServiceImpl.buildPhotographyText(null));
    }

    // ========== 工具 ==========

    private static AiDramaVideoSubmissionDO unknownSubmission() {
        return new AiDramaVideoSubmissionDO().setId(1L).setProjectId(PROJECT_ID).setStoryboardId(STORYBOARD_ID)
                .setRequestId("req-00000001").setModelId(VIDEO_MODEL_ID).setModel("doubao-seedance-1-0-pro")
                .setStatus(AiDramaStatusEnum.PENDING.getStatus());
    }

    private void mockSubmit() {
        AiDramaStoryboardDO storyboard = storyboard()
                .setDialogues(Collections.singletonList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了")))
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L);
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard);
        AiModelDO model = videoModel();
        when(videoService.validateVideoModel(VIDEO_MODEL_ID)).thenReturn(model);
        when(videoService.validateVideoApiKey(model)).thenReturn(apiKey());
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(project());
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Collections.singletonList(
                new AiDramaCharacterDO().setId(1L).setProjectId(PROJECT_ID).setName("林夏").setDescription("短发")));
        when(assetService.getLocation(31L)).thenReturn(new AiDramaLocationDO().setId(31L).setProjectId(PROJECT_ID)
                .setName("车厢").setDescription("深夜"));
        when(assetService.getAppearanceList(Arrays.asList(11L))).thenReturn(Collections.singletonList(
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L)));
        when(assetService.buildStyleText(any())).thenReturn("电影感");
    }

    /**
     * 参考素材：在 {@link #mockSubmit()} 的基础上，形象、场景有选用图，带一个道具，发言角色有选用样音
     */
    private void mockReferenceAssets() {
        when(storyboardMapper.selectById(STORYBOARD_ID)).thenReturn(storyboard()
                .setDialogues(Collections.singletonList(new AiDramaStoryboardDO.Dialogue().setCharacterId(1L).setLine("最后一班了")))
                .setCharacters(Arrays.asList(characterRef(11L))).setLocationId(31L).setVisualAssetIds(Arrays.asList(41L)));
        when(assetService.getAppearanceList(Arrays.asList(11L))).thenReturn(Collections.singletonList(
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L).setSelectedIndex(0)
                        .setImageUrls(Arrays.asList("https://files.example.com/a.png"))));
        when(assetService.getLocation(31L)).thenReturn(new AiDramaLocationDO().setId(31L).setProjectId(PROJECT_ID)
                .setName("车厢").setDescription("深夜").setSelectedIndex(0)
                .setImageUrls(Arrays.asList("https://files.example.com/l.png")));
        when(assetService.getVisualAssetList(Arrays.asList(41L))).thenReturn(Collections.singletonList(
                prop(41L, "车票", "https://files.example.com/p.png")));
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Collections.singletonList(
                new AiDramaCharacterDO().setId(1L).setProjectId(PROJECT_ID).setName("林夏").setDescription("短发")
                        .setSampleAudioId(52L)));
        when(assetService.getAudioList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaAudioDO().setId(51L).setProjectId(PROJECT_ID).setCharacterId(1L)
                        .setType(AiDramaAudioTypeEnum.SAMPLE.getType()).setFileUrl("https://files.example.com/s.mp3"),
                new AiDramaAudioDO().setId(52L).setProjectId(PROJECT_ID).setCharacterId(1L)
                        .setType(AiDramaAudioTypeEnum.SAMPLE.getType()).setFileUrl("https://files.example.com/v.mp3")));
    }

    private static AiDramaStoryboardDO storyboard() {
        return new AiDramaStoryboardDO().setId(STORYBOARD_ID).setProjectId(PROJECT_ID).setSceneNo(1).setTitle("进站")
                .setDuration(5).setVideoPrompt("列车进站");
    }

    private static AiDramaProjectDO project() {
        return new AiDramaProjectDO().setId(PROJECT_ID).setUserId(USER_ID).setComposeAspectRatio("9:16")
                .setStatus(AiDramaProjectStatusEnum.STORYBOARD_READY.getStatus());
    }

    private static AiDramaStoryboardDO.CharacterRef characterRef(Long appearanceId) {
        return new AiDramaStoryboardDO.CharacterRef().setAppearanceId(appearanceId);
    }

    private static AiDramaVisualAssetDO prop(Long id, String title, String imageUrl) {
        return new AiDramaVisualAssetDO().setId(id).setProjectId(PROJECT_ID).setKind(AiDramaVisualAssetKindEnum.PROP.getType())
                .setTitle(title).setImageUrl(imageUrl);
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
