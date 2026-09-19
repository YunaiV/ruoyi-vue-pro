package cn.iocoder.yudao.module.oa.service.workreport;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.workreport.OaWorkReportDO;
import cn.iocoder.yudao.module.oa.dal.mysql.workreport.OaWorkReportMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link OaWorkReportServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaWorkReportServiceImpl.class, ValidationAutoConfiguration.class})
public class OaWorkReportServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaWorkReportServiceImpl workReportService;

    @Resource
    private OaWorkReportMapper workReportMapper;

    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private DeptApi deptApi;
    @MockitoBean
    private PermissionApi permissionApi;
    @MockitoBean
    private OaNoRedisDAO noRedisDAO;

    @Test
    public void testCreateWorkReport_textSummaryWithoutItems() {
        // 准备参数
        OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setType(1)
                .setStartTime(LocalDateTime.of(2026, 9, 17, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 17, 23, 59))
                .setSummary("今天完成客户需求核对").setWorkItems(Collections.emptyList()).setPlanItems(Collections.emptyList());
        // mock 方法
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(10L));
        when(noRedisDAO.generate(OaNoRedisDAO.WORK_REPORT_NO_PREFIX)).thenReturn("WR20260917000001");

        // 调用
        Long id = workReportService.createWorkReport(reqVO, 1L);
        // 断言
        assertEquals(reqVO.getSummary(), workReportMapper.selectById(id).getSummary());
        assertTrue(workReportMapper.selectById(id).getWorkItems().isEmpty());
    }

    @Test
    public void testCreateWorkReport_noDuplicate() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO().setNo("WR20260916000001");
        workReportMapper.insert(report);
        // 准备参数
        OaWorkReportSaveReqVO reqVO = BeanUtils.toBean(report, OaWorkReportSaveReqVO.class).setId(null);
        // mock 方法
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(10L));
        when(noRedisDAO.generate(OaNoRedisDAO.WORK_REPORT_NO_PREFIX)).thenReturn(report.getNo());

        // 调用，并断言异常
        assertServiceException(() -> workReportService.createWorkReport(reqVO, 1L), WORK_REPORT_NO_DUPLICATE);
        assertEquals(1L, workReportMapper.selectCount());
    }

    @Test
    public void testCreateWorkReport_emptyDraft() {
        // 准备参数
        OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setType(1)
                .setStartTime(LocalDateTime.of(2026, 9, 7, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59))
                .setWorkItems(Collections.emptyList()).setPlanItems(Collections.emptyList());

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> workReportService.createWorkReport(reqVO, 1L));
        verifyNoInteractions(adminUserApi, noRedisDAO);
    }

    @Test
    public void testCreateWorkReport_customPeriod() {
        // 准备参数：周报、月报均允许调整实际汇报日期
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(10L));
        when(noRedisDAO.generate(OaNoRedisDAO.WORK_REPORT_NO_PREFIX))
                .thenReturn("WR20260913000001", "WR20260913000002");
        for (Integer type : Arrays.asList(2, 3)) {
            OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setType(type)
                    .setStartTime(LocalDateTime.of(2026, 9, 9, 0, 0))
                    .setEndTime(LocalDateTime.of(2026, 9, 10, 23, 59, 59))
                    .setWorkItems(Collections.emptyList())
                    .setPlanItems(Collections.singletonList(new OaWorkReportSaveReqVO.PlanItem().setContent("下期计划")));

            // 调用
            Long id = workReportService.createWorkReport(reqVO, 1L);

            // 断言：保留实际日期，以开始日期确定统计周期
            OaWorkReportDO report = workReportMapper.selectById(id);
            assertEquals(reqVO.getStartTime(), report.getStartTime());
            assertEquals(reqVO.getEndTime(), report.getEndTime());
            assertEquals(type, report.getType());
        }
    }

    @Test
    public void testCreateWorkReport_invalidPeriod() {
        // 准备参数
        OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setType(2)
                .setStartTime(LocalDateTime.of(2026, 9, 10, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 9, 0, 0))
                .setWorkItems(Collections.emptyList()).setPlanItems(Collections.emptyList());

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> workReportService.createWorkReport(reqVO, 1L));
        verifyNoInteractions(noRedisDAO);
    }

    @Test
    public void testUpdateWorkReport_emptyContent() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO();
        workReportMapper.insert(report);
        // 准备参数
        OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setId(report.getId()).setType(report.getType())
                .setStartTime(report.getStartTime()).setEndTime(report.getEndTime())
                .setWorkItems(Collections.emptyList()).setPlanItems(Collections.emptyList());

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> workReportService.updateWorkReport(reqVO, 1L));
        assertEquals(1, workReportMapper.selectById(report.getId()).getStatus());
        assertEquals(report.getWorkItems(), workReportMapper.selectById(report.getId()).getWorkItems());
    }

    @Test
    public void testSubmitAndCancelWorkReport_success() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO();
        workReportMapper.insert(report);

        // 调用，并断言：提交后不能修改或删除，取消后恢复草稿
        workReportService.submitWorkReport(report.getId(), 1L);
        assertEquals(2, workReportMapper.selectById(report.getId()).getStatus());
        assertServiceException(() -> workReportService.deleteWorkReport(report.getId(), 1L),
                WORK_REPORT_STATUS_INVALID);
        assertServiceException(() -> workReportService.updateWorkReport(
                BeanUtils.toBean(report, OaWorkReportSaveReqVO.class), 1L), WORK_REPORT_STATUS_INVALID);
        workReportService.cancelWorkReport(report.getId(), 1L);
        assertEquals(1, workReportMapper.selectById(report.getId()).getStatus());
        workReportService.deleteWorkReport(report.getId(), 1L);
        assertNull(workReportMapper.selectById(report.getId()));
    }

    @Test
    public void testDeleteWorkReport_notOwner() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO();
        workReportMapper.insert(report);

        // 调用，并断言异常
        assertServiceException(() -> workReportService.deleteWorkReport(report.getId(), 2L),
                WORK_REPORT_ACCESS_DENIED);
    }

    @Test
    public void testGetWorkReport_managerCannotReadDraft() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO();
        workReportMapper.insert(report);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(2L)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(1L)));
        when(permissionApi.hasAnyPermissions(2L, "oa:work-report:statistics")).thenReturn(true);

        // 调用，并断言异常
        assertServiceException(() -> workReportService.getWorkReport(report.getId(), 2L),
                WORK_REPORT_ACCESS_DENIED);
        workReportService.submitWorkReport(report.getId(), 1L);
        assertEquals(report.getId(), workReportService.getWorkReport(report.getId(), 2L).getId());
    }

    @Test
    public void testGetWorkReportStatistics_partialWeekAndZeroReports() {
        // mock 数据：同一员工同一周期多份汇报只计算一次填写
        OaWorkReportDO report = randomWorkReportDO().setType(2).setStatus(2);
        workReportMapper.insert(report);
        workReportMapper.insert(randomWorkReportDO().setType(2).setStatus(2));
        // 准备参数：从周三开始，仍包含这一周的汇报
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(2)
                .setStartTime(LocalDateTime.of(2026, 9, 9, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 9, 23, 59, 59))
                .setQueryStartTime(LocalDateTime.of(2026, 9, 7, 0, 0)).setQueryEndTime(LocalDateTime.of(2026, 9, 13, 23, 59, 59));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(9L)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(1L).setDeptId(10L).setStatus(0),
                new AdminUserRespDTO().setId(2L).setDeptId(10L).setStatus(0),
                new AdminUserRespDTO().setId(3L).setDeptId(10L).setStatus(1)));
        when(deptApi.getDeptMap(anyCollection())).thenReturn(Collections.emptyMap());

        // 调用
        OaWorkReportStatisticsRespVO result = workReportService.getWorkReportStatistics(reqVO, 9L);

        // 断言
        assertEquals(2, result.getUserCount());
        assertEquals(2, result.getExpectedCount());
        assertEquals(1, result.getSubmittedCount());
        assertEquals(1, result.getMissingCount());
        assertEquals(1, result.getUsers().get(0).getExpectedCount());
        assertEquals(1, result.getUsers().get(0).getSubmittedCount());
        assertEquals(1, result.getUsers().get(1).getExpectedCount());
        assertEquals(0, result.getUsers().get(1).getSubmittedCount());
        assertEquals(2, result.getUsers().get(0).getSubmittedReports().size());
    }

    @Test
    public void testGetWorkReportStatistics_partialMonthAndCancelled() {
        // mock 数据
        workReportMapper.insert(randomWorkReportDO().setType(3).setStatus(2)
                .setStartTime(LocalDateTime.of(2026, 8, 1, 0, 0)));
        workReportMapper.insert(randomWorkReportDO().setType(3).setStatus(1));
        // 准备参数
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(3)
                .setStartTime(LocalDateTime.of(2026, 8, 20, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 9, 23, 59, 59))
                .setQueryStartTime(LocalDateTime.of(2026, 8, 1, 0, 0)).setQueryEndTime(LocalDateTime.of(2026, 9, 30, 23, 59, 59));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(9L)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(1L).setDeptId(10L).setStatus(0)));
        when(deptApi.getDeptMap(anyCollection())).thenReturn(Collections.emptyMap());

        // 调用
        OaWorkReportStatisticsRespVO result = workReportService.getWorkReportStatistics(reqVO, 9L);

        // 断言
        assertEquals(2, result.getExpectedCount());
        assertEquals(1, result.getSubmittedCount());
        assertEquals(Collections.singletonList("2026-09"), result.getUsers().get(0).getMissingPeriodKeys());
    }

    @Test
    public void testGetWorkReportPage_filters() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO().setType(2).setNo("WR-FILTER");
        workReportMapper.insert(report);
        workReportMapper.insert(randomWorkReportDO().setType(2).setNo("OTHER"));
        OaWorkReportDO otherReport = randomWorkReportDO().setType(2).setNo("WR-FILTER-OTHER");
        otherReport.setCreator("2");
        workReportMapper.insert(otherReport);
        // 准备参数
        OaWorkReportPageReqVO reqVO = new OaWorkReportPageReqVO().setType(2).setNo("WR-FILTER")
                .setDeptId(10L).setPeriodTime(new LocalDateTime[]{LocalDateTime.of(2026, 9, 7, 0, 0),
                        LocalDateTime.of(2026, 9, 13, 23, 59, 59)})
                .setStartTime(LocalDateTime.of(2026, 9, 7, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59));

        // 调用，并断言
        assertEquals(1L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
        reqVO.setPeriodTime(new LocalDateTime[]{LocalDateTime.of(2026, 9, 14, 0, 0),
                LocalDateTime.of(2026, 9, 20, 23, 59, 59)});
        assertEquals(0L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
        reqVO.setPeriodTime(new LocalDateTime[]{LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59, 59)});
        assertEquals(0L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
        reqVO.setPeriodTime(new LocalDateTime[]{LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 9, 30, 23, 59, 59)});
        assertEquals(1L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
        reqVO.setPeriodTime(null);
        assertEquals(1L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
    }

    @Test
    public void testCreateWorkReport_calendarWeek() {
        // 准备参数
        OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setType(2)
                .setStartTime(LocalDateTime.of(2027, 1, 4, 0, 0))
                .setEndTime(LocalDateTime.of(2027, 1, 10, 23, 59, 59))
                .setWorkItems(Collections.emptyList())
                .setPlanItems(Collections.singletonList(new OaWorkReportSaveReqVO.PlanItem().setContent("下期计划")));
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(10L));
        when(noRedisDAO.generate(OaNoRedisDAO.WORK_REPORT_NO_PREFIX)).thenReturn("WR20260913000001");

        // 调用
        Long id = workReportService.createWorkReport(reqVO, 1L);

        // 断言：包含 1 月 1 日的周为当年第 1 周，跨年同一周使用相同编号
        OaWorkReportDO report = workReportMapper.selectById(id);
        assertEquals("2027-02 工作周报", report.getTitle());
        assertEquals("2027-01", OaWorkReportTypeEnum.WEEKLY.formatPeriod(LocalDateTime.of(2027, 1, 1, 0, 0)));
        assertEquals("2027-01", OaWorkReportTypeEnum.WEEKLY.formatPeriod(LocalDateTime.of(2026, 12, 28, 0, 0)));
    }

    @Test
    public void testUpdateWorkReport_preserveNo() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO().setNo("WR20260913000001");
        workReportMapper.insert(report);
        // 准备参数
        OaWorkReportSaveReqVO reqVO = new OaWorkReportSaveReqVO().setId(report.getId()).setType(3)
                .setStartTime(LocalDateTime.of(2026, 8, 20, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 8, 25, 23, 59, 59))
                .setPlanItems(Collections.singletonList(new OaWorkReportSaveReqVO.PlanItem().setContent("下期计划")));

        // 调用
        workReportService.updateWorkReport(reqVO, 1L);

        // 断言
        OaWorkReportDO updatedReport = workReportMapper.selectById(report.getId());
        assertEquals("2026-08 工作月报", updatedReport.getTitle());
        assertEquals(report.getNo(), updatedReport.getNo());
        verifyNoInteractions(noRedisDAO);
    }

    @Test
    public void testGetWorkReportStatistics_deptScope() {
        // 准备参数
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(1).setDeptId(10L)
                .setStartTime(LocalDateTime.of(2026, 9, 7, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59))
                .setQueryStartTime(LocalDateTime.of(2026, 9, 7, 0, 0)).setQueryEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59));
        // mock 方法
        when(deptApi.getChildDeptList(10L)).thenReturn(Collections.singletonList(new DeptRespDTO().setId(11L)));
        when(adminUserApi.getUserListBySubordinate(9L)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(4L).setDeptId(11L).setStatus(0),
                new AdminUserRespDTO().setId(1L).setDeptId(10L).setStatus(0),
                new AdminUserRespDTO().setId(2L).setDeptId(12L).setStatus(0),
                new AdminUserRespDTO().setId(3L).setDeptId(10L).setStatus(1)));
        when(deptApi.getDeptMap(anyCollection())).thenReturn(Collections.emptyMap());

        // 调用
        OaWorkReportStatisticsRespVO result = workReportService.getWorkReportStatistics(reqVO, 9L);

        // 断言：仅保留本部门和子部门启用员工，无汇报员工仍参与统计，编号升序
        assertEquals(2, result.getUserCount());
        assertEquals(2, result.getMissingCount());
        assertEquals(1L, result.getUsers().get(0).getUserId());
        assertEquals(4L, result.getUsers().get(1).getUserId());
    }

    @Test
    public void testGetWorkReportStatistics_invalidDateRange() {
        // 准备参数
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(1)
                .setStartTime(LocalDateTime.of(2026, 9, 9, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59))
                .setQueryStartTime(LocalDateTime.of(2026, 9, 9, 0, 0)).setQueryEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59));

        // 调用，并断言：在进入业务查询前拒绝倒置的统计日期
        assertThrows(ConstraintViolationException.class, () -> workReportService.getWorkReportStatistics(reqVO, 9L));
        verifyNoInteractions(adminUserApi, deptApi);
    }

    @Test
    public void testGetWorkReportPage_wholeDayBoundary() {
        // mock 数据：开始、结束日期筛选都应包含当天零点及最后一秒
        workReportMapper.insert(randomWorkReportDO().setStartTime(LocalDateTime.of(2026, 9, 7, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 7, 0, 0)));
        workReportMapper.insert(randomWorkReportDO().setStartTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59))
                .setEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59)));
        workReportMapper.insert(randomWorkReportDO().setStartTime(LocalDateTime.of(2026, 9, 8, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 8, 0, 0)));
        // 准备参数：传入带时分秒的时间，仍按所选自然日筛选
        OaWorkReportPageReqVO reqVO = new OaWorkReportPageReqVO()
                .setStartTime(LocalDateTime.of(2026, 9, 7, 12, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 7, 12, 0));

        // 调用，并断言
        assertEquals(2L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
        reqVO.setStartTime(null);
        assertEquals(2L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
        reqVO.setEndTime(null);
        assertEquals(3L, workReportService.getWorkReportPage(reqVO, 1L).getTotal());
    }

    @Test
    public void testGetWorkReportStatistics_naturalDayBoundary() {
        // mock 数据：完整查询时间范围包含首日零点和末日最后一秒
        workReportMapper.insert(randomWorkReportDO().setStatus(2)
                .setStartTime(LocalDateTime.of(2026, 9, 7, 0, 0)));
        workReportMapper.insert(randomWorkReportDO().setStatus(2)
                .setStartTime(LocalDateTime.of(2026, 9, 8, 23, 59, 59)));
        workReportMapper.insert(randomWorkReportDO().setStatus(2)
                .setStartTime(LocalDateTime.of(2026, 9, 9, 0, 0)));
        // 准备参数：即使开始时刻晚于结束时刻，两个自然日都应参与统计
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(1)
                .setStartTime(LocalDateTime.of(2026, 9, 7, 12, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 8, 9, 0))
                .setQueryStartTime(LocalDateTime.of(2026, 9, 7, 0, 0))
                .setQueryEndTime(LocalDateTime.of(2026, 9, 8, 23, 59, 59));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(9L)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(1L).setDeptId(10L).setStatus(0)));
        when(deptApi.getDeptMap(anyCollection())).thenReturn(Collections.emptyMap());

        // 调用
        OaWorkReportStatisticsRespVO result = workReportService.getWorkReportStatistics(reqVO, 9L);

        // 断言
        assertEquals(2, result.getExpectedCount());
        assertEquals(2, result.getSubmittedCount());
        assertEquals(0, result.getMissingCount());
        assertEquals(2, result.getUsers().get(0).getSubmittedReports().size());
    }

    @Test
    public void testGetWorkReportStatistics_weekendReports() {
        // mock 数据：周末已提交报告保留明细，草稿及范围外报告不展示
        OaWorkReportDO report = randomWorkReportDO().setStatus(2)
                .setStartTime(LocalDateTime.of(2026, 3, 22, 0, 0));
        workReportMapper.insert(report);
        workReportMapper.insert(randomWorkReportDO().setStatus(1).setStartTime(report.getStartTime()));
        workReportMapper.insert(randomWorkReportDO().setStatus(2).setStartTime(report.getStartTime().plusDays(1)));
        // 准备参数
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(1)
                .setStartTime(report.getStartTime()).setEndTime(report.getStartTime().plusDays(1).minusSeconds(1))
                .setQueryStartTime(report.getStartTime()).setQueryEndTime(report.getStartTime().plusDays(1).minusSeconds(1));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(9L)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(1L).setDeptId(10L).setStatus(0)));
        when(deptApi.getDeptMap(anyCollection())).thenReturn(Collections.emptyMap());

        // 调用
        OaWorkReportStatisticsRespVO result = workReportService.getWorkReportStatistics(reqVO, 9L);

        // 断言：既有应填周期完成数量不变，周末明细仍可查看
        assertEquals(0, result.getExpectedCount());
        assertEquals(0, result.getSubmittedCount());
        assertEquals(0, result.getMissingCount());
        assertEquals(1, result.getUsers().get(0).getSubmittedReports().size());
        assertEquals(report.getId(), result.getUsers().get(0).getSubmittedReports().get(0).getId());
    }

    @ParameterizedTest
    @CsvSource({"2026-05-01T00:00:00, 1", "2026-05-09T00:00:00, 0", "2099-01-05T00:00:00, 1"})
    public void testGetWorkReportStatistics_selectedDateRange(String date, int expectedCount) {
        // 准备参数：工作日假期仍应填、周六不应填，未来工作日也在所选范围内
        LocalDateTime startTime = LocalDateTime.parse(date);
        LocalDateTime endTime = startTime.plusDays(1).minusSeconds(1);
        OaWorkReportStatisticsReqVO reqVO = new OaWorkReportStatisticsReqVO().setType(1)
                .setStartTime(startTime).setEndTime(endTime).setQueryStartTime(startTime).setQueryEndTime(endTime);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(9L)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(1L).setDeptId(10L).setStatus(0)));
        when(deptApi.getDeptMap(anyCollection())).thenReturn(Collections.emptyMap());

        // 调用
        OaWorkReportStatisticsRespVO result = workReportService.getWorkReportStatistics(reqVO, 9L);

        // 断言
        assertEquals(expectedCount, result.getExpectedCount());
        assertEquals(expectedCount, result.getMissingCount());
        assertEquals(expectedCount, result.getUsers().get(0).getMissingPeriodKeys().size());
    }

    @Test
    public void testCreateWorkReport_invalidWorkItem() {
        // 准备参数
        OaWorkReportSaveReqVO reqVO = BeanUtils.toBean(randomWorkReportDO(), OaWorkReportSaveReqVO.class)
                .setWorkItems(Collections.singletonList(new OaWorkReportSaveReqVO.WorkItem()
                        .setContent("填写工作汇报").setProgress(101)));

        // 调用，并断言：无效工作项在进入新增业务前被拒绝，不生成单号
        assertThrows(ConstraintViolationException.class, () -> workReportService.createWorkReport(reqVO, 1L));
        verifyNoInteractions(noRedisDAO, adminUserApi);
    }

    @Test
    public void testUpdateWorkReport_invalidPlanItem() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO();
        workReportMapper.insert(report);
        // 准备参数
        OaWorkReportSaveReqVO reqVO = BeanUtils.toBean(report, OaWorkReportSaveReqVO.class)
                .setPlanItems(Collections.singletonList(new OaWorkReportSaveReqVO.PlanItem().setContent(" ")));

        // 调用，并断言：拒绝空白计划，不修改已有草稿
        assertThrows(ConstraintViolationException.class, () -> workReportService.updateWorkReport(reqVO, 1L));
        assertTrue(workReportMapper.selectById(report.getId()).getPlanItems().isEmpty());
    }

    @Test
    public void testCreateWorkReport_withItems() {
        // 准备参数
        OaWorkReportSaveReqVO reqVO = BeanUtils.toBean(randomWorkReportDO(), OaWorkReportSaveReqVO.class)
                .setWorkItems(Arrays.asList(new OaWorkReportSaveReqVO.WorkItem().setContent("工作一").setProgress(0),
                        new OaWorkReportSaveReqVO.WorkItem().setContent("工作二").setProgress(100)))
                .setPlanItems(Collections.singletonList(new OaWorkReportSaveReqVO.PlanItem().setContent("下期计划")));
        // mock 方法
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(10L));
        when(noRedisDAO.generate(OaNoRedisDAO.WORK_REPORT_NO_PREFIX)).thenReturn("WR20260913000003");

        // 调用
        Long id = workReportService.createWorkReport(reqVO, 1L);

        // 断言：内嵌 VO 转为 DO 后以 JSON 保存，回读保留内容、进度及顺序
        OaWorkReportDO report = workReportMapper.selectById(id);
        assertEquals(BeanUtils.toBean(reqVO.getWorkItems(), OaWorkReportDO.WorkItem.class), report.getWorkItems());
        assertEquals(BeanUtils.toBean(reqVO.getPlanItems(), OaWorkReportDO.PlanItem.class), report.getPlanItems());
    }

    @Test
    public void testUpdateWorkReport_withItems() {
        // mock 数据
        OaWorkReportDO report = randomWorkReportDO();
        workReportMapper.insert(report);
        // 准备参数
        OaWorkReportSaveReqVO reqVO = BeanUtils.toBean(report, OaWorkReportSaveReqVO.class)
                .setWorkItems(Collections.singletonList(new OaWorkReportSaveReqVO.WorkItem()
                        .setContent("更新工作内容").setProgress(50)))
                .setPlanItems(Arrays.asList(new OaWorkReportSaveReqVO.PlanItem().setContent("计划一"),
                        new OaWorkReportSaveReqVO.PlanItem().setContent("计划二")));

        // 调用
        workReportService.updateWorkReport(reqVO, 1L);

        // 断言
        OaWorkReportDO updatedReport = workReportMapper.selectById(report.getId());
        assertEquals(BeanUtils.toBean(reqVO.getWorkItems(), OaWorkReportDO.WorkItem.class), updatedReport.getWorkItems());
        assertEquals(BeanUtils.toBean(reqVO.getPlanItems(), OaWorkReportDO.PlanItem.class), updatedReport.getPlanItems());
    }

    // ========== 随机对象 ==========

    /**
     * 构造包含已完成工作项的日报草稿。
     *
     * @return 未入库的测试对象
     */
    private static OaWorkReportDO randomWorkReportDO() {
        return randomPojo(OaWorkReportDO.class, report -> report.setId(null)
                .setType(1).setStatus(1).setDeptId(10L)
                .setStartTime(LocalDateTime.of(2026, 9, 7, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 7, 23, 59, 59))
                .setWorkItems(Collections.singletonList(new OaWorkReportDO.WorkItem()
                        .setContent("完成工作汇报迁移").setProgress(100)))
                .setPlanItems(Collections.emptyList()).setFileUrls(Collections.emptyList()).setCreator("1"));
    }
}
