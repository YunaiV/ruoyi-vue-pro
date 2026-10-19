package cn.iocoder.yudao.module.oa.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskFeedbackReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskLogDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskReceiverDO;
import cn.iocoder.yudao.module.oa.dal.mysql.task.OaTaskLogMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.task.OaTaskMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.task.OaTaskReceiverMapper;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskTypeEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_RECEIVED_DELETE_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_STATUS_TRANSITION_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * {@link OaTaskServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaTaskServiceImpl.class)
public class OaTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaTaskServiceImpl taskService;

    @Resource
    private OaTaskMapper taskMapper;
    @Resource
    private OaTaskReceiverMapper taskReceiverMapper;
    @Resource
    private OaTaskLogMapper taskLogMapper;

    @MockBean
    private AdminUserApi adminUserApi;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testFeedbackTask_publisherAsReceiverOnlyUpdatesSelf() {
        // mock 数据
        OaTaskDO task = buildTaskDO(10L);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(10L).setStatus(1));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(20L).setStatus(1));

        // 调用：发布人在接收人入口提交自己的进度
        taskService.feedbackTask(new OaTaskFeedbackReqVO().setTaskId(task.getId())
                .setPublisher(false).setStatus(4), 10L);
        // 断言：其他人的状态和总体最低进度不被覆盖
        assertEquals(4, taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), 10L).getStatus());
        assertEquals(1, taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), 20L).getStatus());
        assertEquals(1, taskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    public void testFeedbackTask_receiverCannotUsePublisherEntry() {
        // mock 数据
        OaTaskDO task = buildTaskDO(10L);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(20L).setStatus(1));

        // 调用，并断言异常
        assertServiceException(() -> taskService.feedbackTask(new OaTaskFeedbackReqVO()
                .setTaskId(task.getId()).setPublisher(true).setStatus(5), 20L), TASK_ACCESS_DENIED);
        assertEquals(1, taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), 20L).getStatus());
    }

    @Test
    public void testCreateTask_success() {
        // 准备参数
        Long publisherUserId = randomLongId();
        Long firstReceiverUserId = randomLongId();
        Long secondReceiverUserId = randomLongId();
        OaTaskSaveReqVO reqVO = buildTaskSaveReqVO(Arrays.asList(firstReceiverUserId,
                secondReceiverUserId));

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(publisherUserId), new MockHttpServletRequest());
        Long taskId = taskService.createTask(reqVO, publisherUserId);

        // 断言
        OaTaskDO task = taskMapper.selectById(taskId);
        verify(adminUserApi).validateUserList(reqVO.getReceiverUserIds());
        assertEquals(publisherUserId.toString(), task.getCreator());
        assertEquals(publisherUserId.toString(), task.getUpdater());
        assertEquals(OaTaskStatusEnum.NEW.getStatus(), task.getStatus());
        assertFalse(task.getTop());
        assertFalse(task.getCanceled());
        List<OaTaskReceiverDO> receivers = taskReceiverMapper.selectListByTaskId(taskId);
        assertEquals(Arrays.asList(firstReceiverUserId, secondReceiverUserId),
                convertList(receivers, OaTaskReceiverDO::getUserId));
    }

    @Test
    public void testCreateTask_selectedInitialState() {
        // 准备参数
        Long userId = randomLongId();
        Long receiverUserId = randomLongId();
        OaTaskSaveReqVO reqVO = buildTaskSaveReqVO(Arrays.asList(receiverUserId))
                .setStatus(OaTaskStatusEnum.COMPLETED.getStatus()).setTop(true).setCanceled(true);

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());
        Long taskId = taskService.createTask(reqVO, userId);

        // 断言
        OaTaskDO task = taskMapper.selectById(taskId);
        assertEquals(OaTaskStatusEnum.COMPLETED.getStatus(), task.getStatus());
        assertTrue(task.getTop());
        assertTrue(task.getCanceled());
        assertEquals(task.getStatus(), taskReceiverMapper.selectByTaskIdAndUserId(taskId, receiverUserId).getStatus());
    }

    @Test
    public void testUpdateTask_overwriteReceiverStatus() {
        // mock 数据
        Long publisherUserId = randomLongId();
        Long retainedReceiverUserId = randomLongId();
        Long removedReceiverUserId = randomLongId();
        Long addedReceiverUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId).setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus());
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(retainedReceiverUserId)
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(removedReceiverUserId)
                .setStatus(OaTaskStatusEnum.SUBMITTED.getStatus()));
        // 准备参数
        OaTaskSaveReqVO reqVO = buildTaskSaveReqVO(Arrays.asList(retainedReceiverUserId, addedReceiverUserId))
                .setId(task.getId()).setTitle("修改后的任务").setStatus(OaTaskStatusEnum.SUBMITTED.getStatus());

        // 调用
        taskService.updateTask(reqVO, publisherUserId);

        // 断言
        assertEquals("修改后的任务", taskMapper.selectById(task.getId()).getTitle());
        assertEquals(publisherUserId.toString(), taskMapper.selectById(task.getId()).getCreator());
        assertEquals(OaTaskStatusEnum.SUBMITTED.getStatus(), taskMapper.selectById(task.getId()).getStatus());
        assertEquals(OaTaskStatusEnum.SUBMITTED.getStatus(), taskReceiverMapper
                .selectByTaskIdAndUserId(task.getId(), retainedReceiverUserId).getStatus());
        assertEquals(OaTaskStatusEnum.SUBMITTED.getStatus(), taskReceiverMapper
                .selectByTaskIdAndUserId(task.getId(), addedReceiverUserId).getStatus());
        assertNull(taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), removedReceiverUserId));
    }

    @Test
    public void testUpdateTask_removeReceiverTwice() {
        // mock 数据
        Long publisherUserId = randomLongId();
        Long retainedUserId = randomLongId();
        Long removedUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(retainedUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(removedUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        // 准备参数
        OaTaskSaveReqVO reqVO = buildTaskSaveReqVO(Collections.singletonList(retainedUserId)).setId(task.getId());

        // 调用：移除、重新加入、再次移除同一接收人
        taskService.updateTask(reqVO, publisherUserId);
        taskService.updateTask(reqVO.setReceiverUserIds(Arrays.asList(retainedUserId, removedUserId)), publisherUserId);
        taskService.updateTask(reqVO.setReceiverUserIds(Collections.singletonList(retainedUserId)), publisherUserId);
        // 断言
        assertNull(taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), removedUserId));
        assertEquals(Collections.singletonList(retainedUserId),
                convertList(taskReceiverMapper.selectListByTaskId(task.getId()), OaTaskReceiverDO::getUserId));
    }

    @Test
    public void testFeedbackTask_receiverUpdateAndOverallMinimum() {
        // mock 数据
        Long publisherUserId = randomLongId();
        Long firstReceiverUserId = randomLongId();
        Long secondReceiverUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(firstReceiverUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(secondReceiverUserId)
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus()));
        // 准备参数
        OaTaskFeedbackReqVO reqVO = new OaTaskFeedbackReqVO().setPublisher(false).setTaskId(task.getId())
                .setStatus(OaTaskStatusEnum.RECEIVED.getStatus()).setContent("已接收任务");

        // 调用
        taskService.feedbackTask(reqVO, firstReceiverUserId);

        // 断言
        assertEquals(OaTaskStatusEnum.RECEIVED.getStatus(), taskReceiverMapper
                .selectByTaskIdAndUserId(task.getId(), firstReceiverUserId).getStatus());
        assertEquals(OaTaskStatusEnum.RECEIVED.getStatus(), taskMapper.selectById(task.getId()).getStatus());
        OaTaskLogDO log = CollUtil.getFirst(taskLogMapper.selectListByTaskId(task.getId()));
        assertEquals(firstReceiverUserId, log.getUserId());
        assertEquals("已接收任务", log.getContent());
    }

    @Test
    public void testFeedbackTask_receiverCanSkipStatus() {
        // mock 数据
        Long receiverUserId = randomLongId();
        OaTaskDO task = buildTaskDO(randomLongId());
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(receiverUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        // 准备参数
        OaTaskFeedbackReqVO reqVO = new OaTaskFeedbackReqVO().setPublisher(false).setTaskId(task.getId())
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus()).setContent("跳过接收状态");

        // 调用
        taskService.feedbackTask(reqVO, receiverUserId);

        // 断言
        assertEquals(reqVO.getStatus(), taskReceiverMapper
                .selectByTaskIdAndUserId(task.getId(), receiverUserId).getStatus());
    }

    @Test
    public void testFeedbackTask_receiverCannotCompleteOrResubmit() {
        // mock 数据
        Long userId = randomLongId();
        OaTaskDO task = buildTaskDO(randomLongId());
        taskMapper.insert(task);
        OaTaskReceiverDO receiver = new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(userId)
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus());
        taskReceiverMapper.insert(receiver);
        // 准备参数
        OaTaskFeedbackReqVO reqVO = new OaTaskFeedbackReqVO().setPublisher(false).setTaskId(task.getId())
                .setStatus(OaTaskStatusEnum.COMPLETED.getStatus()).setContent("反馈进度");

        // 调用，并断言：接收人不能验收完成
        assertServiceException(() -> taskService.feedbackTask(reqVO, userId), TASK_STATUS_TRANSITION_INVALID);

        // 调用：提交前可以回退，再直接提交
        taskService.feedbackTask(reqVO.setStatus(OaTaskStatusEnum.NEW.getStatus()), userId);
        assertEquals(OaTaskStatusEnum.NEW.getStatus(), taskReceiverMapper.selectById(receiver.getId()).getStatus());
        taskService.feedbackTask(reqVO.setStatus(OaTaskStatusEnum.SUBMITTED.getStatus()), userId);

        // 断言：已提交后不能再次反馈
        assertServiceException(() -> taskService.feedbackTask(reqVO, userId), TASK_STATUS_TRANSITION_INVALID);
        taskReceiverMapper.updateById(receiver.setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));
        assertServiceException(() -> taskService.feedbackTask(reqVO, userId), TASK_STATUS_TRANSITION_INVALID);
    }

    @Test
    public void testFeedbackTask_publisherSyncAllReceivers() {
        // mock 数据
        Long publisherUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(randomLongId())
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(randomLongId())
                .setStatus(OaTaskStatusEnum.RECEIVED.getStatus()));
        // 准备参数
        OaTaskFeedbackReqVO reqVO = new OaTaskFeedbackReqVO().setPublisher(true).setTaskId(task.getId())
                .setStatus(OaTaskStatusEnum.COMPLETED.getStatus()).setContent("任务验收完成");

        // 调用
        taskService.feedbackTask(reqVO, publisherUserId);

        // 断言
        assertEquals(OaTaskStatusEnum.COMPLETED.getStatus(), taskMapper.selectById(task.getId()).getStatus());
        assertTrue(taskReceiverMapper.selectListByTaskId(task.getId()).stream()
                .allMatch(receiver -> OaTaskStatusEnum.COMPLETED.getStatus().equals(receiver.getStatus())));
    }

    @Test
    public void testDeleteTask_onlyPublisherAndCascade() {
        // mock 数据
        Long publisherUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(randomLongId())
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskLogMapper.insert(new OaTaskLogDO().setTaskId(task.getId()).setUserId(publisherUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()).setContent("任务已发布"));

        // 调用，并断言非发布人无法删除
        assertServiceException(() -> taskService.deleteTask(task.getId(), randomLongId()), TASK_ACCESS_DENIED);

        // 调用
        taskService.deleteTask(task.getId(), publisherUserId);

        // 断言
        assertNull(taskMapper.selectById(task.getId()));
        assertTrue(taskReceiverMapper.selectListByTaskId(task.getId()).isEmpty());
        assertTrue(taskLogMapper.selectListByTaskId(task.getId()).isEmpty());
    }

    @Test
    public void testDeleteReceivedTask_onlyCurrentRelationship() {
        // mock 数据
        Long currentUserId = randomLongId();
        Long otherUserId = randomLongId();
        OaTaskDO task = buildTaskDO(randomLongId()).setCanceled(true);
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(currentUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(otherUserId)
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus()));

        // 调用
        taskService.deleteReceivedTask(task.getId(), currentUserId);

        // 断言
        assertNull(taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), currentUserId));
        assertEquals(OaTaskStatusEnum.IN_PROGRESS.getStatus(), taskReceiverMapper
                .selectByTaskIdAndUserId(task.getId(), otherUserId).getStatus());
        assertEquals(OaTaskStatusEnum.IN_PROGRESS.getStatus(), taskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    public void testDeleteReceivedTask_notCanceled() {
        // mock 数据
        Long currentUserId = randomLongId();
        OaTaskDO task = buildTaskDO(randomLongId());
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(currentUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> taskService.deleteReceivedTask(task.getId(), currentUserId),
                TASK_RECEIVED_DELETE_DENIED);
    }

    @Test
    public void testGetTaskPage_publishedAndReceivedScopes() {
        // mock 数据
        Long publisherUserId = randomLongId();
        Long receiverUserId = randomLongId();
        OaTaskDO publishedTask = buildTaskDO(publisherUserId).setTitle("我发布的任务");
        taskMapper.insert(publishedTask);
        OaTaskDO receivedTask = buildTaskDO(randomLongId()).setTitle("我的任务");
        taskMapper.insert(receivedTask);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(receivedTask.getId()).setUserId(receiverUserId)
                .setStatus(OaTaskStatusEnum.RECEIVED.getStatus()));
        taskMapper.insert(buildTaskDO(randomLongId()).setTitle("无关任务"));

        // 准备参数
        OaTaskPageReqVO publishedPageReqVO = new OaTaskPageReqVO();
        OaTaskPageReqVO receivedPageReqVO = new OaTaskPageReqVO()
                .setStatus(OaTaskStatusEnum.RECEIVED.getStatus());

        // 调用
        PageResult<OaTaskDO> publishedPage = taskService.getPublishedTaskPage(publishedPageReqVO, publisherUserId);
        PageResult<OaTaskDO> receivedPage = taskService.getReceivedTaskPage(receivedPageReqVO, receiverUserId);

        // 断言
        assertEquals(Collections.singletonList(publishedTask.getId()),
                convertList(publishedPage.getList(), OaTaskDO::getId));
        assertEquals(Collections.singletonList(receivedTask.getId()),
                convertList(receivedPage.getList(), OaTaskDO::getId));
    }

    @Test
    public void testGetTaskStatistics() {
        // mock 数据
        Long firstUserId = randomLongId();
        Long secondUserId = randomLongId();
        taskMapper.insert(buildTaskDO(firstUserId).setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));
        taskMapper.insert(buildTaskDO(firstUserId).setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));
        taskMapper.insert(buildTaskDO(secondUserId).setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));
        taskMapper.insert(buildTaskDO(secondUserId).setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(randomLongId()).setUserId(firstUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(randomLongId()).setUserId(firstUserId)
                .setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(randomLongId()).setUserId(firstUserId)
                .setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(randomLongId()).setUserId(secondUserId)
                .setStatus(OaTaskStatusEnum.COMPLETED.getStatus()));

        // 调用
        Map<Integer, Long> statusCountMap = taskService.getTaskStatusCountMap(firstUserId);
        Map<Long, Long> completedCountMap = taskService.getCompletedTaskCountMapByUserId();

        // 断言
        assertEquals(1L, statusCountMap.get(OaTaskStatusEnum.NEW.getStatus()));
        assertEquals(2L, statusCountMap.get(OaTaskStatusEnum.COMPLETED.getStatus()));
        assertEquals(Arrays.asList(firstUserId, secondUserId), new ArrayList<>(completedCountMap.keySet()));
        assertEquals(2L, completedCountMap.get(firstUserId));
        assertEquals(1L, completedCountMap.get(secondUserId));
    }

    @Test
    public void testUpdateTask_topAndCanceledPreserveProgressAndRestore() {
        // mock 数据
        Long publisherUserId = randomLongId();
        Long receiverUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId).setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus());
        taskMapper.insert(task);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(receiverUserId)
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus()));

        // 准备参数
        OaTaskSaveReqVO reqVO = buildTaskSaveReqVO(Arrays.asList(receiverUserId))
                .setId(task.getId()).setTop(true).setCanceled(true)
                .setStatus(OaTaskStatusEnum.IN_PROGRESS.getStatus());

        // 调用：表单置顶和取消任务不改变进度，取消期间禁止反馈
        taskService.updateTask(reqVO, publisherUserId);

        // 断言
        assertTrue(taskMapper.selectById(task.getId()).getCanceled());
        assertTrue(taskMapper.selectById(task.getId()).getTop());
        assertEquals(OaTaskStatusEnum.IN_PROGRESS.getStatus(), taskMapper.selectById(task.getId()).getStatus());
        assertServiceException(() -> taskService.feedbackTask(new OaTaskFeedbackReqVO().setPublisher(false)
                .setTaskId(task.getId()).setStatus(OaTaskStatusEnum.SUBMITTED.getStatus()), receiverUserId),
                TASK_STATUS_TRANSITION_INVALID);

        // 调用：恢复任务保留取消前的进度
        taskService.updateTask(reqVO.setTop(false).setCanceled(false), publisherUserId);

        // 断言
        assertFalse(taskMapper.selectById(task.getId()).getCanceled());
        assertFalse(taskMapper.selectById(task.getId()).getTop());
        assertEquals(OaTaskStatusEnum.IN_PROGRESS.getStatus(), taskMapper.selectById(task.getId()).getStatus());
        assertEquals(OaTaskStatusEnum.IN_PROGRESS.getStatus(),
                taskReceiverMapper.selectByTaskIdAndUserId(task.getId(), receiverUserId).getStatus());
    }

    @Test
    public void testUpdateTask_notCreator() {
        // mock 数据
        OaTaskDO task = buildTaskDO(randomLongId());
        taskMapper.insert(task);

        // 调用，并断言非创建人无权取消任务
        OaTaskSaveReqVO reqVO = buildTaskSaveReqVO(Arrays.asList(randomLongId()))
                .setId(task.getId()).setTop(true).setCanceled(true);
        assertServiceException(() -> taskService.updateTask(reqVO, randomLongId()),
                TASK_ACCESS_DENIED);
        assertFalse(taskMapper.selectById(task.getId()).getCanceled());
    }

    @Test
    public void testGetReceivedTaskPage_filterCreator() {
        // mock 数据
        Long publisherUserId = randomLongId();
        Long receiverUserId = randomLongId();
        OaTaskDO task = buildTaskDO(publisherUserId);
        OaTaskDO otherTask = buildTaskDO(randomLongId());
        taskMapper.insert(task);
        taskMapper.insert(otherTask);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(receiverUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(otherTask.getId()).setUserId(receiverUserId)
                .setStatus(OaTaskStatusEnum.NEW.getStatus()));

        // 准备参数
        OaTaskPageReqVO reqVO = new OaTaskPageReqVO().setPublisherUserId(publisherUserId);

        // 调用
        PageResult<OaTaskDO> page = taskService.getReceivedTaskPage(reqVO, receiverUserId);

        // 断言
        assertEquals(Collections.singletonList(task.getId()), convertList(page.getList(), OaTaskDO::getId));
    }

    @Test
    public void testGetPublishedTaskPage_filters() {
        // mock 数据
        Long userId = randomLongId();
        LocalDateTime createTime = LocalDateTime.now().minusDays(2).withNano(0);
        OaTaskDO task = buildTaskDO(userId).setTitle("验收任务").setCanceled(true);
        task.setCreateTime(createTime.minusDays(1));
        task.setPublishTime(createTime);
        taskMapper.insert(task);

        // 准备参数
        OaTaskPageReqVO reqVO = new OaTaskPageReqVO().setTitle("验收")
                .setType(task.getType()).setStatus(task.getStatus()).setCanceled(true)
                .setPublishTime(new LocalDateTime[]{createTime, createTime});

        // 调用，并断言全部条件匹配
        assertEquals(1, taskService.getPublishedTaskPage(reqVO, userId).getTotal());

        // 断言标题、类型、状态、取消和时间条件分别生效
        assertEquals(0, taskService.getPublishedTaskPage(reqVO.setTitle("不匹配"), userId).getTotal());
        reqVO.setTitle("验收");
        assertEquals(0, taskService.getPublishedTaskPage(reqVO.setType(2), userId).getTotal());
        reqVO.setType(task.getType());
        assertEquals(0, taskService.getPublishedTaskPage(reqVO.setStatus(OaTaskStatusEnum.COMPLETED.getStatus()),
                userId).getTotal());
        reqVO.setStatus(task.getStatus());
        assertEquals(0, taskService.getPublishedTaskPage(reqVO.setCanceled(false), userId).getTotal());
        reqVO.setCanceled(true);
        assertEquals(0, taskService.getPublishedTaskPage(reqVO.setPublishTime(
                new LocalDateTime[]{createTime.plusDays(1), createTime.plusDays(2)}), userId).getTotal());
    }

    @Test
    public void testGetTaskReceiverListMap() {
        // mock 数据
        OaTaskReceiverDO first = new OaTaskReceiverDO().setTaskId(1L).setUserId(10L).setStatus(1);
        taskReceiverMapper.insert(first);
        OaTaskReceiverDO second = new OaTaskReceiverDO().setTaskId(1L).setUserId(11L).setStatus(1);
        taskReceiverMapper.insert(second);
        taskReceiverMapper.insert(new OaTaskReceiverDO().setTaskId(2L).setUserId(12L).setStatus(1));

        // 调用
        Map<Long, List<OaTaskReceiverDO>> receiverListMap = taskService.getTaskReceiverListMap(
                Collections.singletonList(1L));

        // 断言
        assertEquals(1, receiverListMap.size());
        assertEquals(2, receiverListMap.get(1L).size());
        assertTrue(convertList(receiverListMap.get(1L), OaTaskReceiverDO::getUserId)
                .containsAll(Arrays.asList(10L, 11L)));
    }

    @Test
    public void testGetTaskReceiverListMap_empty() {

        // 调用
        Map<Long, List<OaTaskReceiverDO>> receiverListMap = taskService.getTaskReceiverListMap(
                Collections.emptyList());

        // 断言
        assertTrue(CollUtil.isEmpty(receiverListMap));
    }

    // ========== 随机对象 ==========

    /**
     * 构造开始时间晚于当前时间的新建任务参数。
     *
     * @param receiverUserIds 接收人编号列表
     * @return 保存参数
     */
    private static OaTaskSaveReqVO buildTaskSaveReqVO(List<Long> receiverUserIds) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return new OaTaskSaveReqVO().setType(OaTaskTypeEnum.WORK.getType()).setTitle("完成 OA 任务管理迁移")
                .setStatus(OaTaskStatusEnum.NEW.getStatus()).setTop(false).setCanceled(false)
                .setDescription("完成任务管理的前后端实现和自动化测试")
                .setStartTime(startTime).setEndTime(startTime.plusDays(7)).setReceiverUserIds(receiverUserIds);
    }

    /**
     * 构造开始时间晚于当前时间、未取消的新建任务。
     *
     * @param publisherUserId 发布人编号
     * @return 未入库的测试对象
     */
    private static OaTaskDO buildTaskDO(Long publisherUserId) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        OaTaskDO task = new OaTaskDO().setType(OaTaskTypeEnum.WORK.getType())
                .setStatus(OaTaskStatusEnum.NEW.getStatus()).setTitle("完成 OA 任务管理迁移")
                .setDescription("完成任务管理的前后端实现和自动化测试")
                .setStartTime(startTime).setEndTime(startTime.plusDays(7)).setTop(false).setCanceled(false);
        task.setCreator(publisherUserId.toString());
        return task;
    }

}
