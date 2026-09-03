package cn.iocoder.yudao.module.pms.service.pm.iteration;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationStartReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemActivityDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.iteration.PmsIterationMapper;
import cn.iocoder.yudao.module.pms.enums.pm.iteration.PmsIterationStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemActivityService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemWorkLogService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.ITERATION_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsIterationServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsIterationServiceImpl.class)
public class PmsIterationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsIterationServiceImpl iterationService;

    @Resource
    private PmsIterationMapper iterationMapper;

    @MockitoBean
    private PmsProjectMemberService projectMemberService;
    @MockitoBean
    private PmsWorkItemService workItemService;
    @MockitoBean
    private PmsWorkItemActivityService workItemActivityService;
    @MockitoBean
    private PmsWorkItemWorkLogService workItemWorkLogService;

    @Test
    public void testCreateIteration_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsProjectDO project = randomProjectDO(projectId);
        when(projectMemberService.validateProjectWritable(projectId, userId)).thenReturn(project);
        iterationMapper.insert(randomIterationDO(projectId, PmsIterationStatusEnum.PLANNED.getStatus()).setSort(3));
        // 准备参数
        PmsIterationSaveReqVO reqVO = randomIterationSaveReqVO(projectId);

        // 调用
        Long iterationId = iterationService.createIteration(reqVO, userId);

        // 断言
        PmsIterationDO iteration = iterationMapper.selectById(iterationId);
        assertNotNull(iteration);
        assertEquals(reqVO.getName(), iteration.getName());
        assertEquals(PmsIterationStatusEnum.PLANNED.getStatus(), iteration.getStatus());
        assertEquals(4, iteration.getSort());
        verify(projectMemberService).validateProjectMemberExists(projectId, reqVO.getOwnerUserId());
    }

    @Test
    public void testUpdateIteration_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsIterationDO iteration = randomIterationDO(randomLongId(), PmsIterationStatusEnum.PLANNED.getStatus());
        iterationMapper.insert(iteration);
        when(projectMemberService.validateProjectWritable(iteration.getProjectId(), userId))
                .thenReturn(randomProjectDO(iteration.getProjectId()));
        // 准备参数
        PmsIterationSaveReqVO reqVO = randomPojo(PmsIterationSaveReqVO.class,
                item -> item.setId(iteration.getId()).setProjectId(iteration.getProjectId())
                .setName("第二期迭代").setOwnerUserId(randomLongId()).setTarget("交付第二期")
                .setStartTime(LocalDateTime.now()).setEndTime(LocalDateTime.now().plusDays(10)));

        // 调用
        iterationService.updateIteration(reqVO, userId);

        // 断言
        PmsIterationDO updatedIteration = iterationMapper.selectById(iteration.getId());
        assertEquals(reqVO.getName(), updatedIteration.getName());
        assertEquals(reqVO.getTarget(), updatedIteration.getTarget());
        assertEquals(iteration.getStatus(), updatedIteration.getStatus());
    }

    @Test
    public void testStartIteration_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsIterationDO iteration = randomIterationDO(randomLongId(), PmsIterationStatusEnum.PLANNED.getStatus());
        iterationMapper.insert(iteration);
        when(projectMemberService.validateProjectWritable(iteration.getProjectId(), userId))
                .thenReturn(randomProjectDO(iteration.getProjectId()));
        // 准备参数
        LocalDateTime startTime = LocalDateTime.now();
        PmsIterationStartReqVO reqVO = new PmsIterationStartReqVO().setId(iteration.getId())
                .setStartTime(startTime).setEndTime(startTime.plusDays(14));

        // 调用
        iterationService.startIteration(reqVO, userId);

        // 断言
        PmsIterationDO startedIteration = iterationMapper.selectById(iteration.getId());
        assertEquals(PmsIterationStatusEnum.ACTIVE.getStatus(), startedIteration.getStatus());
        assertEquals(reqVO.getStartTime(), startedIteration.getStartTime());
        assertEquals(reqVO.getEndTime(), startedIteration.getEndTime());
    }

    @Test
    public void testStartIteration_statusInvalid() {
        // mock 数据
        Long userId = randomLongId();
        PmsIterationDO iteration = randomIterationDO(randomLongId(), PmsIterationStatusEnum.COMPLETED.getStatus());
        iterationMapper.insert(iteration);
        when(projectMemberService.validateProjectWritable(iteration.getProjectId(), userId))
                .thenReturn(randomProjectDO(iteration.getProjectId()));
        // 准备参数
        LocalDateTime startTime = LocalDateTime.now();
        PmsIterationStartReqVO reqVO = new PmsIterationStartReqVO().setId(iteration.getId())
                .setStartTime(startTime).setEndTime(startTime.plusDays(14));

        // 调用，并断言异常
        assertServiceException(() -> iterationService.startIteration(reqVO, userId), ITERATION_STATUS_INVALID);
    }

    @Test
    public void testCompleteIteration_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsIterationDO iteration = randomIterationDO(randomLongId(), PmsIterationStatusEnum.ACTIVE.getStatus());
        iterationMapper.insert(iteration);
        when(projectMemberService.validateProjectWritable(iteration.getProjectId(), userId))
                .thenReturn(randomProjectDO(iteration.getProjectId()));

        // 调用
        iterationService.completeIteration(iteration.getId(), userId);

        // 断言
        PmsIterationDO completedIteration = iterationMapper.selectById(iteration.getId());
        assertEquals(PmsIterationStatusEnum.COMPLETED.getStatus(), completedIteration.getStatus());
        assertNotNull(completedIteration.getFinishTime());
    }

    @Test
    public void testDeleteIteration_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsIterationDO iteration = randomIterationDO(randomLongId(), PmsIterationStatusEnum.PLANNED.getStatus());
        iterationMapper.insert(iteration);
        when(projectMemberService.validateProjectWritable(iteration.getProjectId(), userId))
                .thenReturn(randomProjectDO(iteration.getProjectId()));

        // 调用
        iterationService.deleteIteration(iteration.getId(), userId);

        // 断言
        assertNull(iterationMapper.selectById(iteration.getId()));
        verify(workItemService).clearWorkItemIterationId(iteration.getId());
    }

    @Test
    public void testGetIterationPage_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsIterationDO plannedIteration = randomIterationDO(projectId, PmsIterationStatusEnum.PLANNED.getStatus())
                .setName("第一期");
        iterationMapper.insert(plannedIteration);
        iterationMapper.insert(randomIterationDO(projectId, PmsIterationStatusEnum.COMPLETED.getStatus())
                .setName("历史迭代"));
        // 准备参数
        PmsIterationPageReqVO reqVO = new PmsIterationPageReqVO().setProjectId(projectId)
                .setStatus(PmsIterationStatusEnum.PLANNED.getStatus()).setName("第一");

        // 调用
        PageResult<PmsIterationDO> pageResult = iterationService.getIterationPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(plannedIteration.getId(), CollUtil.getFirst(pageResult.getList()).getId());
        verify(projectMemberService).validateProjectReadable(projectId, userId);
    }

    @Test
    public void testGetProjectIterationStatusCountMap_success() {
        // mock 数据
        Long firstProjectId = randomLongId();
        Long secondProjectId = randomLongId();
        iterationMapper.insert(randomIterationDO(firstProjectId, PmsIterationStatusEnum.PLANNED.getStatus()));
        iterationMapper.insert(randomIterationDO(firstProjectId, PmsIterationStatusEnum.PLANNED.getStatus()));
        iterationMapper.insert(randomIterationDO(firstProjectId, PmsIterationStatusEnum.COMPLETED.getStatus()));
        iterationMapper.insert(randomIterationDO(secondProjectId, PmsIterationStatusEnum.ACTIVE.getStatus()));

        // 调用
        Map<Long, Map<Integer, Long>> countMap = iterationService.getProjectIterationStatusCountMap(
                Arrays.asList(firstProjectId, secondProjectId));

        // 断言
        assertEquals(2L, countMap.get(firstProjectId).get(PmsIterationStatusEnum.PLANNED.getStatus()));
        assertEquals(1L, countMap.get(firstProjectId).get(PmsIterationStatusEnum.COMPLETED.getStatus()));
        assertEquals(1L, countMap.get(secondProjectId).get(PmsIterationStatusEnum.ACTIVE.getStatus()));
    }

    @Test
    public void testGetIterationOverview_success() {
        // mock 数据
        Long userId = randomLongId();
        LocalDateTime iterationStartTime = LocalDateTime.now().minusDays(2).toLocalDate().atStartOfDay();
        PmsIterationDO iteration = randomIterationDO(randomLongId(), PmsIterationStatusEnum.ACTIVE.getStatus())
                .setStartTime(iterationStartTime).setEndTime(iterationStartTime.plusDays(4));
        iterationMapper.insert(iteration);
        PmsWorkItemDO pendingWorkItem = randomWorkItemDO(PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType()).setEstimatedHours(8);
        PmsWorkItemDO completedWorkItem = randomWorkItemDO(PmsWorkItemTypeEnum.DEFECT.getType(),
                PmsWorkItemStatusTypeEnum.COMPLETED.getType()).setEstimatedHours(4);
        when(workItemService.getActiveWorkItemListByIterationId(iteration.getId()))
                .thenReturn(Arrays.asList(pendingWorkItem, completedWorkItem));
        PmsWorkItemWorkLogDO workLog = new PmsWorkItemWorkLogDO();
        workLog.setWorkItemId(pendingWorkItem.getId());
        workLog.setActualHours(3);
        workLog.setRemainingHours(5);
        workLog.setCreateTime(LocalDateTime.now().withHour(10));
        when(workItemWorkLogService.getWorkItemWorkLogListByWorkItemIds(ArgumentMatchers.<Long>anyCollection()))
                .thenReturn(Collections.singletonList(workLog));
        when(workItemActivityService.getWorkItemActivityListByWorkItemIds(anyCollection(), anyInt()))
                .thenReturn(Collections.singletonList(new PmsWorkItemActivityDO().setId(randomLongId())
                        .setWorkItemId(pendingWorkItem.getId()).setOperatorUserId(userId).setContent("更新了工作项")));

        // 调用
        PmsIterationOverviewRespVO overview = iterationService.getIterationOverview(iteration.getId(), userId);

        // 断言
        assertEquals(2, overview.getTotalCount());
        assertEquals(1, overview.getPendingCount());
        assertEquals(1, overview.getCompletedCount());
        assertEquals(50, overview.getProgress());
        assertEquals(1, overview.getTypeStatusCountMap().get(PmsWorkItemTypeEnum.TASK.getType())
                .get(PmsWorkItemStatusTypeEnum.PENDING.getType()));
        assertEquals(1, overview.getTypeStatusCountMap().get(PmsWorkItemTypeEnum.DEFECT.getType())
                .get(PmsWorkItemStatusTypeEnum.COMPLETED.getType()));
        assertEquals(14, overview.getStatusTrends().size());
        PmsIterationOverviewRespVO.TrendItem todayTrend = overview.getStatusTrends().get(13);
        assertEquals(1, todayTrend.getPendingCount());
        assertEquals(1, todayTrend.getCompletedCount());
        assertEquals(5, overview.getBurnDowns().size());
        assertEquals(9, overview.getBurnDowns().get(1).getIdealRemaining());
        assertEquals(9, overview.getBurnDowns().get(2).getActualRemaining());
        assertEquals(1, overview.getRecentActivities().size());
        assertEquals(pendingWorkItem.getName(), CollUtil.getFirst(overview.getRecentActivities()).getWorkItemName());
    }

    // ========== 随机对象 ==========

    private static PmsIterationSaveReqVO randomIterationSaveReqVO(Long projectId) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return randomPojo(PmsIterationSaveReqVO.class,
                item -> item.setId(null).setProjectId(projectId).setName("第一期迭代")
                .setOwnerUserId(randomLongId()).setTarget("交付第一期").setDescription("迭代说明")
                .setStartTime(startTime).setEndTime(startTime.plusDays(14)));
    }

    private static PmsIterationDO randomIterationDO(Long projectId, Integer status) {
        return randomPojo(PmsIterationDO.class, item -> item.setProjectId(projectId).setName("测试迭代")
                .setOwnerUserId(null).setStatus(status).setSort(1).setStartTime(null).setEndTime(null)
                .setFinishTime(null));
    }

    private static PmsProjectDO randomProjectDO(Long id) {
        return randomPojo(PmsProjectDO.class, project -> project.setId(id).setName("测试项目")
                .setStatus(PmsProjectStatusEnum.ACTIVE.getStatus()).setType(1).setLevel(3).setOpenStatus(false));
    }

    private static PmsWorkItemDO randomWorkItemDO(Integer type, Integer status) {
        PmsWorkItemDO workItem = randomPojo(PmsWorkItemDO.class, item -> item.setId(randomLongId()).setType(type)
                .setStatus(status).setSerialNumber(1).setName("测试工作项"));
        workItem.setUpdateTime(LocalDateTime.now());
        return workItem;
    }

}
