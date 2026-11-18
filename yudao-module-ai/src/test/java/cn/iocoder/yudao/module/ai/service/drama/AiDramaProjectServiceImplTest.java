package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.AiDramaComposeReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.AiDramaProjectUpdatePublicReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaAudioDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaProjectDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaScriptDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaProjectMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils.ComposeRequest;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils.ComposeResult;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiDramaProjectServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiDramaProjectServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long PROJECT_ID = 100L;

    @Spy
    @InjectMocks
    private AiDramaProjectServiceImpl projectService;

    @Mock
    private AiDramaProjectMapper projectMapper;
    @Mock
    private AiDramaScriptService scriptService;
    @Mock
    private AiDramaAssetService assetService;
    @Mock
    private AiDramaStoryboardService storyboardService;
    @Mock
    private AiDramaCanvasService canvasService;
    @Mock
    private FileApi fileApi;

    private MockedStatic<AiFfmpegUtils> ffmpegUtilsMock;

    @BeforeEach
    public void setUp() {
        ffmpegUtilsMock = mockStatic(AiFfmpegUtils.class);
    }

    @AfterEach
    public void tearDown() {
        ffmpegUtilsMock.close();
    }

    // ========== 归属 ==========

    @Test
    public void testValidateProjectMy_otherUser() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(OTHER_USER_ID));

        assertServiceException(() -> projectService.validateProjectMy(PROJECT_ID, USER_ID), DRAMA_PROJECT_NOT_EXISTS);
    }

    // ========== 公开 ==========

    @Test
    public void testUpdateProjectPublicStatus_success() {
        // 管理员公开：不校验作者
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(OTHER_USER_ID).setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        AiDramaProjectUpdatePublicReqVO reqVO = new AiDramaProjectUpdatePublicReqVO();
        reqVO.setId(PROJECT_ID);
        reqVO.setPublicStatus(true);

        projectService.updateProjectPublicStatus(reqVO);

        ArgumentCaptor<AiDramaProjectDO> captor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateById(captor.capture());
        assertTrue(captor.getValue().getPublicStatus());
    }

    @Test
    public void testUpdateProjectPublicStatus_notComposed() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID).setComposeStatus(AiDramaStatusEnum.FAIL.getStatus()));
        AiDramaProjectUpdatePublicReqVO reqVO = new AiDramaProjectUpdatePublicReqVO();
        reqVO.setId(PROJECT_ID);
        reqVO.setPublicStatus(true);

        assertServiceException(() -> projectService.updateProjectPublicStatus(reqVO), DRAMA_PROJECT_PUBLIC_NOT_ALLOWED);
        verify(projectMapper, never()).updateById(any(AiDramaProjectDO.class));
    }

    @Test
    public void testUpdateProjectPublicStatus_cancelWithoutCompose() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID));
        AiDramaProjectUpdatePublicReqVO reqVO = new AiDramaProjectUpdatePublicReqVO();
        reqVO.setId(PROJECT_ID);
        reqVO.setPublicStatus(false);

        projectService.updateProjectPublicStatus(reqVO);

        ArgumentCaptor<AiDramaProjectDO> captor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateById(captor.capture());
        assertFalse(captor.getValue().getPublicStatus());
    }

    @Test
    public void testGetProjectPublic_notPublic() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID)
                .setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus()).setPublicStatus(false));

        assertServiceException(() -> projectService.getProjectPublic(PROJECT_ID), DRAMA_PROJECT_NOT_EXISTS);
    }

    @Test
    public void testGetProjectPublic_success() {
        AiDramaProjectDO project = project(OTHER_USER_ID).setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus())
                .setPublicStatus(true);
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project);

        assertSame(project, projectService.getProjectPublic(PROJECT_ID));
    }

    // ========== 合成 ==========

    @Test
    public void testComposeProjectMy_running() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID)
                .setComposeStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        assertServiceException(() -> projectService.composeProjectMy(USER_ID, composeReqVO(1L, 2L)), DRAMA_TASK_RUNNING);
    }

    @Test
    public void testComposeProjectMy_storyboardNotSuccess() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID));
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard(1L, AiDramaStatusEnum.SUCCESS.getStatus()),
                storyboard(2L, AiDramaStatusEnum.IN_PROGRESS.getStatus()).setTitle("镜头 2")));

        assertServiceException(() -> projectService.composeProjectMy(USER_ID, composeReqVO(1L, 2L)),
                DRAMA_COMPOSE_STORYBOARD_NOT_SUCCESS, "镜头 2");
    }

    @Test
    public void testComposeProjectMy_otherProjectNarration() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID));
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard(1L, AiDramaStatusEnum.SUCCESS.getStatus()), storyboard(2L, AiDramaStatusEnum.SUCCESS.getStatus())));
        when(assetService.getAudio(9L)).thenReturn(new AiDramaAudioDO().setId(9L).setProjectId(999L)
                .setType(AiDramaAudioTypeEnum.NARRATION.getType()));
        AiDramaComposeReqVO reqVO = composeReqVO(1L, 2L);
        reqVO.setNarrationAudioId(9L);

        assertServiceException(() -> projectService.composeProjectMy(USER_ID, reqVO), DRAMA_AUDIO_NOT_EXISTS);
    }

    @Test
    public void testComposeProjectMy_success() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID));
        // 镜头 3：未选、视频未完成的已有镜头，也计入提交时的最大镜头编号
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard(1L, AiDramaStatusEnum.SUCCESS.getStatus()), storyboard(2L, AiDramaStatusEnum.SUCCESS.getStatus()),
                storyboard(3L, AiDramaStatusEnum.IN_PROGRESS.getStatus())));
        when(fileApi.presignGetUrlQuietly(anyString())).thenAnswer(invocation -> invocation.getArgument(0) + "?sign");
        doNothing().when(projectService).executeCompose(any(), any(), any(), any());
        // 准备参数：按提交的顺序合成
        AiDramaComposeReqVO reqVO = composeReqVO(2L, 1L);
        reqVO.setTransitionType(AiDramaTransitionTypeEnum.DISSOLVE.getType());
        reqVO.setWatermarkText(" 芋道 ");

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(projectService);
            projectService.composeProjectMy(USER_ID, reqVO);
        }

        // 断言：置为合成中，记录转场
        ArgumentCaptor<AiDramaProjectDO> updateCaptor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateById(updateCaptor.capture());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), updateCaptor.getValue().getComposeStatus());
        assertEquals(AiDramaTransitionTypeEnum.DISSOLVE.getType(), updateCaptor.getValue().getComposeTransitionType());
        assertEquals(0.5, updateCaptor.getValue().getComposeTransitionDurationSeconds());
        // 断言：合成参数，带上本次的合成任务编号
        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ComposeRequest> requestCaptor = ArgumentCaptor.forClass(ComposeRequest.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AiDramaProjectDO.ComposeVideo>> videosCaptor = ArgumentCaptor.forClass(List.class);
        verify(projectService).executeCompose(eq(PROJECT_ID), taskIdCaptor.capture(), videosCaptor.capture(), requestCaptor.capture());
        assertNotNull(taskIdCaptor.getValue());
        assertEquals(taskIdCaptor.getValue(), updateCaptor.getValue().getComposeTaskId());
        // 断言：使用的镜头视频按提交的顺序，记录原始地址（非签名地址）
        assertEquals(Arrays.asList(2L, 1L), videosCaptor.getValue().stream()
                .map(AiDramaProjectDO.ComposeVideo::getStoryboardId).toList());
        assertEquals("https://files.example.com/2.mp4", videosCaptor.getValue().get(0).getVideoUrl());
        assertEquals(3L, videosCaptor.getValue().get(0).getMaxStoryboardId());
        assertEquals(3L, videosCaptor.getValue().get(1).getMaxStoryboardId());
        ComposeRequest request = requestCaptor.getValue();
        assertEquals(Arrays.asList("https://files.example.com/2.mp4?sign", "https://files.example.com/1.mp4?sign"),
                request.getVideoUrls());
        assertEquals("9:16", request.getAspectRatio());
        assertEquals("芋道", request.getWatermarkText());
        assertNull(request.getNarrationUrl());
    }

    @Test
    public void testExecuteCompose_success() throws Exception {
        ffmpegUtilsMock.when(() -> AiFfmpegUtils.compose(any(), any(), any(), any())).thenReturn(new ComposeResult(new byte[]{1}, 12.5));
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://files.example.com/drama.mp4");

        List<AiDramaProjectDO.ComposeVideo> videos = List.of(composeVideo(1L), composeVideo(2L));
        projectService.executeCompose(PROJECT_ID, "task-1", videos, composeRequest());

        // 断言：以合成中、本次任务编号为条件；记录成片实际使用的画幅、镜头数、镜头视频
        ArgumentCaptor<AiDramaProjectDO> captor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateByIdAndComposeStatusAndTaskId(eq(PROJECT_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), captor.getValue().getComposeStatus());
        assertEquals("https://files.example.com/drama.mp4", captor.getValue().getComposeFileUrl());
        assertEquals(12.5, captor.getValue().getComposeDuration());
        assertEquals("16:9", captor.getValue().getComposeAspectRatio());
        assertEquals(2, captor.getValue().getComposeStoryboardCount());
        assertEquals(videos, captor.getValue().getComposeVideos());
    }

    @Test
    public void testGetComposeStaleReason_notStale() {
        // 准备：成片使用镜头 1、2；镜头 2 只改了文字；镜头 3 是合成时未选的已有镜头
        AiDramaProjectDO project = composedProject(composeVideo(1L), composeVideo(2L));
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(composedStoryboard(1L),
                composedStoryboard(2L).setTitle("改过的标题").setVideoPrompt("改过的视频提示词"),
                (AiDramaStoryboardDO) composedStoryboard(3L).setCreateTime(project.getComposeFinishTime().minusMinutes(1)));

        assertNull(projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_noRecord() {
        // 准备：之前合成的成片，没有记录使用的镜头视频，不判断
        AiDramaProjectDO project = composedProject().setComposeVideos(null);

        assertNull(projectService.getComposeStaleReason(project, List.of(composedStoryboard(1L).setVideoUrl("https://files.example.com/x.mp4")), 1L));
    }

    @Test
    public void testGetComposeStaleReason_videoReplaced() {
        // 准备：镜头 2 的视频重新生成成功，或换用、恢复了历史视频
        AiDramaProjectDO project = composedProject(composeVideo(1L), composeVideo(2L));
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(composedStoryboard(1L),
                composedStoryboard(2L).setVideoUrl("https://files.example.com/2-new.mp4"));

        assertEquals("成片使用的镜头视频已更换", projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_regenerating() {
        // 准备：镜头 2 重新生成中，镜头视频仍是原视频
        AiDramaProjectDO project = composedProject(composeVideo(1L), composeVideo(2L));
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(composedStoryboard(1L),
                composedStoryboard(2L).setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        assertNull(projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_deleted() {
        AiDramaProjectDO project = composedProject(composeVideo(1L), composeVideo(2L));

        assertEquals("成片使用的镜头已删除", projectService.getComposeStaleReason(project, List.of(composedStoryboard(1L)), 1L));
    }

    @Test
    public void testGetComposeStaleReason_added() {
        // 准备：之前合成的快照，没有记录最大镜头编号；成片完成后新增镜头 3，且视频已完成
        AiDramaProjectDO project = composedProject(composeVideo(1L), composeVideo(2L));
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(composedStoryboard(1L), composedStoryboard(2L),
                (AiDramaStoryboardDO) composedStoryboard(3L).setCreateTime(project.getComposeFinishTime().plusMinutes(1)));

        assertEquals("成片合成后新增了镜头", projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_addedDuringCompose() {
        // 准备：提交时最大镜头编号 2，10:00 完成；镜头 3 在合成期间（9:40）新增，之后视频完成
        AiDramaProjectDO project = composedProject(composeVideo(1L).setMaxStoryboardId(2L),
                composeVideo(2L).setMaxStoryboardId(2L));
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(composedStoryboard(1L), composedStoryboard(2L),
                (AiDramaStoryboardDO) composedStoryboard(3L).setCreateTime(LocalDateTime.of(2026, 11, 17, 9, 40)));

        assertEquals("成片合成后新增了镜头", projectService.getComposeStaleReason(project, storyboards, 1L));
        // 视频未完成时不算
        storyboards.get(2).setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus());
        assertNull(projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_addedSameSecond() {
        // 准备：镜头 3 与提交合成在同一秒新增（创建时间秒级，与快照时间无法区分），按编号判断为新增
        AiDramaProjectDO project = composedProject(composeVideo(1L).setMaxStoryboardId(2L),
                composeVideo(2L).setMaxStoryboardId(2L));
        LocalDateTime sameSecond = LocalDateTime.of(2026, 11, 17, 9, 30);
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(
                (AiDramaStoryboardDO) composedStoryboard(1L).setCreateTime(sameSecond),
                (AiDramaStoryboardDO) composedStoryboard(2L).setCreateTime(sameSecond),
                (AiDramaStoryboardDO) composedStoryboard(3L).setCreateTime(sameSecond));

        assertEquals("成片合成后新增了镜头", projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_subsetAndReorder() {
        // 准备：画布按 2、1 的顺序只合成了两镜，提交前已有的镜头 3（提交时未完成，之后视频完成）未选
        AiDramaProjectDO project = composedProject(composeVideo(2L).setMaxStoryboardId(3L),
                composeVideo(1L).setMaxStoryboardId(3L));
        List<AiDramaStoryboardDO> storyboards = Arrays.asList(composedStoryboard(1L), composedStoryboard(2L),
                composedStoryboard(3L));

        assertNull(projectService.getComposeStaleReason(project, storyboards, 1L));
    }

    @Test
    public void testGetComposeStaleReason_scriptStoryboardsUpdated() {
        // 准备：成片使用剧本 1 的镜头；当前剧本 2
        AiDramaProjectDO project = composedProject(composeVideo(1L), composeVideo(2L));
        List<AiDramaStoryboardDO> storyboards = new ArrayList<>(Arrays.asList(composedStoryboard(1L), composedStoryboard(2L)));
        // 只新增了剧本版本、尚无分镜视频：不算
        storyboards.add(composedStoryboard(3L).setScriptId(2L).setVideoStatus(null).setVideoUrl(null));
        assertNull(projectService.getComposeStaleReason(project, storyboards, 2L));
        // 当前剧本版本的分镜已有完成视频
        storyboards.add(composedStoryboard(4L).setScriptId(2L));
        assertEquals("剧本的分镜已更新", projectService.getComposeStaleReason(project, storyboards, 2L));
    }

    private static AiDramaProjectDO composedProject(AiDramaProjectDO.ComposeVideo... videos) {
        return project(USER_ID).setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus())
                .setComposeFileUrl("https://files.example.com/drama.mp4")
                .setComposeFinishTime(LocalDateTime.of(2026, 11, 17, 10, 0)).setComposeVideos(Arrays.asList(videos));
    }

    private static AiDramaStoryboardDO composedStoryboard(Long id) {
        AiDramaStoryboardDO storyboard = storyboard(id, AiDramaStatusEnum.SUCCESS.getStatus()).setScriptId(1L);
        storyboard.setCreateTime(LocalDateTime.of(2026, 11, 17, 9, 0));
        return storyboard;
    }

    private static AiDramaProjectDO.ComposeVideo composeVideo(Long storyboardId) {
        return new AiDramaProjectDO.ComposeVideo().setStoryboardId(storyboardId)
                .setVideoUrl("https://files.example.com/" + storyboardId + ".mp4");
    }

    @Test
    public void testExecuteCompose_fail() throws Exception {
        ffmpegUtilsMock.when(() -> AiFfmpegUtils.compose(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("FFmpeg 执行失败：No such filter"));

        projectService.executeCompose(PROJECT_ID, "task-1", null, composeRequest());

        ArgumentCaptor<AiDramaProjectDO> captor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateByIdAndComposeStatusAndTaskId(eq(PROJECT_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getComposeStatus());
        assertEquals("FFmpeg 执行失败：No such filter", captor.getValue().getComposeErrorMessage());
        assertNull(captor.getValue().getComposeAspectRatio());
        assertNull(captor.getValue().getComposeStoryboardCount());
        verify(fileApi, never()).createFile(any(byte[].class));
    }

    @Test
    public void testExecuteCompose_resubmittedDuringUpload() throws Exception {
        // 准备：库中的项目，旧合成（task-old）合成中
        AtomicReference<Integer> dbStatus = new AtomicReference<>(AiDramaStatusEnum.IN_PROGRESS.getStatus());
        AtomicReference<String> dbTaskId = new AtomicReference<>("task-old");
        List<AiDramaProjectDO> written = new ArrayList<>();
        when(projectMapper.updateByIdAndComposeStatusAndTaskId(eq(PROJECT_ID), any(), any(), any())).thenAnswer(invocation -> {
            if (!Objects.equals(invocation.getArgument(1), dbStatus.get())
                    || !Objects.equals(invocation.getArgument(2), dbTaskId.get())) {
                return 0;
            }
            AiDramaProjectDO updateObj = invocation.getArgument(3);
            written.add(updateObj);
            dbStatus.set(updateObj.getComposeStatus());
            return 1;
        });
        ffmpegUtilsMock.when(() -> AiFfmpegUtils.compose(any(), any(), any(), any())).thenReturn(new ComposeResult(new byte[]{1}, 12.5));
        // 准备：旧合成上传成片期间，被管理员标记失败并重新合成，任务编号换成 task-new
        when(fileApi.createFile(any(byte[].class))).thenAnswer(invocation -> {
            if (Objects.equals(dbTaskId.get(), "task-old")) {
                dbStatus.set(AiDramaStatusEnum.FAIL.getStatus());
                dbStatus.set(AiDramaStatusEnum.IN_PROGRESS.getStatus());
                dbTaskId.set("task-new");
                return "https://files.example.com/old.mp4";
            }
            return "https://files.example.com/new.mp4";
        });

        // 调用：旧合成上传完成后回写，不命中，新合成仍合成中
        projectService.executeCompose(PROJECT_ID, "task-old", null, composeRequest());
        assertTrue(written.isEmpty());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), dbStatus.get());
        assertEquals("task-new", dbTaskId.get());
        // 调用：新合成结束，正常写入
        projectService.executeCompose(PROJECT_ID, "task-new", null, composeRequest());
        assertEquals(1, written.size());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), written.get(0).getComposeStatus());
        assertEquals("https://files.example.com/new.mp4", written.get(0).getComposeFileUrl());
    }

    @Test
    public void testRecomposeProject_notEnoughStoryboards() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID).setComposeStatus(AiDramaStatusEnum.FAIL.getStatus()));
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard(1L, AiDramaStatusEnum.SUCCESS.getStatus()), storyboard(2L, AiDramaStatusEnum.FAIL.getStatus())));

        assertServiceException(() -> projectService.recomposeProject(PROJECT_ID),
                DRAMA_COMPOSE_STORYBOARD_NOT_SUCCESS, "已完成的镜头不足 2 个");
    }

    @Test
    public void testRecomposeProject_otherScriptVersion() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID).setComposeStatus(AiDramaStatusEnum.FAIL.getStatus()));
        when(scriptService.getScriptByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(2L).setProjectId(PROJECT_ID));
        // 准备：2 个已完成的镜头，其中 1 个属于历史剧本版本
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                storyboard(1L, AiDramaStatusEnum.SUCCESS.getStatus()).setScriptId(1L),
                storyboard(2L, AiDramaStatusEnum.SUCCESS.getStatus()).setScriptId(2L)));

        // 断言：只使用当前剧本版本的镜头，不足 2 个
        assertServiceException(() -> projectService.recomposeProject(PROJECT_ID),
                DRAMA_COMPOSE_STORYBOARD_NOT_SUCCESS, "已完成的镜头不足 2 个");
    }

    // ========== 生成任务 ==========

    @Test
    public void testUpdateComposeFail_notRunning() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID).setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> projectService.updateComposeFail(PROJECT_ID, "管理员标记失败"), DRAMA_TASK_NOT_RUNNING);
        verify(projectMapper, never()).updateByIdAndComposeStatusAndTaskId(any(), any(), any(), any());
    }

    @Test
    public void testUpdateComposeFail_success() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID)
                .setComposeStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()).setComposeTaskId("task-1"));

        projectService.updateComposeFail(PROJECT_ID, "管理员标记失败");

        // 断言：以读取到的状态、任务编号为条件
        ArgumentCaptor<AiDramaProjectDO> captor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateByIdAndComposeStatusAndTaskId(eq(PROJECT_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getComposeStatus());
        assertEquals("管理员标记失败", captor.getValue().getComposeErrorMessage());
    }

    @Test
    public void testTimeoutCompose() {
        when(projectMapper.selectListByComposeStatusAndUpdateTimeLt(eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()), any()))
                .thenReturn(List.of(project(USER_ID).setComposeStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                        .setComposeTaskId("task-1")));
        when(projectMapper.updateByIdAndComposeStatusAndTaskId(eq(PROJECT_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), any())).thenReturn(1);

        assertEquals(1, projectService.timeoutCompose(LocalDateTime.now(), "任务超时"));

        ArgumentCaptor<AiDramaProjectDO> captor = ArgumentCaptor.forClass(AiDramaProjectDO.class);
        verify(projectMapper).updateByIdAndComposeStatusAndTaskId(eq(PROJECT_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getComposeStatus());
        assertEquals("任务超时", captor.getValue().getComposeErrorMessage());
    }

    // ========== 删除 ==========

    @Test
    public void testDeleteProject_cascade() {
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project(USER_ID));

        projectService.deleteProject(PROJECT_ID);

        // 断言：先删除资产、分镜、剧本、画布，最后删除项目
        InOrder inOrder = inOrder(assetService, storyboardService, scriptService, canvasService, projectMapper);
        inOrder.verify(assetService).deleteAssetListByProjectId(PROJECT_ID);
        inOrder.verify(storyboardService).deleteStoryboardListByProjectId(PROJECT_ID);
        inOrder.verify(scriptService).deleteScriptByProjectId(PROJECT_ID);
        inOrder.verify(canvasService).deleteCanvasByProjectId(PROJECT_ID);
        inOrder.verify(projectMapper).deleteById(PROJECT_ID);
    }

    // ========== 工具 ==========

    private static AiDramaProjectDO project(Long userId) {
        return new AiDramaProjectDO().setId(PROJECT_ID).setUserId(userId).setName("末班地铁").setComposeAspectRatio("9:16")
                .setPublicStatus(false);
    }

    private static AiDramaStoryboardDO storyboard(Long id, Integer videoStatus) {
        return new AiDramaStoryboardDO().setId(id).setProjectId(PROJECT_ID).setVideoStatus(videoStatus)
                .setVideoUrl("https://files.example.com/" + id + ".mp4");
    }

    private static AiDramaComposeReqVO composeReqVO(Long... storyboardIds) {
        AiDramaComposeReqVO reqVO = new AiDramaComposeReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setStoryboardIds(Arrays.asList(storyboardIds));
        reqVO.setAspectRatio("9:16");
        reqVO.setTransitionType(AiDramaTransitionTypeEnum.NONE.getType());
        return reqVO;
    }

    private static ComposeRequest composeRequest() {
        return new ComposeRequest().setVideoUrls(Arrays.asList("https://files.example.com/1.mp4", "https://files.example.com/2.mp4"))
                .setAspectRatio("16:9").setTransitionType(AiDramaTransitionTypeEnum.NONE).setTransitionDuration(0.5);
    }

}
