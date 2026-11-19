package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiDramaTaskServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiDramaTaskServiceImplTest extends BaseMockitoUnitTest {

    private static final Long PROJECT_ID = 100L;

    @InjectMocks
    private AiDramaTaskServiceImpl taskService;

    @Mock
    private AiDramaProjectService projectService;
    @Mock
    private AiDramaAssetService assetService;
    @Mock
    private AiDramaStoryboardService storyboardService;

    // ========== 分页 ==========

    @Test
    public void testGetTaskPage_userWithoutProject() {
        when(projectService.getProjectListByUserId(1L)).thenReturn(Collections.emptyList());
        AiDramaTaskPageReqVO reqVO = new AiDramaTaskPageReqVO();
        reqVO.setUserId(1L);

        PageResult<AiDramaTaskRespVO> pageResult = taskService.getTaskPage(reqVO);

        assertEquals(0L, pageResult.getTotal());
        verifyNoInteractions(assetService, storyboardService);
        verify(projectService, never()).getComposeTaskPage(any(), any(), any());
    }

    @Test
    public void testGetTaskPage_allTypesMerge() {
        LocalDateTime now = LocalDateTime.now();
        // 镜头视频：一个超时的生成中、一个较早的失败
        AiDramaStoryboardDO runningStoryboard = new AiDramaStoryboardDO().setId(1L).setProjectId(PROJECT_ID)
                .setSceneNo(1).setTitle("进站").setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus());
        runningStoryboard.setUpdateTime(now.minusMinutes(90));
        AiDramaStoryboardDO failStoryboard = new AiDramaStoryboardDO().setId(2L).setProjectId(PROJECT_ID)
                .setSceneNo(2).setVideoStatus(AiDramaStatusEnum.FAIL.getStatus());
        failStoryboard.setUpdateTime(now.minusDays(1));
        when(storyboardService.getStoryboardTaskPage(any(), any(), isNull()))
                .thenReturn(new PageResult<>(Arrays.asList(runningStoryboard, failStoryboard), 2L));
        // 成片：最新
        AiDramaProjectDO composeProject = new AiDramaProjectDO().setId(PROJECT_ID)
                .setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus())
                .setComposeFileUrl("https://files.example.com/drama.mp4");
        composeProject.setUpdateTime(now);
        when(projectService.getComposeTaskPage(any(), any(), isNull()))
                .thenReturn(new PageResult<>(Arrays.asList(composeProject), 1L));
        when(assetService.getAppearanceTaskPage(any(), any(), isNull())).thenReturn(PageResult.empty());
        when(assetService.getLocationTaskPage(any(), any(), isNull())).thenReturn(PageResult.empty());
        when(assetService.getVisualAssetTaskPage(any(), any(), isNull())).thenReturn(PageResult.empty());
        when(projectService.getProjectList(any())).thenReturn(Arrays.asList(
                new AiDramaProjectDO().setId(PROJECT_ID).setUserId(1L).setName("末班地铁")));
        AiDramaTaskPageReqVO reqVO = new AiDramaTaskPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(2);

        PageResult<AiDramaTaskRespVO> pageResult = taskService.getTaskPage(reqVO);

        // 断言：总数为各类之和；按更新时间倒序取第 1 页
        assertEquals(3L, pageResult.getTotal());
        assertEquals(2, pageResult.getList().size());
        AiDramaTaskRespVO compose = pageResult.getList().get(0);
        assertEquals(AiDramaTaskTypeEnum.COMPOSE.getType(), compose.getType());
        assertEquals("https://files.example.com/drama.mp4", compose.getResultUrl());
        assertEquals("末班地铁", compose.getProjectName());
        assertEquals(1L, compose.getUserId());
        assertFalse(compose.getTimeout());
        AiDramaTaskRespVO storyboard = pageResult.getList().get(1);
        assertEquals(AiDramaTaskTypeEnum.STORYBOARD.getType(), storyboard.getType());
        assertEquals("第 1 镜 · 进站", storyboard.getTitle());
        assertTrue(storyboard.getTimeout());
        // 断言：每类取前 pageNo * pageSize 条
        verify(storyboardService).getStoryboardTaskPage(argThat(page -> page.getPageNo() == 1 && page.getPageSize() == 2),
                same(reqVO), isNull());
    }

    @Test
    public void testGetTaskPage_modelFilterAppearance() {
        AiDramaTaskPageReqVO reqVO = new AiDramaTaskPageReqVO();
        reqVO.setType(AiDramaTaskTypeEnum.APPEARANCE.getType());
        reqVO.setModelId(10L);
        when(assetService.getAppearanceTaskPage(any(), same(reqVO), isNull())).thenReturn(new PageResult<>(
                Collections.singletonList(new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L).setAppearanceIndex(0)
                        .setModelId(10L).setStatus(AiDramaStatusEnum.SUCCESS.getStatus())
                        .setImageUrls(Arrays.asList("https://files.example.com/a.png", "https://files.example.com/b.png"))
                        .setImageDescriptions(Arrays.asList("第一张", "第二张")).setSelectedIndex(0)), 1L));

        PageResult<AiDramaTaskRespVO> pageResult = taskService.getTaskPage(reqVO);

        // 断言：角色形象记录了出图模型，按模型筛选；结果为选用的候选图，提示词为最后一次的说明
        assertEquals(1L, pageResult.getTotal());
        AiDramaTaskRespVO task = pageResult.getList().get(0);
        assertEquals(10L, task.getModelId());
        assertEquals("https://files.example.com/a.png", task.getResultUrl());
        assertEquals("第二张", task.getPrompt());
    }

    // ========== 操作 ==========

    @Test
    public void testResubmitTask_composeNotFail() {
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(new AiDramaProjectDO().setId(PROJECT_ID)
                .setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> taskService.resubmitTask(actionReqVO(AiDramaTaskTypeEnum.COMPOSE, PROJECT_ID)),
                DRAMA_TASK_NOT_FAIL);
        verify(projectService, never()).recomposeProject(any());
    }

    @Test
    public void testResubmitTask_frameVideoRunning() {
        when(assetService.getVisualAsset(1L)).thenReturn(new AiDramaVisualAssetDO().setId(1L).setStoryboardId(2L));
        doThrow(exception(DRAMA_TASK_RUNNING)).when(storyboardService).validateStoryboardVideoNotRunning(2L);

        // 断言：镜头视频生成中，关键帧不能重新出图
        assertServiceException(() -> taskService.resubmitTask(actionReqVO(AiDramaTaskTypeEnum.VISUAL_ASSET, 1L)),
                DRAMA_TASK_RUNNING);
        verify(assetService, never()).resubmitVisualAssetImage(any());
    }

    @Test
    public void testResubmitTask_storyboard() {
        taskService.resubmitTask(actionReqVO(AiDramaTaskTypeEnum.STORYBOARD, 1L));

        verify(storyboardService).resubmitStoryboardVideo(1L);
    }

    @Test
    public void testSyncTask_notSupported() {
        assertServiceException(() -> taskService.syncTask(actionReqVO(AiDramaTaskTypeEnum.APPEARANCE, 1L)),
                DRAMA_TASK_SYNC_NOT_SUPPORTED);
    }

    @Test
    public void testUpdateTaskFail_storyboard() {
        taskService.updateTaskFail(actionReqVO(AiDramaTaskTypeEnum.STORYBOARD, 1L));

        verify(storyboardService).updateStoryboardVideoFail(1L, "管理员标记失败");
    }

    @Test
    public void testUpdateTaskFail_compose() {
        taskService.updateTaskFail(actionReqVO(AiDramaTaskTypeEnum.COMPOSE, PROJECT_ID));

        verify(projectService).updateComposeFail(PROJECT_ID, "管理员标记失败");
    }

    // ========== 超时 ==========

    @Test
    public void testTimeoutTasks() {
        when(storyboardService.timeoutStoryboardVideo(any(), eq("任务超时"))).thenReturn(1);
        when(assetService.timeoutImage(any(), eq("任务超时"))).thenReturn(2);
        when(projectService.timeoutCompose(any(), eq("任务超时"))).thenReturn(3);

        // 断言：各类之和
        assertEquals(6, taskService.timeoutTasks());
    }

    private static AiDramaTaskActionReqVO actionReqVO(AiDramaTaskTypeEnum type, Long id) {
        AiDramaTaskActionReqVO reqVO = new AiDramaTaskActionReqVO();
        reqVO.setType(type.getType());
        reqVO.setId(id);
        return reqVO;
    }

}
