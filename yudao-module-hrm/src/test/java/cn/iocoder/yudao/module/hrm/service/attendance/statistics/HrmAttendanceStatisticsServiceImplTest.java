package cn.iocoder.yudao.module.hrm.service.attendance.statistics;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceLateEarlyDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.statistics.HrmAttendanceResultEnum;
import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceGroupService;
import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceHolidayService;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceClockService;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceLeaveService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmAttendanceStatisticsServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class HrmAttendanceStatisticsServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private HrmAttendanceStatisticsServiceImpl attendanceStatisticsService;

    @Mock
    private HrmAttendanceClockService attendanceClockService;
    @Mock
    private HrmEmployeeService employeeService;
    @Mock
    private HrmAttendanceLeaveService attendanceLeaveService;
    @Mock
    private HrmAttendanceGroupService attendanceGroupService;
    @Mock
    private HrmAttendanceHolidayService attendanceHolidayService;

    @Test
    public void testGetAttendanceMonthRecordPage_fullAttendanceFilterUsesMemoryPage() {
        // mock 数据
        List<HrmEmployeeDO> employees = new ArrayList<>();
        for (long id = 1L; id <= 201L; id++) {
            employees.add(new HrmEmployeeDO().setId(id).setName("员工" + id)
                    .setEntryTime(LocalDateTime.of(2026, 1, 1, 9, 0)));
        }
        when(employeeService.getEmployeeList(any(HrmEmployeeListReqVO.class))).thenReturn(employees);
        when(attendanceGroupService.getAttendanceGroupMap(any())).thenReturn(Collections.emptyMap());
        when(attendanceHolidayService.getAttendanceHolidayMap(any())).thenReturn(Collections.emptyMap());
        when(attendanceLeaveService.getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(any(), any()))
                .thenReturn(Collections.emptyList());
        HrmAttendanceMonthRecordPageReqVO reqVO = new HrmAttendanceMonthRecordPageReqVO();
        reqVO.setYear(2026).setMonth(7).setFullAttendance(false);
        reqVO.setPageNo(2).setPageSize(10);

        // 调用
        PageResult<HrmAttendanceMonthRecordRespVO> result =
                attendanceStatisticsService.getAttendanceMonthRecordPage(reqVO);

        // 断言
        assertEquals(201L, result.getTotal());
        assertEquals(10, result.getList().size());
        assertEquals(11L, result.getList().get(0).getEmployeeId());
        assertEquals(20L, result.getList().get(9).getEmployeeId());
        verify(employeeService).getEmployeeList(any(HrmEmployeeListReqVO.class));
    }

    @Test
    public void testGetAttendanceMonthRecordList_actualDaysUsesNetWorkDays() {
        // mock 数据
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(1L).setName("张三")
                .setEntryTime(LocalDate.of(2026, 1, 1).atStartOfDay());
        HrmAttendanceGroupDO attendanceGroup = new HrmAttendanceGroupDO().setId(10L);
        when(employeeService.getEmployeeMap(any()))
                .thenReturn(Collections.singletonMap(employee.getId(), employee));
        when(attendanceGroupService.getAttendanceGroupMap(Collections.singletonList(employee.getId())))
                .thenReturn(Collections.singletonMap(employee.getId(), attendanceGroup));
        when(attendanceGroupService.getAttendanceGroupShiftMap(any(), any(), any()))
                .thenReturn(Collections.singletonMap(attendanceDate, standardShift()));
        when(attendanceHolidayService.getAttendanceHolidayMap(any())).thenReturn(Collections.emptyMap());
        when(attendanceClockService.getAttendanceClockListByEmployeeIdsAndClockTime(any(), any()))
                .thenReturn(Collections.singletonList(
                        clock(attendanceDate.atTime(18, 0), 2).setEmployeeId(employee.getId())));
        when(attendanceLeaveService.getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(any(), any()))
                .thenReturn(Collections.singletonList(
                        leave(attendanceDate.atTime(9, 0), attendanceDate.atTime(13, 0))
                                .setEmployeeId(employee.getId())));

        // 调用
        List<HrmAttendanceMonthRecordRespVO> result = attendanceStatisticsService
                .getAttendanceMonthRecordList(2026, 7, Collections.singletonList(employee.getId()));

        // 断言
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getAttendDays());
        assertDecimal("0.38", result.get(0).getLeaveDays());
        assertDecimal("0.62", result.get(0).getActualDays());
    }

    @Test
    public void testGetAttendanceDailyDetail_distinguishesRestAndNotScheduled() {
        // mock 数据
        LocalDate attendanceDate = LocalDate.of(2026, 7, 12);
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(1L).setName("张三");
        HrmAttendanceGroupDO attendanceGroup = new HrmAttendanceGroupDO().setId(10L);
        when(employeeService.getEmployee(employee.getId())).thenReturn(employee);
        when(attendanceClockService.getAttendanceClockListByEmployeeIdAndClockTime(any(), any()))
                .thenReturn(Collections.emptyList());
        when(attendanceLeaveService.getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(any(), any()))
                .thenReturn(Collections.emptyList());
        when(attendanceHolidayService.getAttendanceHolidayMap(any())).thenReturn(Collections.emptyMap());
        when(attendanceGroupService.getMyAttendanceGroup(employee.getId()))
                .thenReturn(attendanceGroup)
                .thenReturn(null);
        when(attendanceGroupService.getAttendanceGroupShiftMap(any(), any(), any()))
                .thenReturn(Collections.singletonMap(attendanceDate, null));
        HrmAttendanceDailyDetailReqVO reqVO = new HrmAttendanceDailyDetailReqVO()
                .setEmployeeId(employee.getId()).setAttendanceTime(attendanceDate.atStartOfDay());

        // 调用
        HrmAttendanceDailyDetailRespVO restResult = attendanceStatisticsService.getAttendanceDailyDetail(reqVO);
        HrmAttendanceDailyDetailRespVO notScheduledResult =
                attendanceStatisticsService.getAttendanceDailyDetail(reqVO);

        // 断言
        assertEquals(HrmAttendanceResultEnum.REST.getFormat(), restResult.getAttendanceResult());
        assertEquals(HrmAttendanceResultEnum.NOT_SCHEDULED.getFormat(), notScheduledResult.getAttendanceResult());
    }

    @Test
    public void testGetAttendanceDailyDetail_includesCrossDayClockAndLeave() {
        // mock 数据
        LocalDate attendanceDate = LocalDate.of(2026, 7, 31);
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(1L).setName("张三");
        HrmAttendanceGroupDO attendanceGroup = new HrmAttendanceGroupDO().setId(10L);
        HrmAttendanceGroupDO.Shift shift = HrmAttendanceGroupDO.Shift.builder()
                .startTime(LocalTime.of(22, 0)).endTime(LocalTime.of(6, 0)).build();
        HrmAttendanceClockDO offDutyClock = clock(attendanceDate.plusDays(1).atTime(6, 0), 2)
                .setAttendanceTime(attendanceDate.plusDays(1).atTime(6, 0));
        HrmAttendanceLeaveDO leave = leave(
                attendanceDate.atTime(22, 0), attendanceDate.plusDays(1).atTime(3, 0));
        when(employeeService.getEmployee(employee.getId())).thenReturn(employee);
        when(attendanceGroupService.getMyAttendanceGroup(employee.getId())).thenReturn(attendanceGroup);
        when(attendanceHolidayService.getAttendanceHolidayMap(any())).thenReturn(Collections.emptyMap());
        when(attendanceGroupService.getAttendanceGroupShiftMap(any(), any(), any()))
                .thenReturn(Collections.singletonMap(attendanceDate, shift));
        when(attendanceClockService.getAttendanceClockListByEmployeeIdAndClockTime(any(), any()))
                .thenReturn(Collections.singletonList(offDutyClock));
        when(attendanceLeaveService.getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(any(), any()))
                .thenReturn(Collections.singletonList(leave));
        HrmAttendanceDailyDetailReqVO reqVO = new HrmAttendanceDailyDetailReqVO()
                .setEmployeeId(employee.getId()).setAttendanceTime(attendanceDate.atStartOfDay());

        // 调用
        HrmAttendanceDailyDetailRespVO result = attendanceStatisticsService.getAttendanceDailyDetail(reqVO);

        // 断言
        assertEquals(1, result.getClockCount());
        assertEquals(300, result.getLeaveMinutes());
        assertDecimal("0.63", result.getLeaveDays());
        ArgumentCaptor<LocalDateTime[]> leaveTimesCaptor = ArgumentCaptor.forClass(LocalDateTime[].class);
        verify(attendanceLeaveService).getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(
                any(), leaveTimesCaptor.capture());
        assertEquals(attendanceDate.atTime(22, 0), leaveTimesCaptor.getValue()[0]);
        assertEquals(attendanceDate.plusDays(1).atTime(6, 0), leaveTimesCaptor.getValue()[1]);
    }

    @Test
    public void testGetAttendanceDate_crossDayOffDutyBelongsToShiftBeginDate() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 31);
        HrmAttendanceGroupDO.Shift shift = HrmAttendanceGroupDO.Shift.builder()
                .startTime(LocalTime.of(22, 0)).endTime(LocalTime.of(6, 0)).build();
        HrmAttendanceClockDO clock = clock(attendanceDate.plusDays(1).atTime(6, 0), 2)
                .setAttendanceTime(attendanceDate.plusDays(1).atTime(6, 0));

        // 调用
        LocalDate result = attendanceStatisticsService.getAttendanceDate(
                clock, Collections.singletonMap(attendanceDate, shift));

        // 断言
        assertEquals(attendanceDate, result);
    }

    @Test
    public void testGetAttendanceDetail_employeeNotExists() {
        // 准备参数
        HrmAttendanceDailyDetailReqVO dailyReqVO = new HrmAttendanceDailyDetailReqVO()
                .setEmployeeId(1L).setAttendanceTime(LocalDateTime.of(2026, 8, 1, 0, 0));

        // 调用，并断言
        assertNull(attendanceStatisticsService.getAttendanceDailyDetail(dailyReqVO));
        assertNull(attendanceStatisticsService.getAttendanceMonthDetail(1L, 2026, 8));
    }

    @Test
    public void testGetAttendanceDate_normalOffDutyKeepsNaturalDate() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 31);
        HrmAttendanceClockDO clock = clock(attendanceDate.atTime(18, 0), 2)
                .setAttendanceTime(attendanceDate.atTime(18, 0));

        // 调用
        LocalDate result = attendanceStatisticsService.getAttendanceDate(
                clock, Collections.singletonMap(attendanceDate, standardShift()));

        // 断言
        assertEquals(attendanceDate, result);
    }

    @Test
    public void testCalculateAttendanceDay_ignoresLegacyMultipleStages() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 12);
        HrmAttendanceClockDO firstStageClock = clock(attendanceDate.atTime(9, 0), 1);
        HrmAttendanceClockDO legacySecondStageClock = clock(attendanceDate.atTime(12, 0), 1).setStage(2);

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, null,
                        Arrays.asList(firstStageClock, legacySecondStageClock), Collections.emptyList(), null);

        // 断言
        assertEquals(1, result.getActualClockCount());
    }

    @Test
    public void testCalculateAttendanceDay_doesNotMissCardBeforeClosedClockDeadline() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用：打卡窗口结束边界仍允许打卡
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, standardShift(),
                        Collections.emptyList(), Collections.emptyList(), attendanceDate.atTime(9, 30));

        // 断言
        assertEquals(0, result.getMisscardCount());
        assertEquals(HrmAttendanceResultEnum.PENDING_CLOCK.getFormat(), result.getAttendanceResult());
    }

    @Test
    public void testCalculateAttendanceDay_marksMissCardAfterClockDeadline() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, standardShift(),
                        Collections.emptyList(), Collections.emptyList(),
                        attendanceDate.atTime(9, 30, 0, 1));

        // 断言
        assertEquals(1, result.getMisscardCount());
        assertEquals("缺卡 1 次", result.getAttendanceResult());
    }

    @Test
    public void testCalculateAttendanceDay_doesNotSettleBeforeClosedOffDutyDeadline() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用：已有上班卡，但下班打卡窗口尚未结束
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, standardShift(),
                        Collections.singletonList(clock(attendanceDate.atTime(9, 0), 1)),
                        Collections.emptyList(), attendanceDate.atTime(18, 30));

        // 断言
        assertEquals(0, result.getMisscardCount());
        assertEquals(HrmAttendanceResultEnum.PENDING_CLOCK.getFormat(), result.getAttendanceResult());
    }

    @Test
    public void testCalculateAttendanceDay_partialMorningLeave() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, standardShift(),
                        Collections.singletonList(clock(attendanceDate.atTime(18, 0), 2)),
                        Collections.singletonList(leave(
                                attendanceDate.atTime(9, 0), attendanceDate.atTime(13, 0))),
                        attendanceDate.plusDays(1).atStartOfDay());

        // 断言
        assertEquals(480, result.getScheduledMinutes());
        assertEquals(180, result.getLeaveMinutes());
        assertDecimal("0.38", result.getLeaveDays());
        assertEquals(0, result.getMisscardCount());
        assertEquals(0, result.getAbsenteeismMinutes());
        assertEquals("请假 0.38 天", result.getAttendanceResult());
    }

    @Test
    public void testCalculateAttendanceDay_lateAndEarlySubtractLeave() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, standardShift(),
                        Arrays.asList(clock(attendanceDate.atTime(10, 0), 1),
                                clock(attendanceDate.atTime(17, 0), 2)),
                        Arrays.asList(leave(attendanceDate.atTime(9, 0), attendanceDate.atTime(9, 30)),
                                leave(attendanceDate.atTime(17, 30), attendanceDate.atTime(18, 0))),
                        attendanceDate.plusDays(1).atStartOfDay());

        // 断言
        assertEquals(30, result.getLateMinutes());
        assertEquals(1, result.getLateCount());
        assertEquals(30, result.getEarlyMinutes());
        assertEquals(1, result.getEarlyCount());
        assertEquals(60, result.getLeaveMinutes());
        assertEquals(0, result.getMisscardCount());
    }

    @Test
    public void testCalculateAttendanceDay_partialLeaveWithoutClocks() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(
                        attendanceDate, standardShift(), Collections.emptyList(),
                        Collections.singletonList(leave(
                                attendanceDate.atTime(9, 0), attendanceDate.atTime(11, 0))),
                        attendanceDate.plusDays(1).atStartOfDay());

        // 断言
        assertEquals(120, result.getLeaveMinutes());
        assertDecimal("0.25", result.getLeaveDays());
        assertEquals(360, result.getAbsenteeismMinutes());
        assertDecimal("0.75", result.getAbsenteeismDays());
        assertEquals(0, result.getMisscardCount());
        assertEquals("请假 0.25 天 / 旷工 0.75 天", result.getAttendanceResult());
    }

    @Test
    public void testCalculateAttendanceDay_overlappingLeaveIntervals() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(
                        attendanceDate, standardShift(), Collections.emptyList(),
                        Arrays.asList(leave(attendanceDate.atTime(9, 0), attendanceDate.atTime(11, 0)),
                                leave(attendanceDate.atTime(10, 0), attendanceDate.atTime(12, 0))),
                        attendanceDate.plusDays(1).atStartOfDay());

        // 断言
        assertEquals(180, result.getLeaveMinutes());
        assertDecimal("0.38", result.getLeaveDays());
        assertEquals(300, result.getAbsenteeismMinutes());
    }

    @Test
    public void testCalculateAttendanceDay_crossDayShift() {
        // 准备参数
        LocalDate attendanceDate = LocalDate.of(2026, 7, 8);
        HrmAttendanceGroupDO.Shift shift = HrmAttendanceGroupDO.Shift.builder()
                .startTime(LocalTime.of(22, 0)).endTime(LocalTime.of(6, 0))
                .excludeRestTime(true).restStartTime(LocalTime.of(2, 0)).restEndTime(LocalTime.of(3, 0)).build();

        // 调用
        HrmAttendanceStatisticsServiceImpl.AttendanceDayCalculation result =
                attendanceStatisticsService.calculateAttendanceDay(attendanceDate, shift,
                        Collections.singletonList(clock(attendanceDate.plusDays(1).atTime(6, 0), 2)),
                        Collections.singletonList(leave(
                                attendanceDate.atTime(22, 0), attendanceDate.plusDays(1).atTime(3, 0))),
                        attendanceDate.plusDays(2).atStartOfDay());

        // 断言
        assertEquals(420, result.getScheduledMinutes());
        assertEquals(240, result.getLeaveMinutes());
        assertDecimal("0.57", result.getLeaveDays());
        assertEquals(0, result.getMisscardCount());
        assertEquals(0, result.getAbsenteeismMinutes());
    }

    @Test
    public void testSetAttendanceDeductAmounts_configuredRule() {
        // 准备参数
        HrmAttendanceMonthRecordRespVO respVO = new HrmAttendanceMonthRecordRespVO()
                .setLateCount(3).setLateMinute(20)
                .setEarlyCount(2).setEarlyMinute(10)
                .setAbsenteeismDays(new BigDecimal("0.50")).setMisscardCount(2);
        HrmAttendanceGroupDO.DeductRule deductRule = HrmAttendanceGroupDO.DeductRule.builder()
                .lateMethod(HrmAttendanceLateEarlyDeductMethodEnum.BY_COUNT.getMethod())
                .lateDeductMoney(new BigDecimal("5.00"))
                .earlyMethod(HrmAttendanceLateEarlyDeductMethodEnum.BY_MINUTE.getMethod())
                .earlyDeductMoney(new BigDecimal("2.00"))
                .absenteeismMethod(1).absenteeismDeductMoney(new BigDecimal("100.00"))
                .misscardMethod(1).misscardDeductMoney(new BigDecimal("8.00"))
                .build();

        // 调用
        attendanceStatisticsService.setAttendanceDeductAmounts(respVO, deductRule);

        // 断言
        assertDecimal("15.00", respVO.getLateDeductAmount());
        assertDecimal("20.00", respVO.getEarlyDeductAmount());
        assertDecimal("50.00", respVO.getAbsenteeismDeductAmount());
        assertDecimal("16.00", respVO.getMisscardDeductAmount());
        assertDecimal("101.00", respVO.getAttendanceDeductAmount());
    }

    @Test
    public void testSetAttendanceDeductAmounts_fixedMonthWithoutEarlyEvent() {
        // 准备参数
        HrmAttendanceMonthRecordRespVO respVO = new HrmAttendanceMonthRecordRespVO()
                .setLateCount(1).setLateMinute(5)
                .setEarlyCount(0).setEarlyMinute(0)
                .setAbsenteeismDays(BigDecimal.ZERO).setMisscardCount(0);
        HrmAttendanceGroupDO.DeductRule deductRule = HrmAttendanceGroupDO.DeductRule.builder()
                .lateMethod(HrmAttendanceLateEarlyDeductMethodEnum.FIXED_MONTH.getMethod())
                .lateDeductMoney(new BigDecimal("30.00"))
                .earlyMethod(HrmAttendanceLateEarlyDeductMethodEnum.FIXED_MONTH.getMethod())
                .earlyDeductMoney(new BigDecimal("20.00"))
                .absenteeismMethod(1).absenteeismDeductMoney(BigDecimal.ZERO)
                .misscardMethod(1).misscardDeductMoney(BigDecimal.ZERO)
                .build();

        // 调用
        attendanceStatisticsService.setAttendanceDeductAmounts(respVO, deductRule);

        // 断言
        assertDecimal("30.00", respVO.getLateDeductAmount());
        assertDecimal("20.00", respVO.getEarlyDeductAmount());
        assertDecimal("50.00", respVO.getAttendanceDeductAmount());
    }

    private static HrmAttendanceGroupDO.Shift standardShift() {
        return HrmAttendanceGroupDO.Shift.builder()
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0))
                .clockInStartTime(LocalTime.of(8, 0)).clockInEndTime(LocalTime.of(9, 30))
                .clockOutStartTime(LocalTime.of(17, 30)).clockOutEndTime(LocalTime.of(18, 30))
                .excludeRestTime(true).restStartTime(LocalTime.of(12, 0)).restEndTime(LocalTime.of(13, 0)).build();
    }

    private static HrmAttendanceClockDO clock(LocalDateTime clockTime, int type) {
        return HrmAttendanceClockDO.builder().clockTime(clockTime).type(type).stage(1).build();
    }

    private static HrmAttendanceLeaveDO leave(LocalDateTime startTime, LocalDateTime endTime) {
        return HrmAttendanceLeaveDO.builder().startTime(startTime).endTime(endTime).build();
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

}
