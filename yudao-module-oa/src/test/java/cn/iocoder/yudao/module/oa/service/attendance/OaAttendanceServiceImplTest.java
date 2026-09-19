package cn.iocoder.yudao.module.oa.service.attendance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceMonthReportReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceMonthReportRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendancePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceUpdateReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceWeekReportReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceWeekReportRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.attendance.OaAttendanceDO;
import cn.iocoder.yudao.module.oa.dal.mysql.attendance.OaAttendanceMapper;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceTypeEnum;
import cn.iocoder.yudao.module.oa.framework.config.OaProperties;
import cn.iocoder.yudao.module.oa.service.leave.OaLeaveApplyService;
import cn.iocoder.yudao.module.oa.service.travel.OaTravelApplyService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDateTimeRange;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getMonthDateTimeRange;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_TIME_INVALID;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ATTENDANCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ATTENDANCE_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OaAttendanceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaAttendanceServiceImpl.class, OaProperties.class})
public class OaAttendanceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaAttendanceServiceImpl attendanceService;

    @Resource
    private OaAttendanceMapper attendanceMapper;
    @Resource
    private OaProperties properties;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private OaLeaveApplyService leaveApplyService;
    @MockBean
    private OaTravelApplyService travelApplyService;

    private MockedStatic<LocalDateTime> dateTimeMock;

    @BeforeEach
    public void before() {
        properties.getAttendance().setClockBeginTime(LocalTime.of(5, 0))
                .setWorkBeginTime(LocalTime.of(8, 0)).setWorkEndTime(LocalTime.of(17, 0)).setMonthlyWorkDays(22);
        // 固定打卡时钟，避免测试依赖实际执行时间
        LocalDateTime clockTime = LocalDateTime.of(2026, 9, 5, 10, 0);
        dateTimeMock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
        dateTimeMock.when(LocalDateTime::now).thenReturn(clockTime);
    }

    @AfterEach
    public void tearDown() {
        dateTimeMock.close();
    }

    @Test
    public void testCreateApplyAttendance_detailWithoutDuplicateMonthDays() {
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));
        when(leaveApplyService.getApprovedLeaveDaysMap(Collections.singletonList(1L), getMonthDateTimeRange(2026, 9)))
                .thenReturn(Collections.singletonMap(1L, 2));
        when(travelApplyService.getApprovedTravelDaysMap(Collections.singletonList(1L), getMonthDateTimeRange(2026, 9)))
                .thenReturn(Collections.singletonMap(1L, 3));

        // 调用
        attendanceService.createApplyAttendance(1L, OaAttendanceTypeEnum.LEAVE, LocalDateTime.of(2026, 9, 1, 8, 0));
        attendanceService.createApplyAttendance(1L, OaAttendanceTypeEnum.TRAVEL, LocalDateTime.of(2026, 9, 3, 8, 0));
        // 断言：明细可见；月报仍只统计一次申请天数
        PageResult<OaAttendanceDO> page = attendanceService.getMyAttendancePage(new OaAttendancePageReqVO(), 1L);
        assertEquals(2L, page.getTotal());
        assertEquals(OaAttendanceStatusEnum.TRAVEL.getStatus(), CollUtil.getFirst(page.getList()).getStatus());
        OaAttendanceMonthReportRespVO report = CollUtil.getFirst(attendanceService.getAttendanceMonthReport(
                new OaAttendanceMonthReportReqVO().setYear(2026).setMonth(9), 10L));
        assertEquals(2, report.getLeaveDays());
        assertEquals(3, report.getTravelDays());
        assertEquals(0, report.getClockInCount());
        assertEquals(0, report.getClockOutCount());
        assertEquals(0, report.getNormalCount());
    }

    @Test
    public void testGetAttendanceMonthReport_approvedDaysWithoutClockRecords() {
        // mock 方法：管理范围内的用户没有打卡，但有审批通过的申请
        when(adminUserApi.getUserListBySubordinate(10L)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(1L), new AdminUserRespDTO().setId(2L)));
        List<Long> userIds = Arrays.asList(1L, 2L);
        LocalDateTime[] monthTime = getMonthDateTimeRange(2026, 9);
        when(leaveApplyService.getApprovedLeaveDaysMap(userIds, monthTime)).thenReturn(Collections.singletonMap(1L, 3));
        when(travelApplyService.getApprovedTravelDaysMap(userIds, monthTime)).thenReturn(Collections.singletonMap(1L, 5));
        // 准备参数
        OaAttendanceMonthReportReqVO reqVO = new OaAttendanceMonthReportReqVO().setYear(2026).setMonth(9);

        // 调用
        List<OaAttendanceMonthReportRespVO> reports = attendanceService.getAttendanceMonthReport(reqVO, 10L);

        // 断言：无打卡用户仍展示，未发生请假和出差时填零
        assertEquals(2, reports.size());
        OaAttendanceMonthReportRespVO first = CollUtil.findOne(reports, report -> report.getUserId().equals(1L));
        assertEquals(0, first.getClockInCount());
        assertEquals(3, first.getLeaveDays());
        assertEquals(5, first.getTravelDays());
        OaAttendanceMonthReportRespVO second = CollUtil.findOne(reports, report -> report.getUserId().equals(2L));
        assertEquals(0, second.getLeaveDays());
        assertEquals(0, second.getTravelDays());
        verify(leaveApplyService).getApprovedLeaveDaysMap(userIds, monthTime);
        verify(travelApplyService).getApprovedTravelDaysMap(userIds, monthTime);
    }

    @Test
    public void testClockAttendance_clockInNormal() {
        // 准备参数
        Long userId = 1L;
        properties.getAttendance().setClockBeginTime(LocalTime.MIN).setWorkBeginTime(LocalTime.MAX);
        LocalDateTime beginTime = LocalDateTime.now().withNano(0);

        // 调用
        Long attendanceId = attendanceService.clockAttendance(userId, "127.0.0.1");

        // 断言
        OaAttendanceDO attendance = attendanceMapper.selectById(attendanceId);
        assertEquals(OaAttendanceTypeEnum.CLOCK_IN.getType(), attendance.getType());
        assertEquals(OaAttendanceStatusEnum.NORMAL.getStatus(), attendance.getStatus());
        assertFalse(attendance.getAttendanceTime().isBefore(beginTime));
        assertFalse(attendance.getAttendanceTime().isAfter(LocalDateTime.now().withNano(0)));
    }

    @Test
    public void testClockAttendance_clockInLate() {
        // 准备参数
        properties.getAttendance().setClockBeginTime(LocalTime.MIN).setWorkBeginTime(LocalTime.MIN);

        // 调用
        Long attendanceId = attendanceService.clockAttendance(1L, "127.0.0.1");

        // 断言
        assertEquals(OaAttendanceStatusEnum.LATE.getStatus(),
                attendanceMapper.selectById(attendanceId).getStatus());
    }

    @Test
    public void testClockAttendance_refreshClockOut() {
        // mock 数据
        Long userId = 1L;
        properties.getAttendance().setClockBeginTime(LocalTime.MIN).setWorkEndTime(LocalTime.MAX);
        attendanceService.clockAttendance(userId, "127.0.0.1");
        Long clockOutId = attendanceService.clockAttendance(userId, "127.0.0.1");
        OaAttendanceDO clockOut = attendanceMapper.selectById(clockOutId).setRemark("外出后返回打卡");
        attendanceMapper.updateById(clockOut);

        // 调用
        Long refreshedClockOutId = attendanceService.clockAttendance(userId, "192.168.1.10");

        // 断言
        assertEquals(clockOutId, refreshedClockOutId);
        List<OaAttendanceDO> attendances = attendanceMapper.selectListByUserIdAndAttendanceTime(
                userId, getDateTimeRange(LocalDateTime.now().toLocalDate(), LocalDateTime.now().toLocalDate()));
        assertEquals(2, attendances.size());
        clockOut = attendanceMapper.selectById(clockOutId);
        assertEquals(OaAttendanceStatusEnum.EARLY.getStatus(), clockOut.getStatus());
        assertEquals("外出后返回打卡", clockOut.getRemark());
        assertEquals("192.168.1.10", clockOut.getAttendanceIp());
    }

    @Test
    public void testClockAttendance_beforeClockBeginTime() {
        // 准备参数
        properties.getAttendance().setClockBeginTime(LocalTime.MAX);

        // 调用，并断言异常
        assertServiceException(() -> attendanceService.clockAttendance(1L, "127.0.0.1"),
                ATTENDANCE_CLOCK_TIME_INVALID, LocalTime.MAX, LocalTime.of(17, 0));
    }

    @ParameterizedTest
    @CsvSource({"04:59:59,false", "05:00:00,false", "05:00:01,true",
            "16:59:59,true", "17:00:00,false", "17:00:01,false", "23:59:59,false"})
    public void testClockAttendance_timeBoundary(String time, boolean allowed) {
        // 准备参数
        LocalDateTime clockTime = LocalDate.of(2026, 9, 5).atTime(LocalTime.parse(time));
        dateTimeMock.when(LocalDateTime::now).thenReturn(clockTime);

        // 调用，并断言边界外不新增打卡记录
        if (!allowed) {
            assertServiceException(() -> attendanceService.clockAttendance(1L, "127.0.0.1"),
                    ATTENDANCE_CLOCK_TIME_INVALID, LocalTime.of(5, 0), LocalTime.of(17, 0));
            assertEquals(0L, attendanceMapper.selectCount());
            return;
        }
        Long id = attendanceService.clockAttendance(1L, "127.0.0.1");
        assertEquals(clockTime, attendanceMapper.selectById(id).getAttendanceTime());
    }

    @Test
    public void testClockAttendance_afterEndTimeDoesNotRefreshClockOut() {
        // mock 数据
        OaAttendanceDO clockIn = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 5, 8, 0));
        OaAttendanceDO clockOut = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_OUT,
                OaAttendanceStatusEnum.EARLY, LocalDateTime.of(2026, 9, 5, 16, 0));
        attendanceMapper.insert(clockIn);
        attendanceMapper.insert(clockOut);
        // mock 方法
        LocalDateTime clockTime = LocalDateTime.of(2026, 9, 5, 18, 0);
        dateTimeMock.when(LocalDateTime::now).thenReturn(clockTime);

        // 调用，并断言原有下班记录不被修改
        assertServiceException(() -> attendanceService.clockAttendance(1L, "192.168.1.10"),
                ATTENDANCE_CLOCK_TIME_INVALID, LocalTime.of(5, 0), LocalTime.of(17, 0));
        OaAttendanceDO result = attendanceMapper.selectById(clockOut.getId());
        assertEquals(clockOut.getAttendanceTime(), result.getAttendanceTime());
        assertEquals(clockOut.getAttendanceIp(), result.getAttendanceIp());
        assertEquals(clockOut.getStatus(), result.getStatus());
        assertEquals(2L, attendanceMapper.selectCount());
    }

    @Test
    public void testUpdateAttendance_invalidStatus() {
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // mock 数据
        OaAttendanceDO attendance = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0));
        attendanceMapper.insert(attendance);
        // 准备参数
        OaAttendanceUpdateReqVO reqVO = new OaAttendanceUpdateReqVO().setId(attendance.getId())
                .setStatus(OaAttendanceStatusEnum.EARLY.getStatus());

        // 调用，并断言异常
        assertServiceException(() -> attendanceService.updateAttendance(reqVO, 10L), ATTENDANCE_STATUS_INVALID);
    }

    @Test
    public void testUpdateAttendance_success() {
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // mock 数据
        OaAttendanceDO attendance = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.LATE, LocalDateTime.of(2026, 9, 1, 8, 10)).setRemark("旧备注");
        attendanceMapper.insert(attendance);
        // 准备参数
        OaAttendanceUpdateReqVO reqVO = new OaAttendanceUpdateReqVO().setId(attendance.getId())
                .setStatus(OaAttendanceStatusEnum.NORMAL.getStatus()).setRemark("新备注");

        // 调用
        attendanceService.updateAttendance(reqVO, 10L);

        // 断言
        OaAttendanceDO updatedAttendance = attendanceMapper.selectById(attendance.getId());
        assertEquals(OaAttendanceStatusEnum.NORMAL.getStatus(), updatedAttendance.getStatus());
        assertEquals("新备注", updatedAttendance.getRemark());
    }

    @Test
    public void testGetAttendanceMonthReport_success() {
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // mock 数据
        Long userId = 1L;
        attendanceMapper.insert(randomAttendanceDO(userId, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0)));
        attendanceMapper.insert(randomAttendanceDO(userId, OaAttendanceTypeEnum.CLOCK_OUT,
                OaAttendanceStatusEnum.EARLY, LocalDateTime.of(2026, 9, 1, 16, 30)));
        attendanceMapper.insert(randomAttendanceDO(userId, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.LATE, LocalDateTime.of(2026, 9, 2, 8, 10)));
        // 准备参数
        OaAttendanceMonthReportReqVO reqVO = new OaAttendanceMonthReportReqVO()
                .setYear(2026).setMonth(9).setUserId(userId);

        // 调用
        OaAttendanceMonthReportRespVO report = CollUtil.getFirst(
                attendanceService.getAttendanceMonthReport(reqVO, 10L));

        // 断言
        assertEquals(2, report.getClockInCount());
        assertEquals(1, report.getClockOutCount());
        assertEquals(1, report.getNormalCount());
        assertEquals(1, report.getLateCount());
        assertEquals(1, report.getEarlyCount());
        assertEquals(0, report.getAbsentDays());
    }


    @ParameterizedTest
    @CsvSource({"2026, 8, 0, 22, 22", "2026, 8, 1, 22, 21", "2026, 8, 23, 22, -1",
            "2026, 9, 0, 22, 0", "2026, 10, 0, 22, 0", "2026, 8, 1, 20, 19"})
    public void testGetAttendanceMonthReport_absentDays(int year, int month, int clockOutCount,
                                                       int monthlyWorkDays, int expectedDays) {
        // mock 数据：旷工天数按每月工作天数减下班次数计算，结果允许为负数
        properties.getAttendance().setMonthlyWorkDays(monthlyWorkDays);
        for (int i = 0; i < clockOutCount; i++) {
            attendanceMapper.insert(randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_OUT,
                    OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(year, month, i + 1, 17, 0)));
        }
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // 调用
        OaAttendanceMonthReportRespVO report = CollUtil.getFirst(attendanceService.getAttendanceMonthReport(
                new OaAttendanceMonthReportReqVO().setYear(year).setMonth(month), 10L));
        // 断言
        assertEquals(expectedDays, report.getAbsentDays());
        assertEquals(clockOutCount, report.getClockOutCount());
    }

    @Test
    public void testGetAttendancePage_managedUsersOnly() {
        // mock 数据
        for (Long userId : Arrays.asList(1L, 2L, 10L)) {
            attendanceMapper.insert(randomAttendanceDO(userId, OaAttendanceTypeEnum.CLOCK_IN,
                    OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0)));
        }
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // 调用
        PageResult<OaAttendanceDO> result = attendanceService.getAttendancePage(new OaAttendancePageReqVO(), 10L);

        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(1L, CollUtil.getFirst(result.getList()).getUserId());
        assertEquals(0L, attendanceService.getAttendancePage(
                new OaAttendancePageReqVO().setUserId(2L), 10L).getTotal());
        assertEquals(0L, attendanceService.getAttendancePage(
                new OaAttendancePageReqVO().setUserId(10L), 10L).getTotal());
    }

    @Test
    public void testAttendanceManagement_outsideScope() {
        // mock 数据
        OaAttendanceDO attendance = randomAttendanceDO(2L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0));
        attendanceMapper.insert(attendance);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));
        // 准备参数
        OaAttendanceUpdateReqVO reqVO = new OaAttendanceUpdateReqVO().setId(attendance.getId())
                .setStatus(OaAttendanceStatusEnum.LATE.getStatus());

        // 调用，并断言异常
        assertServiceException(() -> attendanceService.getAttendance(attendance.getId(), 10L),
                ATTENDANCE_NOT_EXISTS);
        assertServiceException(() -> attendanceService.updateAttendance(reqVO, 10L), ATTENDANCE_NOT_EXISTS);
        assertServiceException(() -> attendanceService.deleteAttendance(attendance.getId(), 10L),
                ATTENDANCE_NOT_EXISTS);
        assertEquals(OaAttendanceStatusEnum.NORMAL.getStatus(),
                attendanceMapper.selectById(attendance.getId()).getStatus());
    }

    @Test
    public void testGetAndDeleteAttendance_success() {
        // mock 数据
        OaAttendanceDO attendance = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0));
        attendanceMapper.insert(attendance);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // 调用，并断言
        assertEquals(attendance.getId(), attendanceService.getAttendance(attendance.getId(), 10L).getId());
        attendanceService.deleteAttendance(attendance.getId(), 10L);
        assertNull(attendanceMapper.selectById(attendance.getId()));
    }

    @Test
    public void testAttendanceReports_includeUsersWithoutRecords() {
        // mock 数据
        attendanceMapper.insert(randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0)));
        attendanceMapper.insert(randomAttendanceDO(3L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.LATE, LocalDateTime.of(2026, 9, 1, 9, 0)));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(1L), new AdminUserRespDTO().setId(2L)));
        // 准备参数
        OaAttendanceWeekReportReqVO weekReqVO = new OaAttendanceWeekReportReqVO()
                .setStartDate(LocalDate.of(2026, 9, 1));
        OaAttendanceMonthReportReqVO monthReqVO = new OaAttendanceMonthReportReqVO().setYear(2026).setMonth(9);

        // 调用
        List<OaAttendanceWeekReportRespVO> weeks = attendanceService.getAttendanceWeekReport(weekReqVO, 10L);
        List<OaAttendanceMonthReportRespVO> months = attendanceService.getAttendanceMonthReport(monthReqVO, 10L);

        // 断言
        assertEquals(2, weeks.size());
        assertEquals(2, months.size());
        OaAttendanceWeekReportRespVO emptyWeek = CollUtil.findOne(weeks, row -> row.getUserId().equals(2L));
        assertEquals(7, emptyWeek.getDailyAttendances().size());
        emptyWeek.getDailyAttendances().forEach(day -> {
            assertNull(day.getClockInId());
            assertNull(day.getClockOutId());
            assertNull(day.getClockInTime());
            assertNull(day.getClockOutTime());
        });
        assertEquals(0, CollUtil.findOne(months, row -> row.getUserId().equals(2L)).getClockInCount());
        assertEquals(1, CollUtil.findOne(months, row -> row.getUserId().equals(1L)).getClockInCount());
        assertTrue(attendanceService.getAttendanceWeekReport(weekReqVO.setUserId(3L), 10L).isEmpty());
        assertTrue(attendanceService.getAttendanceMonthReport(monthReqVO.setUserId(3L), 10L).isEmpty());
        assertEquals(1, attendanceService.getAttendanceWeekReport(weekReqVO.setUserId(2L), 10L).size());
    }

    @Test
    public void testAttendanceManagement_noSubordinates() {
        // mock 数据
        attendanceMapper.insert(randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0)));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L)).thenReturn(Collections.emptyList());

        // 调用，并断言
        assertEquals(0L, attendanceService.getAttendancePage(new OaAttendancePageReqVO(), 10L).getTotal());
        assertTrue(attendanceService.getAttendanceWeekReport(
                new OaAttendanceWeekReportReqVO().setStartDate(LocalDate.of(2026, 9, 1)), 10L).isEmpty());
        assertTrue(attendanceService.getAttendanceMonthReport(
                new OaAttendanceMonthReportReqVO().setYear(2026).setMonth(9), 10L).isEmpty());
    }

    @Test
    public void testGetMyAttendancePage_ignoresOtherUserFilter() {
        // mock 数据
        attendanceMapper.insert(randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0)));
        attendanceMapper.insert(randomAttendanceDO(2L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 8, 0)));

        // 调用
        PageResult<OaAttendanceDO> result = attendanceService.getMyAttendancePage(
                new OaAttendancePageReqVO().setUserId(2L), 1L);

        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(1L, CollUtil.getFirst(result.getList()).getUserId());
    }

    @Test
    public void testAttendanceWeekReport_updateRecord() {
        // mock 数据
        OaAttendanceDO clockIn = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_IN,
                OaAttendanceStatusEnum.LATE, LocalDateTime.of(2026, 9, 1, 8, 10));
        OaAttendanceDO clockOut = randomAttendanceDO(1L, OaAttendanceTypeEnum.CLOCK_OUT,
                OaAttendanceStatusEnum.NORMAL, LocalDateTime.of(2026, 9, 1, 18, 0));
        attendanceMapper.insert(clockIn);
        attendanceMapper.insert(clockOut);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(10L))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));
        // 准备参数
        OaAttendanceWeekReportReqVO reqVO = new OaAttendanceWeekReportReqVO()
                .setStartDate(LocalDate.of(2026, 9, 1));

        // 调用
        OaAttendanceWeekReportRespVO report = CollUtil.getFirst(
                attendanceService.getAttendanceWeekReport(reqVO, 10L));
        OaAttendanceWeekReportRespVO.DailyAttendance day = CollUtil.findOne(
                report.getDailyAttendances(), item -> item.getDate().equals(LocalDate.of(2026, 9, 1)));

        // 断言：周报返回对应记录编号，复用修改接口后再次查询可见最新状态
        assertEquals(clockIn.getId(), day.getClockInId());
        assertEquals(clockOut.getId(), day.getClockOutId());
        attendanceService.updateAttendance(new OaAttendanceUpdateReqVO().setId(day.getClockInId())
                .setStatus(OaAttendanceStatusEnum.NORMAL.getStatus()).setRemark("已核实"), 10L);
        OaAttendanceWeekReportRespVO updatedReport = CollUtil.getFirst(
                attendanceService.getAttendanceWeekReport(reqVO, 10L));
        assertEquals(OaAttendanceStatusEnum.NORMAL.getStatus(), CollUtil.findOne(
                updatedReport.getDailyAttendances(), item -> item.getDate().equals(day.getDate())).getClockInStatus());
        assertEquals("已核实", attendanceMapper.selectById(clockIn.getId()).getRemark());
        assertEquals(clockOut.getAttendanceTime(), attendanceMapper.selectById(clockOut.getId()).getAttendanceTime());
    }

    // ========== 随机对象 ==========

    /**
     * 构造具有指定类型、状态和打卡时间的考勤记录。
     *
     * @param userId 用户编号
     * @param type 业务类型
     * @param status 业务状态
     * @param attendanceTime 打卡时间
     * @return 未入库的测试对象
     */
    private static OaAttendanceDO randomAttendanceDO(Long userId, OaAttendanceTypeEnum type,
                                                     OaAttendanceStatusEnum status,
                                                     LocalDateTime attendanceTime) {
        return randomPojo(OaAttendanceDO.class, attendance -> attendance.setId(null).setUserId(userId)
                .setType(type.getType()).setStatus(status.getStatus()).setAttendanceTime(attendanceTime)
                .setAttendanceIp("127.0.0.1").setRemark(null));
    }

}
