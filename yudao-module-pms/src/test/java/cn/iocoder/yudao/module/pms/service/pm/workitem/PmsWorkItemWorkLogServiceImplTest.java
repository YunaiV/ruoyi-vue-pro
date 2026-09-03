package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSummaryRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemWorkLogMapper;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_WORK_LOG_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsWorkItemWorkLogServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsWorkItemWorkLogServiceImpl.class)
public class PmsWorkItemWorkLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemWorkLogServiceImpl workLogService;

    @Resource
    private PmsWorkItemWorkLogMapper workLogMapper;

    @MockitoBean
    private PmsWorkItemService workItemService;
    @MockitoBean
    private PmsProjectMemberService projectMemberService;
    @MockitoBean
    private PmsWorkItemActivityService workItemActivityService;
    @MockitoBean
    private PmsIterationService iterationService;

    @Test
    public void testCreateWorkItemWorkLog_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsWorkItemDO workItem = randomWorkItemDO();
        when(workItemService.getWritableWorkItem(workItem.getId(), userId)).thenReturn(workItem);
        when(projectMemberService.validateProjectWritable(workItem.getProjectId(), userId))
                .thenReturn(new PmsProjectDO().setId(workItem.getProjectId()));
        // 准备参数
        PmsWorkItemWorkLogSaveReqVO reqVO = new PmsWorkItemWorkLogSaveReqVO()
                .setWorkItemId(workItem.getId()).setActualHours(4).setRemainingHours(6)
                .setDescription("  完成接口联调  ");

        // 调用
        Long id = workLogService.createWorkItemWorkLog(reqVO, userId);

        // 断言
        PmsWorkItemWorkLogDO workLog = workLogMapper.selectById(id);
        assertEquals(workItem.getProjectId(), workLog.getProjectId());
        assertEquals(4, workLog.getActualHours());
        assertEquals(6, workLog.getRemainingHours());
        assertEquals("完成接口联调", workLog.getDescription());
    }

    @Test
    public void testUpdateWorkItemWorkLog_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsWorkItemDO workItem = randomWorkItemDO();
        PmsWorkItemWorkLogDO workLog = new PmsWorkItemWorkLogDO().setProjectId(workItem.getProjectId())
                .setWorkItemId(workItem.getId()).setActualHours(2).setRemainingHours(8).setDescription("旧说明");
        workLogMapper.insert(workLog);
        when(workItemService.getWritableWorkItem(workItem.getId(), userId)).thenReturn(workItem);
        when(projectMemberService.validateProjectWritable(workItem.getProjectId(), userId))
                .thenReturn(new PmsProjectDO().setId(workItem.getProjectId()));
        // 准备参数
        PmsWorkItemWorkLogSaveReqVO reqVO = new PmsWorkItemWorkLogSaveReqVO().setId(workLog.getId())
                .setWorkItemId(workItem.getId()).setActualHours(5).setRemainingHours(3).setDescription(null);

        // 调用
        workLogService.updateWorkItemWorkLog(reqVO, userId);

        // 断言
        PmsWorkItemWorkLogDO updatedWorkLog = workLogMapper.selectById(workLog.getId());
        assertEquals(5, updatedWorkLog.getActualHours());
        assertEquals(3, updatedWorkLog.getRemainingHours());
        assertNull(updatedWorkLog.getDescription());
    }

    @Test
    public void testUpdateWorkItemWorkLog_workItemMismatch() {
        // mock 数据
        PmsWorkItemDO workItem = randomWorkItemDO();
        PmsWorkItemWorkLogDO workLog = new PmsWorkItemWorkLogDO().setProjectId(workItem.getProjectId())
                .setWorkItemId(workItem.getId()).setActualHours(2).setRemainingHours(8);
        workLogMapper.insert(workLog);
        // 准备参数
        PmsWorkItemWorkLogSaveReqVO reqVO = new PmsWorkItemWorkLogSaveReqVO().setId(workLog.getId())
                .setWorkItemId(randomLongId()).setActualHours(5).setRemainingHours(3);

        // 调用，并断言异常
        assertServiceException(() -> workLogService.updateWorkItemWorkLog(reqVO, randomLongId()),
                WORK_ITEM_WORK_LOG_INVALID);
    }

    @Test
    public void testGetWorkItemWorkLogSummary_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsWorkItemDO workItem = randomWorkItemDO().setEstimatedHours(10);
        when(workItemService.getWorkItem(workItem.getId(), userId)).thenReturn(workItem);
        workLogMapper.insert(new PmsWorkItemWorkLogDO().setProjectId(workItem.getProjectId())
                .setWorkItemId(workItem.getId()).setActualHours(3).setRemainingHours(7));
        workLogMapper.insert(new PmsWorkItemWorkLogDO().setProjectId(workItem.getProjectId())
                .setWorkItemId(workItem.getId()).setActualHours(4).setRemainingHours(2));

        // 调用
        PmsWorkItemWorkLogSummaryRespVO summary = workLogService.getWorkItemWorkLogSummary(workItem.getId(), userId);

        // 断言
        assertEquals(10, summary.getEstimatedHours());
        assertEquals(7, summary.getActualHours());
        assertEquals(2, summary.getRemainingHours());
        assertEquals(2, summary.getRecords().size());
    }

    @Test
    public void testGetProjectWorkItemWorkLogReport_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        Long iterationId = randomLongId();
        PmsWorkItemDO workItem = randomWorkItemDO().setProjectId(projectId).setIterationId(iterationId)
                .setSerialNumber(1);
        workLogMapper.insert(new PmsWorkItemWorkLogDO().setProjectId(projectId)
                .setWorkItemId(workItem.getId()).setActualHours(3));
        workLogMapper.insert(new PmsWorkItemWorkLogDO().setProjectId(projectId)
                .setWorkItemId(workItem.getId()).setActualHours(4));
        when(workItemService.getWorkItemList(Collections.singleton(workItem.getId())))
                .thenReturn(Collections.singletonList(workItem));
        when(iterationService.getIterationList(Collections.singleton(iterationId)))
                .thenReturn(Collections.singletonList(new PmsIterationDO().setId(iterationId).setName("第一迭代")));

        // 调用
        LocalDate today = LocalDate.now();
        PmsProjectWorkLogReportReqVO reqVO = new PmsProjectWorkLogReportReqVO().setProjectId(projectId)
                .setCreateTime(new LocalDateTime[]{today.minusDays(1).atStartOfDay(), today.atTime(LocalTime.MAX)})
                .setIterationName("第一");
        PmsProjectWorkLogReportRespVO report = workLogService.getProjectWorkItemWorkLogReport(
                reqVO, userId);

        // 断言
        assertEquals(2, report.getDates().size());
        assertEquals(today.minusDays(1).toString(), CollUtil.getFirst(report.getDates()));
        assertEquals(today.toString(), report.getDates().get(1));
        assertEquals(7, report.getTotalHours());
        assertEquals(1, report.getGroups().size());
        assertEquals("第一迭代", CollUtil.getFirst(report.getGroups()).getIterationName());
        assertEquals(7, CollUtil.getFirst(CollUtil.getFirst(report.getGroups()).getItems()).getTotalHours());
        verify(projectMemberService).validateProjectReadable(projectId, userId);
    }

    // ========== 随机对象 ==========

    private static PmsWorkItemDO randomWorkItemDO() {
        return new PmsWorkItemDO().setId(randomLongId()).setProjectId(randomLongId()).setName("测试任务");
    }

}
