package cn.iocoder.yudao.module.hrm.service.attendance.statistics;

import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceGroupService;
import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceHolidayService;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceClockService;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceLeaveService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.TimeRange;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyOverviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthDailyOverviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStageEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceLateEarlyDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.statistics.HrmAttendanceResultEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceAdd;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceMultiply;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceScale;

/**
 * HRM 考勤统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmAttendanceStatisticsServiceImpl implements HrmAttendanceStatisticsService {

    @Resource
    private HrmAttendanceClockService attendanceClockService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmAttendanceLeaveService attendanceLeaveService;
    @Resource
    private HrmAttendanceGroupService attendanceGroupService;
    @Resource
    private HrmAttendanceHolidayService attendanceHolidayService;

    @Override
    public HrmAttendanceDailyDetailRespVO getAttendanceDailyDetail(HrmAttendanceDailyDetailReqVO reqVO) {
        // 1.1 查询员工
        HrmEmployeeDO employee = employeeService.getEmployee(reqVO.getEmployeeId());
        if (employee == null) {
            return null;
        }
        LocalDate attendanceDate = reqVO.getAttendanceTime().toLocalDate();
        // 1.2 查询员工考勤组和相邻两日班次，用于识别跨日班次的业务日期
        HrmAttendanceGroupDO attendanceGroup = attendanceGroupService.getMyAttendanceGroup(employee.getId());
        Map<LocalDate, HrmAttendanceGroupDO.Shift> shiftMap = getEmployeeShiftMap(
                attendanceGroup, attendanceDate.minusDays(1), attendanceDate);
        HrmAttendanceGroupDO.Shift shift = shiftMap.get(attendanceDate);
        // 1.3 查询并归集当前业务日的打卡记录
        List<HrmAttendanceClockDO> clockList = getDateClockListMap(
                getEmployeeClockListForAttendanceTimes(employee.getId(),
                        LocalDateTimeUtils.getDateTimeRange(attendanceDate, attendanceDate)), shiftMap)
                .getOrDefault(attendanceDate, Collections.emptyList());
        // 1.4 查询覆盖当前班次的有效请假记录
        TimeRange shiftTimeRange = buildShiftTimeRange(attendanceDate, shift);
        LocalDateTime[] leaveTimes = shiftTimeRange == null
                ? LocalDateTimeUtils.getDateTimeRange(attendanceDate, attendanceDate)
                : new LocalDateTime[]{shiftTimeRange.getStartTime(), shiftTimeRange.getEndTime()};
        List<HrmAttendanceLeaveDO> leaveList = attendanceLeaveService
                .getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(
                        Collections.singleton(employee.getId()), leaveTimes);

        // 2. 计算单日考勤明细
        return buildDailyDetail(employee, attendanceDate, clockList,
                attendanceGroup != null, shift, leaveList, LocalDateTime.now());
    }

    @Override
    public PageResult<HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordPage(
            HrmAttendanceMonthRecordPageReqVO reqVO) {
        // 1. 未筛选全勤时，分页查询员工并计算月度汇总
        if (reqVO.getFullAttendance() == null) {
            PageResult<HrmEmployeeDO> employeePage = employeeService.getEmployeePage(buildEmployeePageReqVO(reqVO));
            List<HrmAttendanceMonthRecordRespVO> monthSummaryList = buildMonthSummaryList(
                    reqVO.getYear(), reqVO.getMonth(), employeePage.getList());
            return new PageResult<>(monthSummaryList, employeePage.getTotal());
        }

        // 2. 筛选全勤时，计算全部员工的月度汇总并进行内存分页
        List<HrmEmployeeDO> employeeList = getAttendanceEmployeeList(reqVO);
        List<HrmAttendanceMonthRecordRespVO> monthSummaryList =
                buildMonthSummaryList(reqVO.getYear(), reqVO.getMonth(), employeeList);
        List<HrmAttendanceMonthRecordRespVO> filteredMonthSummaryList = filterList(monthSummaryList,
                record -> Objects.equals(record.getFullAttendance(), reqVO.getFullAttendance()));
        return PageUtils.buildPageResult(filteredMonthSummaryList, reqVO);
    }

    @Override
    public List<HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordList(HrmAttendanceMonthRecordPageReqVO reqVO) {
        // 1. 计算员工月度考勤汇总
        List<HrmEmployeeDO> employeeList = getAttendanceEmployeeList(reqVO);
        List<HrmAttendanceMonthRecordRespVO> monthSummaryList =
                buildMonthSummaryList(reqVO.getYear(), reqVO.getMonth(), employeeList);

        // 2. 按需过滤全勤条件
        if (reqVO.getFullAttendance() == null) {
            return monthSummaryList;
        }
        return filterList(monthSummaryList,
                record -> Objects.equals(record.getFullAttendance(), reqVO.getFullAttendance()));
    }

    @Override
    public List<HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordList(
            Integer year, Integer month, List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        // 1. 查询员工列表
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        List<HrmEmployeeDO> employeeList = filterAttendanceEmployeeList(
                year, month, convertList(employeeIds, employeeMap::get));

        // 2. 计算员工月度考勤汇总
        return buildMonthSummaryList(year, month, employeeList);
    }

    @Override
    public PageResult<HrmAttendanceMonthDailyOverviewRespVO> getAttendanceMonthDailyOverviewPage(
            HrmAttendanceMonthRecordPageReqVO reqVO) {
        // 1. 分页查询员工
        PageResult<HrmEmployeeDO> employeePage = employeeService.getEmployeePage(buildEmployeePageReqVO(reqVO));

        // 2. 计算员工月度打卡概况
        List<HrmAttendanceMonthDailyOverviewRespVO> monthDailyOverviewList = buildMonthDailyOverviewList(
                reqVO.getYear(), reqVO.getMonth(), employeePage.getList());
        return new PageResult<>(monthDailyOverviewList, employeePage.getTotal());
    }

    @Override
    public List<HrmAttendanceMonthDailyOverviewRespVO> getAttendanceMonthDailyOverviewList(
            HrmAttendanceMonthRecordPageReqVO reqVO) {
        // 1. 查询员工列表
        List<HrmEmployeeDO> employeeList = getAttendanceEmployeeList(reqVO);

        // 2. 计算员工月度打卡概况
        return buildMonthDailyOverviewList(reqVO.getYear(), reqVO.getMonth(), employeeList);
    }

    @Override
    public HrmAttendanceMonthDetailRespVO getAttendanceMonthDetail(
            Long employeeId, Integer year, Integer month) {
        // 1.1 查询员工
        HrmEmployeeDO employee = employeeService.getEmployee(employeeId);
        if (employee == null) {
            return null;
        }
        // 1.2 查询员工当月的考勤关联数据
        AttendanceMonthData monthData =
                getAttendanceMonthData(year, month, Collections.singletonList(employee));

        // 2. 拼接月度考勤详情
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(year, month);
        List<HrmAttendanceLeaveDO> monthLeaveList = filterList(
                monthData.getEmployeeLeaveList(employee.getId()),
                leave -> LocalDateTimeUtils.isClosedRangeOverlap(
                        leave.getStartTime(), leave.getEndTime(),
                        monthTimes[0], monthTimes[1]));
        List<HrmAttendanceLeaveRespVO> leaveList = convertList(monthLeaveList, leave ->
                BeanUtils.toBean(leave, HrmAttendanceLeaveRespVO.class)
                        .setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                        .setDeptId(employee.getDeptId()).setPostName(employee.getPostName()));
        return new HrmAttendanceMonthDetailRespVO()
                .setSummary(buildMonthSummary(employee, monthData))
                .setDailyDetails(buildMonthDailyDetails(employee, monthData))
                .setLeaves(leaveList);
    }

    // ==================== 排班解析 ====================

    /**
     * 获得员工指定日期范围的有效班次
     *
     * @param attendanceGroup 考勤组
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 日期对应班次 Map
     */
    private Map<LocalDate, HrmAttendanceGroupDO.Shift> getEmployeeShiftMap(
            HrmAttendanceGroupDO attendanceGroup, LocalDate beginDate, LocalDate endDate) {
        if (attendanceGroup == null) {
            return Collections.emptyMap();
        }
        Map<LocalDate, HrmAttendanceHolidayDO> holidayMap = attendanceHolidayService
                .getAttendanceHolidayMap(LocalDateTimeUtils.getDateTimeRange(
                        beginDate, endDate));
        return attendanceGroupService.getAttendanceGroupShiftMap(attendanceGroup,
                LocalDateTimeUtils.getDateList(beginDate,
                        Math.toIntExact(ChronoUnit.DAYS.between(beginDate, endDate)) + 1),
                holidayMap);
    }

    private List<HrmAttendanceDailyDetailRespVO> buildMonthDailyDetails(
            HrmEmployeeDO employee, AttendanceMonthData monthData) {
        Map<LocalDate, List<HrmAttendanceClockDO>> dateClockListMap =
                getDateClockListMap(monthData.getEmployeeClockList(employee.getId()),
                        monthData.getEmployeeShiftMap(employee.getId()));
        Map<LocalDate, HrmAttendanceGroupDO.Shift> shiftMap =
                monthData.getEmployeeShiftMap(employee.getId());
        List<HrmAttendanceLeaveDO> leaveList = monthData.getEmployeeLeaveList(employee.getId());
        return convertList(monthData.getAttendanceDateList(),
                attendanceDate -> buildDailyDetail(employee, attendanceDate,
                        dateClockListMap.getOrDefault(attendanceDate, Collections.emptyList()),
                        monthData.getEmployeeAttendanceGroup(employee.getId()) != null,
                        shiftMap.get(attendanceDate), leaveList, monthData.getCalculationTime()));
    }

    /**
     * 构建员工单日考勤明细
     *
     * @param employee 员工
     * @param attendanceDate 考勤日期
     * @param clockList 打卡记录
     * @param attendanceGroupConfigured 是否已配置考勤组
     * @param shift 班次
     * @param leaveList 请假记录
     * @param calculationTime 统一计算时间
     * @return 单日考勤明细
     */
    private HrmAttendanceDailyDetailRespVO buildDailyDetail(HrmEmployeeDO employee, LocalDate attendanceDate,
                                                            List<HrmAttendanceClockDO> clockList,
                                                            boolean attendanceGroupConfigured,
                                                            HrmAttendanceGroupDO.Shift shift,
                                                            List<HrmAttendanceLeaveDO> leaveList,
                                                            LocalDateTime calculationTime) {
        // 1. 设置员工和排班基础信息
        HrmAttendanceDailyDetailRespVO respVO = new HrmAttendanceDailyDetailRespVO();
        respVO.setEmployeeId(employee.getId()).setEmployeeName(employee.getName())
                .setJobNumber(employee.getJobNumber()).setDeptId(employee.getDeptId())
                .setPostName(employee.getPostName()).setAttendanceTime(attendanceDate.atStartOfDay());
        if (shift != null) {
            respVO.setShiftName(String.format("%s-%s", shift.getStartTime(), shift.getEndTime()));
        }

        // 2. 计算并设置单日考勤结果
        boolean scheduledWorkday = isScheduledWorkday(employee, attendanceDate, shift);
        AttendanceDayCalculation calculation = scheduledWorkday
                ? calculateAttendanceDay(attendanceDate, shift, clockList, leaveList, calculationTime)
                : AttendanceDayCalculation.rest(getActualClockCount(clockList));
        respVO.setScheduled(scheduledWorkday)
                .setRequiredClockCount(scheduledWorkday ? 2 : 0)
                .setScheduledMinutes(calculation.getScheduledMinutes())
                .setMisscardCount(calculation.getMisscardCount());
        respVO.setAbsenteeism(calculation.getAbsenteeismMinutes() > 0)
                .setAbsenteeismMinutes(calculation.getAbsenteeismMinutes())
                .setAbsenteeismDays(calculation.getAbsenteeismDays());
        respVO.setLeaveStatus(calculation.getLeaveMinutes() > 0)
                .setLeaveMinutes(calculation.getLeaveMinutes())
                .setLeaveDays(calculation.getLeaveDays());
        respVO.setAttendanceResult(calculation.getAttendanceResult())
                .setClockCount(calculation.getActualClockCount());
        if (shift == null && !attendanceGroupConfigured) {
            respVO.setAttendanceResult(HrmAttendanceResultEnum.NOT_SCHEDULED.getFormat());
        }

        // 3. 设置迟到和早退统计
        if (scheduledWorkday) {
            respVO.setLateCount(calculation.getLateCount()).setLateMinutes(calculation.getLateMinutes())
                    .setEarlyCount(calculation.getEarlyCount()).setEarlyMinutes(calculation.getEarlyMinutes());
        } else {
            respVO.setLateCount(countClockByStatus(clockList, HrmAttendanceClockStatusEnum.LATE.getStatus()))
                    .setLateMinutes(sumClockExceptionMinutesByStatus(
                            clockList, HrmAttendanceClockStatusEnum.LATE.getStatus()))
                    .setEarlyCount(countClockByStatus(clockList, HrmAttendanceClockStatusEnum.EARLY.getStatus()))
                    .setEarlyMinutes(sumClockExceptionMinutesByStatus(
                            clockList, HrmAttendanceClockStatusEnum.EARLY.getStatus()));
        }

        // 4. 排序并设置打卡记录
        List<HrmAttendanceClockDO> sortedClockList = new ArrayList<>(clockList);
        sortedClockList.sort(Comparator.comparing(HrmAttendanceClockDO::getClockTime));
        respVO.setClockList(convertList(sortedClockList, clock -> {
            HrmAttendanceClockRespVO clockRespVO = BeanUtils.toBean(clock, HrmAttendanceClockRespVO.class);
            clockRespVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                    .setDeptId(employee.getDeptId()).setPostName(employee.getPostName());
            return clockRespVO;
        }));
        return respVO;
    }

    // ==================== 单日考勤计算 ====================

    /**
     * 计算员工单日考勤结果
     *
     * @param attendanceDate 考勤日期
     * @param shift 班次
     * @param clockList 打卡记录
     * @param leaveList 请假记录
     * @param calculationTime 计算时间
     * @return 单日考勤结果
     */
    @VisibleForTesting
    AttendanceDayCalculation calculateAttendanceDay(LocalDate attendanceDate,
                                                     HrmAttendanceGroupDO.Shift shift,
                                                     List<HrmAttendanceClockDO> clockList,
                                                     List<HrmAttendanceLeaveDO> leaveList,
                                                     LocalDateTime calculationTime) {
        calculationTime = calculationTime == null ? LocalDateTime.now() : calculationTime;
        if (shift == null) {
            return AttendanceDayCalculation.rest(getActualClockCount(clockList));
        }
        TimeRange shiftTimeRange = buildShiftTimeRange(attendanceDate, shift);
        if (shiftTimeRange == null) {
            return AttendanceDayCalculation.rest(getActualClockCount(clockList));
        }

        // 1.1 扣除班次休息时间，得到实际应工作范围
        List<TimeRange> workRanges = buildWorkTimeRanges(shiftTimeRange, shift);
        int scheduledMinutes = LocalDateTimeUtils.calculateDurationMinutes(workRanges);
        // 1.2 过滤无效请假记录，并计算请假覆盖的工作范围
        List<HrmAttendanceLeaveDO> validLeaveList = filterList(leaveList,
                leave -> LocalDateTimeUtils.isTimeRangeValid(leave.getStartTime(), leave.getEndTime()));
        List<TimeRange> leaveWorkRanges = buildLeaveWorkRanges(validLeaveList, workRanges);
        int leaveMinutes = LocalDateTimeUtils.calculateDurationMinutes(leaveWorkRanges);
        BigDecimal leaveDays = calculateDayRatio(leaveMinutes, scheduledMinutes);

        // 2.1 匹配上、下班有效打卡记录
        int lateCount = 0;
        int lateMinutes = 0;
        int earlyCount = 0;
        int earlyMinutes = 0;
        int misscardCount = 0;
        int absenteeismMinutes = 0;
        HrmAttendanceClockDO onDutyClock = findClockByType(
                clockList, HrmAttendanceClockTypeEnum.ON_DUTY.getType());
        HrmAttendanceClockDO offDutyClock = findClockByType(
                clockList, HrmAttendanceClockTypeEnum.OFF_DUTY.getType());
        int uncoveredMinutes = Math.max(scheduledMinutes - leaveMinutes, 0);
        LocalDateTime clockInDeadline = getClockDeadline(attendanceDate,
                shift.getClockInStartTime(), shift.getClockInEndTime(), shiftTimeRange.getStartTime());
        LocalDateTime clockOutDeadline = getClockDeadline(attendanceDate,
                shift.getClockOutStartTime(), shift.getClockOutEndTime(), shiftTimeRange.getEndTime());
        // 2.2 下班打卡窗口结束且全天无打卡时，未请假时长计为旷工
        if (onDutyClock == null && offDutyClock == null
                && calculationTime.isAfter(clockOutDeadline)
                && uncoveredMinutes > 0) {
            absenteeismMinutes = uncoveredMinutes;
        } else {
            // 2.3 对应打卡窗口结束、没有打卡且未请假时，计为缺卡
            if (onDutyClock == null
                    && calculationTime.isAfter(clockInDeadline)
                    && isAttendanceRequiredAt(validLeaveList, shiftTimeRange.getStartTime())) {
                misscardCount++;
            }
            if (offDutyClock == null
                    && calculationTime.isAfter(clockOutDeadline)
                    && isAttendanceRequiredAt(validLeaveList, shiftTimeRange.getEndTime())) {
                misscardCount++;
            }
            // 2.4 打卡晚于上班时间时，扣除休息及请假范围后计为迟到
            if (onDutyClock != null && onDutyClock.getClockTime() != null
                    && onDutyClock.getClockTime().isAfter(shiftTimeRange.getStartTime())) {
                int minutes = calculateEffectiveExceptionMinutes(
                        shiftTimeRange.getStartTime(), onDutyClock.getClockTime(), workRanges, leaveWorkRanges);
                if (minutes > 0) {
                    lateCount++;
                    lateMinutes += minutes;
                }
            }
            // 2.5 打卡早于下班时间时，扣除休息及请假范围后计为早退
            if (offDutyClock != null && offDutyClock.getClockTime() != null
                    && offDutyClock.getClockTime().isBefore(shiftTimeRange.getEndTime())) {
                int minutes = calculateEffectiveExceptionMinutes(
                        offDutyClock.getClockTime(), shiftTimeRange.getEndTime(), workRanges, leaveWorkRanges);
                if (minutes > 0) {
                    earlyCount++;
                    earlyMinutes += minutes;
                }
            }
        }

        // 3. 汇总单日考勤结果
        BigDecimal absenteeismDays = calculateDayRatio(absenteeismMinutes, scheduledMinutes);
        int actualClockCount = getActualClockCount(clockList);
        String attendanceResult = buildAttendanceResult(attendanceDate, calculationTime, scheduledMinutes,
                leaveMinutes, leaveDays, absenteeismMinutes, absenteeismDays, misscardCount,
                lateMinutes, earlyMinutes, actualClockCount, calculationTime.isAfter(clockOutDeadline));
        return new AttendanceDayCalculation(scheduledMinutes, leaveMinutes, leaveDays,
                lateCount, lateMinutes, earlyCount, earlyMinutes, misscardCount,
                absenteeismMinutes, absenteeismDays, actualClockCount, attendanceResult);
    }

    private TimeRange buildShiftTimeRange(LocalDate attendanceDate, HrmAttendanceGroupDO.Shift shift) {
        if (shift == null || shift.getStartTime() == null || shift.getEndTime() == null) {
            return null;
        }
        LocalDateTime startTime = attendanceDate.atTime(shift.getStartTime());
        LocalDateTime endTime = attendanceDate.atTime(shift.getEndTime());
        if (LocalDateTimeUtils.isBeforeOrEqual(endTime, startTime)) {
            endTime = endTime.plusDays(1);
        }
        return new TimeRange(startTime, endTime);
    }

    /**
     * 获得指定应打卡时间所属打卡时间段的截止时间
     *
     * @param attendanceDate 考勤日期
     * @param beginTime 打卡开始时间
     * @param endTime 打卡结束时间
     * @param attendanceTime 应打卡时间
     * @return 打卡截止时间
     */
    private LocalDateTime getClockDeadline(LocalDate attendanceDate, LocalTime beginTime,
                                           LocalTime endTime, LocalDateTime attendanceTime) {
        TimeRange clockTimeRange = LocalDateTimeUtils.findDailyTimeRange(
                attendanceDate, beginTime, endTime, attendanceTime);
        return clockTimeRange == null ? attendanceTime : clockTimeRange.getEndTime();
    }

    /**
     * 判断考勤日是否已经超过下班打卡截止时间
     *
     * @param attendanceDate 考勤日期
     * @param shift 班次
     * @param calculationTime 计算时间
     * @return 是否已经结算
     */
    private boolean isAttendanceDaySettled(LocalDate attendanceDate, HrmAttendanceGroupDO.Shift shift,
                                           LocalDateTime calculationTime) {
        TimeRange shiftTimeRange = buildShiftTimeRange(attendanceDate, shift);
        return shiftTimeRange != null && calculationTime.isAfter(getClockDeadline(
                attendanceDate, shift.getClockOutStartTime(), shift.getClockOutEndTime(),
                shiftTimeRange.getEndTime()));
    }

    /**
     * 构建班次的有效工作时间范围，并按配置扣除休息时间
     *
     * @param shiftTimeRange 班次时间范围
     * @param shift 班次配置
     * @return 有效工作时间范围
     */
    private List<TimeRange> buildWorkTimeRanges(TimeRange shiftTimeRange, HrmAttendanceGroupDO.Shift shift) {
        if (Boolean.FALSE.equals(shift.getExcludeRestTime())
                || shift.getRestStartTime() == null || shift.getRestEndTime() == null) {
            return Collections.singletonList(shiftTimeRange);
        }
        LocalDate firstDate = shiftTimeRange.getStartTime().toLocalDate().minusDays(1);
        LocalDate lastDate = shiftTimeRange.getEndTime().toLocalDate();
        List<TimeRange> restRanges = LocalDateTimeUtils.buildDailyTimeRanges(
                firstDate, lastDate, shift.getRestStartTime(), shift.getRestEndTime());
        List<TimeRange> workTimeRanges = new ArrayList<>(Collections.singletonList(shiftTimeRange));
        for (TimeRange restRange : restRanges) {
            workTimeRanges = LocalDateTimeUtils.subtractTimeRanges(workTimeRanges, restRange);
        }
        return LocalDateTimeUtils.mergeTimeRanges(workTimeRanges);
    }

    private List<TimeRange> buildLeaveWorkRanges(List<HrmAttendanceLeaveDO> leaveList,
                                                  List<TimeRange> workRanges) {
        List<TimeRange> ranges = new ArrayList<>();
        for (HrmAttendanceLeaveDO leave : leaveList) {
            TimeRange leaveRange = new TimeRange(leave.getStartTime(), leave.getEndTime());
            for (TimeRange workRange : workRanges) {
                TimeRange overlap = LocalDateTimeUtils.intersectTimeRange(leaveRange, workRange);
                if (overlap != null) {
                    ranges.add(overlap);
                }
            }
        }
        return LocalDateTimeUtils.mergeTimeRanges(ranges);
    }

    /**
     * 计算异常时段中实际属于工作且未被请假覆盖的分钟数
     *
     * @param startTime 异常开始时间
     * @param endTime 异常结束时间
     * @param workRanges 工作时间范围
     * @param leaveRanges 请假时间范围
     * @return 有效异常分钟数
     */
    private int calculateEffectiveExceptionMinutes(LocalDateTime startTime, LocalDateTime endTime,
                                                   List<TimeRange> workRanges, List<TimeRange> leaveRanges) {
        if (LocalDateTimeUtils.isBeforeOrEqual(endTime, startTime)) {
            return 0;
        }
        TimeRange exceptionRange = new TimeRange(startTime, endTime);
        int workMinutes = LocalDateTimeUtils.calculateDurationMinutes(LocalDateTimeUtils.intersectTimeRanges(
                workRanges, Collections.singletonList(exceptionRange)));
        int leaveMinutes = LocalDateTimeUtils.calculateDurationMinutes(LocalDateTimeUtils.intersectTimeRanges(
                leaveRanges, Collections.singletonList(exceptionRange)));
        return Math.max(workMinutes - leaveMinutes, 0);
    }

    private boolean isAttendanceRequiredAt(List<HrmAttendanceLeaveDO> leaveList, LocalDateTime attendanceTime) {
        return leaveList.stream().noneMatch(leave -> LocalDateTimeUtils.isBetween(
                leave.getStartTime(), leave.getEndTime(), attendanceTime));
    }

    private HrmAttendanceClockDO findClockByType(List<HrmAttendanceClockDO> clockList, Integer type) {
        Comparator<HrmAttendanceClockDO> comparator = Comparator.comparing(HrmAttendanceClockDO::getClockTime);
        return clockList.stream()
                .filter(clock -> clock.getClockTime() != null)
                .filter(clock -> Objects.equals(clock.getStage() == null ? 1 : clock.getStage(), 1))
                .filter(clock -> Objects.equals(clock.getType(), type))
                .min(Objects.equals(type, HrmAttendanceClockTypeEnum.ON_DUTY.getType())
                        ? comparator : comparator.reversed())
                .orElse(null);
    }

    private BigDecimal calculateDayRatio(int minutes, int scheduledMinutes) {
        if (minutes <= 0 || scheduledMinutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(scheduledMinutes), 2, RoundingMode.HALF_UP);
    }

    private String buildAttendanceResult(LocalDate attendanceDate, LocalDateTime currentTime, int scheduledMinutes,
                                         int leaveMinutes, BigDecimal leaveDays, int absenteeismMinutes,
                                         BigDecimal absenteeismDays, int misscardCount, int lateMinutes,
                                         int earlyMinutes, int actualClockCount, boolean settled) {
        if (attendanceDate.isAfter(currentTime.toLocalDate())) {
            return HrmAttendanceResultEnum.FUTURE.getFormat();
        }
        boolean fullLeave = scheduledMinutes > 0 && leaveMinutes >= scheduledMinutes;
        if (fullLeave && absenteeismMinutes == 0 && misscardCount == 0
                && lateMinutes == 0 && earlyMinutes == 0) {
            return HrmAttendanceResultEnum.LEAVE.getFormat();
        }
        List<String> results = new ArrayList<>();
        if (leaveMinutes > 0) {
            results.add(HrmAttendanceResultEnum.LEAVE_DAYS.format(NumberUtil.toStr(leaveDays)));
        }
        if (absenteeismMinutes > 0) {
            if (leaveMinutes == 0 && absenteeismDays.compareTo(BigDecimal.ONE) == 0) {
                results.add(HrmAttendanceResultEnum.ABSENTEEISM.getFormat());
            } else {
                results.add(HrmAttendanceResultEnum.ABSENTEEISM_DAYS.format(
                        NumberUtil.toStr(absenteeismDays)));
            }
        }
        if (misscardCount > 0) {
            results.add(HrmAttendanceResultEnum.MISS_CARD.format(misscardCount));
        }
        if (lateMinutes > 0) {
            results.add(HrmAttendanceResultEnum.LATE.format(lateMinutes));
        }
        if (earlyMinutes > 0) {
            results.add(HrmAttendanceResultEnum.EARLY.format(earlyMinutes));
        }
        if (CollUtil.isNotEmpty(results)) {
            return String.join(" / ", results);
        }
        return !settled || actualClockCount == 0
                ? HrmAttendanceResultEnum.PENDING_CLOCK.getFormat()
                : HrmAttendanceResultEnum.NORMAL.getFormat();
    }

    private List<HrmAttendanceMonthRecordRespVO> buildMonthSummaryList(
            Integer year, Integer month, List<HrmEmployeeDO> employeeList) {
        if (CollUtil.isEmpty(employeeList)) {
            return Collections.emptyList();
        }
        AttendanceMonthData monthData = getAttendanceMonthData(year, month, employeeList);
        return convertList(employeeList, employee -> buildMonthSummary(employee, monthData));
    }

    private List<HrmAttendanceMonthDailyOverviewRespVO> buildMonthDailyOverviewList(
            Integer year, Integer month, List<HrmEmployeeDO> employeeList) {
        if (CollUtil.isEmpty(employeeList)) {
            return Collections.emptyList();
        }
        AttendanceMonthData monthData = getAttendanceMonthData(year, month, employeeList);
        return convertList(employeeList, employee -> buildMonthDailyOverview(employee, monthData));
    }

    private HrmAttendanceMonthDailyOverviewRespVO buildMonthDailyOverview(
            HrmEmployeeDO employee, AttendanceMonthData monthData) {
        List<HrmAttendanceDailyDetailRespVO> dailyDetailList = buildMonthDailyDetails(employee, monthData);
        Map<LocalDate, HrmAttendanceDailyOverviewRespVO> dailyClockMap = new LinkedHashMap<>();
        for (HrmAttendanceDailyDetailRespVO dailyDetail : dailyDetailList) {
            dailyClockMap.put(dailyDetail.getAttendanceTime().toLocalDate(), new HrmAttendanceDailyOverviewRespVO()
                    .setClocks(dailyDetail.getClockList())
                    .setAttendanceResult(dailyDetail.getAttendanceResult()));
        }
        HrmAttendanceMonthDailyOverviewRespVO respVO = new HrmAttendanceMonthDailyOverviewRespVO();
        respVO.setEmployeeId(employee.getId()).setEmployeeName(employee.getName())
                .setJobNumber(employee.getJobNumber()).setDeptId(employee.getDeptId())
                .setPostName(employee.getPostName())
                .setYear(monthData.getYear()).setMonth(monthData.getMonth()).setDailyClockMap(dailyClockMap);
        return respVO;
    }

    private HrmAttendanceMonthRecordRespVO buildMonthSummary(
            HrmEmployeeDO employee, AttendanceMonthData monthData) {
        // 1. 归集员工当月打卡、班次和请假数据，逐日计算应出勤日结果
        List<HrmAttendanceClockDO> clockList = monthData.getEmployeeClockList(employee.getId());
        Map<LocalDate, HrmAttendanceGroupDO.Shift> shiftMap = monthData.getEmployeeShiftMap(employee.getId());
        Map<LocalDate, List<HrmAttendanceClockDO>> dateClockListMap =
                getDateClockListMap(clockList, shiftMap);
        List<HrmAttendanceLeaveDO> leaveList = monthData.getEmployeeLeaveList(employee.getId());
        List<LocalDate> scheduledDateList = filterList(monthData.getAttendanceDateList(),
                attendanceDate -> isScheduledWorkday(employee, attendanceDate, shiftMap.get(attendanceDate))
                        && isAttendanceDaySettled(attendanceDate, shiftMap.get(attendanceDate),
                        monthData.getCalculationTime()));
        List<AttendanceDayCalculation> dayCalculationList = convertList(scheduledDateList,
                attendanceDate -> calculateAttendanceDay(attendanceDate, shiftMap.get(attendanceDate),
                        dateClockListMap.getOrDefault(attendanceDate, Collections.emptyList()), leaveList,
                        monthData.getCalculationTime()));

        // 2. 拼接员工信息和基础出勤天数
        HrmAttendanceMonthRecordRespVO respVO = new HrmAttendanceMonthRecordRespVO();
        respVO.setEmployeeId(employee.getId()).setEmployeeName(employee.getName())
                .setJobNumber(employee.getJobNumber()).setDeptId(employee.getDeptId())
                .setPostName(employee.getPostName()).setEntryTime(employee.getEntryTime())
                .setEmployeeStatus(employee.getStatus()).setWorkCity(employee.getWorkCity());
        HrmAttendanceGroupDO attendanceGroup =
                monthData.getEmployeeAttendanceGroup(employee.getId());
        respVO.setAttendanceGroupName(attendanceGroup == null ? null : attendanceGroup.getName());
        respVO.setYear(monthData.getYear()).setMonth(monthData.getMonth());
        respVO.setAttendDays(scheduledDateList.size());
        // 3. 汇总迟到、早退、缺卡、旷工和请假结果
        if (CollUtil.isEmpty(dayCalculationList)) {
            respVO.setLateCount(countClockByStatus(clockList, HrmAttendanceClockStatusEnum.LATE.getStatus()));
            respVO.setEarlyCount(countClockByStatus(clockList, HrmAttendanceClockStatusEnum.EARLY.getStatus()));
            respVO.setLateMinute(sumClockExceptionMinutesByStatus(
                    clockList, HrmAttendanceClockStatusEnum.LATE.getStatus()));
            respVO.setEarlyMinute(sumClockExceptionMinutesByStatus(
                    clockList, HrmAttendanceClockStatusEnum.EARLY.getStatus()));
        } else {
            respVO.setLateCount(getSumValue(
                    dayCalculationList, AttendanceDayCalculation::getLateCount, Integer::sum, 0));
            respVO.setEarlyCount(getSumValue(
                    dayCalculationList, AttendanceDayCalculation::getEarlyCount, Integer::sum, 0));
            respVO.setLateMinute(getSumValue(
                    dayCalculationList, AttendanceDayCalculation::getLateMinutes, Integer::sum, 0));
            respVO.setEarlyMinute(getSumValue(
                    dayCalculationList, AttendanceDayCalculation::getEarlyMinutes, Integer::sum, 0));
        }
        respVO.setMisscardCount(getSumValue(
                dayCalculationList, AttendanceDayCalculation::getMisscardCount, Integer::sum, 0));
        respVO.setAbsenteeismMinutes(getSumValue(
                dayCalculationList, AttendanceDayCalculation::getAbsenteeismMinutes, Integer::sum, 0));
        respVO.setAbsenteeismDays(sumDays(dayCalculationList, AttendanceDayCalculation::getAbsenteeismDays));
        respVO.setLeaveMinutes(getSumValue(
                dayCalculationList, AttendanceDayCalculation::getLeaveMinutes, Integer::sum, 0));
        respVO.setLeaveDays(sumDays(dayCalculationList, AttendanceDayCalculation::getLeaveDays));

        // 4. 计算实际出勤、考勤扣款和全勤结果
        respVO.setActualDays(BigDecimal.valueOf(respVO.getAttendDays())
                .subtract(respVO.getLeaveDays())
                .subtract(respVO.getAbsenteeismDays())
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP));
        setAttendanceDeductAmounts(respVO, monthData.getEmployeeDeductRule(employee.getId()));
        respVO.setFullAttendance(respVO.getAttendDays() > 0
                && respVO.getActualDays().compareTo(BigDecimal.valueOf(respVO.getAttendDays())) >= 0
                && respVO.getLateCount() == 0 && respVO.getEarlyCount() == 0
                && respVO.getMisscardCount() == 0
                && respVO.getLeaveDays().compareTo(BigDecimal.ZERO) == 0
                && respVO.getAbsenteeismDays().compareTo(BigDecimal.ZERO) == 0);
        return respVO;
    }

    /**
     * 根据员工所属考勤组的扣款规则，计算月度迟到、早退、旷工和缺卡扣款
     *
     * @param respVO 月度考勤汇总
     * @param deductRule 扣款规则
     */
    @VisibleForTesting
    void setAttendanceDeductAmounts(
            HrmAttendanceMonthRecordRespVO respVO, HrmAttendanceGroupDO.DeductRule deductRule) {
        if (deductRule == null) {
            respVO.setLateDeductAmount(BigDecimal.ZERO).setEarlyDeductAmount(BigDecimal.ZERO)
                    .setAbsenteeismDeductAmount(BigDecimal.ZERO).setMisscardDeductAmount(BigDecimal.ZERO)
                    .setAttendanceDeductAmount(BigDecimal.ZERO);
            return;
        }
        BigDecimal lateDeductAmount = calculateLateEarlyDeductAmount(
                deductRule.getLateMethod(), deductRule.getLateDeductMoney(),
                respVO.getLateCount(), respVO.getLateMinute());
        BigDecimal earlyDeductAmount = calculateLateEarlyDeductAmount(
                deductRule.getEarlyMethod(), deductRule.getEarlyDeductMoney(),
                respVO.getEarlyCount(), respVO.getEarlyMinute());
        BigDecimal absenteeismDeductAmount = multiplyDeductMoney(
                deductRule.getAbsenteeismDeductMoney(), respVO.getAbsenteeismDays());
        BigDecimal misscardDeductAmount = multiplyDeductMoney(
                deductRule.getMisscardDeductMoney(), BigDecimal.valueOf(respVO.getMisscardCount()));
        respVO.setLateDeductAmount(lateDeductAmount).setEarlyDeductAmount(earlyDeductAmount)
                .setAbsenteeismDeductAmount(absenteeismDeductAmount)
                .setMisscardDeductAmount(misscardDeductAmount)
                .setAttendanceDeductAmount(priceAdd(lateDeductAmount, earlyDeductAmount,
                        absenteeismDeductAmount, misscardDeductAmount));
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private BigDecimal calculateLateEarlyDeductAmount(
            Integer method, BigDecimal deductMoney, int count, int minutes) {
        HrmAttendanceLateEarlyDeductMethodEnum methodEnum =
                HrmAttendanceLateEarlyDeductMethodEnum.valueOf(method);
        if (methodEnum == null || deductMoney == null) {
            return BigDecimal.ZERO;
        }
        switch (methodEnum) {
            case FIXED_MONTH:
                return priceScale(deductMoney);
            case BY_MINUTE:
                return multiplyDeductMoney(deductMoney, BigDecimal.valueOf(minutes));
            case BY_COUNT:
                return multiplyDeductMoney(deductMoney, BigDecimal.valueOf(count));
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal multiplyDeductMoney(BigDecimal deductMoney, BigDecimal quantity) {
        return ObjectUtil.defaultIfNull(priceMultiply(deductMoney, quantity), BigDecimal.ZERO);
    }

    private AttendanceMonthData getAttendanceMonthData(
            Integer year, Integer month, List<HrmEmployeeDO> employeeList) {
        LocalDateTime calculationTime = LocalDateTime.now();
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(year, month);
        LocalDate monthBeginDate = monthTimes[0].toLocalDate();
        LocalDate monthEndDate = monthTimes[1].toLocalDate();
        List<LocalDate> attendanceDateList = LocalDateTimeUtils.getDateList(
                monthBeginDate, monthBeginDate.lengthOfMonth());
        List<LocalDate> shiftAttendanceDateList = LocalDateTimeUtils.getDateList(
                monthBeginDate.minusDays(1), monthBeginDate.lengthOfMonth() + 1);
        List<Long> employeeIds = convertList(employeeList, HrmEmployeeDO::getId);

        // 1. 批量查询月度考勤关联数据
        List<HrmAttendanceClockDO> clockList =
                getEmployeeClockListForAttendanceTimes(employeeIds, monthTimes);
        Map<Long, HrmAttendanceGroupDO> employeeAttendanceGroupMap =
                attendanceGroupService.getAttendanceGroupMap(employeeIds);
        Map<LocalDate, HrmAttendanceHolidayDO> holidayMap =
                attendanceHolidayService.getAttendanceHolidayMap(
                        LocalDateTimeUtils.getDateTimeRange(
                                monthBeginDate.minusDays(1), monthEndDate));
        List<HrmAttendanceLeaveDO> leaveList = attendanceLeaveService
                .getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(
                        employeeIds, LocalDateTimeUtils.getDateTimeRange(
                                monthBeginDate, monthEndDate.plusDays(1)));

        // 2.1 按考勤组解析班次，同一考勤组只计算一次
        Map<Long, HrmAttendanceGroupDO> attendanceGroupMap = convertMap(
                employeeAttendanceGroupMap.values(), HrmAttendanceGroupDO::getId);
        Map<Long, Map<LocalDate, HrmAttendanceGroupDO.Shift>> attendanceGroupShiftMap = convertMap(
                attendanceGroupMap.values(), HrmAttendanceGroupDO::getId,
                attendanceGroup -> attendanceGroupService.getAttendanceGroupShiftMap(
                        attendanceGroup, shiftAttendanceDateList, holidayMap));
        // 2.2 按员工归集打卡、班次和请假数据
        Map<Long, Map<LocalDate, HrmAttendanceGroupDO.Shift>> employeeShiftMap = convertMap(
                employeeList, HrmEmployeeDO::getId, employee -> {
                    HrmAttendanceGroupDO attendanceGroup = employeeAttendanceGroupMap.get(employee.getId());
                    return attendanceGroup == null ? Collections.emptyMap()
                            : attendanceGroupShiftMap.get(attendanceGroup.getId());
                });
        Map<Long, List<HrmAttendanceClockDO>> rawEmployeeClockListMap =
                convertMultiMap(clockList, HrmAttendanceClockDO::getEmployeeId);
        Map<Long, List<HrmAttendanceClockDO>> employeeClockListMap = convertMap(
                employeeList, HrmEmployeeDO::getId, employee -> filterList(
                        rawEmployeeClockListMap.getOrDefault(employee.getId(), Collections.emptyList()),
                        clock -> isAttendanceDateInRange(clock, employeeShiftMap.get(employee.getId()),
                                monthBeginDate, monthEndDate)));
        Map<Long, List<HrmAttendanceLeaveDO>> employeeLeaveListMap =
                convertMultiMap(leaveList, HrmAttendanceLeaveDO::getEmployeeId);
        return new AttendanceMonthData(year, month, calculationTime,
                attendanceDateList, employeeClockListMap, employeeAttendanceGroupMap,
                employeeShiftMap, employeeLeaveListMap);
    }

    private List<HrmEmployeeDO> getAttendanceEmployeeList(HrmAttendanceMonthRecordPageReqVO reqVO) {
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                reqVO.getYear(), reqVO.getMonth());
        HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO();
        employeeReqVO.setIds(reqVO.getEmployeeId() == null
                ? null : Collections.singletonList(reqVO.getEmployeeId()));
        employeeReqVO.setSearch(reqVO.getSearch()).setDeptIds(reqVO.getDeptIds())
                .setActiveTime(monthTimes);
        List<HrmEmployeeDO> employeeList = employeeService.getEmployeeList(employeeReqVO);
        return employeeList;
    }

    private HrmEmployeePageReqVO buildEmployeePageReqVO(HrmAttendanceMonthRecordPageReqVO reqVO) {
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                reqVO.getYear(), reqVO.getMonth());
        HrmEmployeePageReqVO employeeReqVO = new HrmEmployeePageReqVO();
        employeeReqVO.setIds(reqVO.getEmployeeId() == null
                ? null : Collections.singletonList(reqVO.getEmployeeId()));
        employeeReqVO.setSearch(reqVO.getSearch()).setDeptIds(reqVO.getDeptIds())
                .setActiveTime(monthTimes);
        employeeReqVO.setPageNo(reqVO.getPageNo()).setPageSize(reqVO.getPageSize());
        return employeeReqVO;
    }

    private List<HrmEmployeeDO> filterAttendanceEmployeeList(
            Integer year, Integer month, List<HrmEmployeeDO> employeeList) {
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(year, month);
        return filterList(employeeList, employee -> employee != null
                && employee.getEntryTime() != null
                && LocalDateTimeUtils.isBeforeOrEqual(employee.getEntryTime(), monthTimes[1])
                && (employee.getLeaveTime() == null
                || LocalDateTimeUtils.isAfterOrEqual(employee.getLeaveTime(), monthTimes[0])));
    }

    private BigDecimal sumDays(List<AttendanceDayCalculation> dayCalculationList,
                               Function<AttendanceDayCalculation, BigDecimal> valueFunction) {
        return dayCalculationList.stream()
                .map(valueFunction)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<HrmAttendanceClockDO> getEmployeeClockListForAttendanceTimes(
            Long employeeId, LocalDateTime[] attendanceTimes) {
        LocalDate beginDate = attendanceTimes[0].toLocalDate();
        LocalDate endDate = attendanceTimes[1].toLocalDate();
        return attendanceClockService.getAttendanceClockListByEmployeeIdAndClockTime(employeeId,
                LocalDateTimeUtils.getDateTimeRange(
                        beginDate.minusDays(1), endDate.plusDays(1)));
    }

    private List<HrmAttendanceClockDO> getEmployeeClockListForAttendanceTimes(
            List<Long> employeeIds, LocalDateTime[] attendanceTimes) {
        LocalDate beginDate = attendanceTimes[0].toLocalDate();
        LocalDate endDate = attendanceTimes[1].toLocalDate();
        return attendanceClockService.getAttendanceClockListByEmployeeIdsAndClockTime(employeeIds,
                LocalDateTimeUtils.getDateTimeRange(
                        beginDate.minusDays(1), endDate.plusDays(1)));
    }

    private Map<LocalDate, List<HrmAttendanceClockDO>> getDateClockListMap(
            List<HrmAttendanceClockDO> clockList,
            Map<LocalDate, HrmAttendanceGroupDO.Shift> shiftMap) {
        Map<LocalDate, List<HrmAttendanceClockDO>> result = new LinkedHashMap<>();
        for (HrmAttendanceClockDO clock : clockList) {
            LocalDate attendanceDate = getAttendanceDate(clock, shiftMap);
            if (attendanceDate != null) {
                result.computeIfAbsent(attendanceDate, key -> new ArrayList<>()).add(clock);
            }
        }
        return result;
    }

    private boolean isAttendanceDateInRange(
            HrmAttendanceClockDO clock, Map<LocalDate, HrmAttendanceGroupDO.Shift> shiftMap,
            LocalDate beginDate, LocalDate endDate) {
        LocalDate attendanceDate = getAttendanceDate(clock, shiftMap);
        return attendanceDate != null && LocalDateTimeUtils.isBetween(beginDate, endDate, attendanceDate);
    }

    /**
     * 根据应打卡时间和班次计算打卡所属业务日期
     *
     * @param clock 打卡记录
     * @param shiftMap 日期对应班次 Map
     * @return 打卡所属业务日期
     */
    @VisibleForTesting
    LocalDate getAttendanceDate(HrmAttendanceClockDO clock,
                                Map<LocalDate, HrmAttendanceGroupDO.Shift> shiftMap) {
        if (clock.getAttendanceTime() == null) {
            return clock.getClockTime() == null ? null : clock.getClockTime().toLocalDate();
        }
        LocalDate attendanceDate = clock.getAttendanceTime().toLocalDate();
        if (isScheduledClockTime(clock, attendanceDate, shiftMap.get(attendanceDate))) {
            return attendanceDate;
        }
        LocalDate previousAttendanceDate = attendanceDate.minusDays(1);
        if (isScheduledClockTime(clock, previousAttendanceDate, shiftMap.get(previousAttendanceDate))) {
            return previousAttendanceDate;
        }
        return attendanceDate;
    }

    /**
     * 判断打卡记录的应打卡时间是否属于指定业务日期的班次
     *
     * @param clock 打卡记录
     * @param attendanceDate 考勤业务日期
     * @param shift 班次
     * @return 是否属于指定业务日期的班次
     */
    private boolean isScheduledClockTime(
            HrmAttendanceClockDO clock, LocalDate attendanceDate,
            HrmAttendanceGroupDO.Shift shift) {
        TimeRange shiftTimeRange = buildShiftTimeRange(attendanceDate, shift);
        if (shiftTimeRange == null) {
            return false;
        }
        LocalDateTime scheduledTime;
        if (Objects.equals(clock.getType(), HrmAttendanceClockTypeEnum.ON_DUTY.getType())) {
            scheduledTime = shiftTimeRange.getStartTime();
        } else if (Objects.equals(clock.getType(), HrmAttendanceClockTypeEnum.OFF_DUTY.getType())) {
            scheduledTime = shiftTimeRange.getEndTime();
        } else {
            return false;
        }
        return Objects.equals(clock.getAttendanceTime(), scheduledTime);
    }

    private boolean isScheduledWorkday(HrmEmployeeDO employee, LocalDate attendanceDate,
                                       HrmAttendanceGroupDO.Shift shift) {
        return shift != null
                && (employee.getEntryTime() == null || LocalDateTimeUtils.isBeforeOrEqual(
                        employee.getEntryTime().toLocalDate(), attendanceDate))
                && (employee.getLeaveTime() == null || LocalDateTimeUtils.isAfterOrEqual(employee.getLeaveTime().toLocalDate(), attendanceDate));
    }

    private int getActualClockCount(List<HrmAttendanceClockDO> clockList) {
        return (int) clockList.stream()
                .filter(clock -> clock.getType() != null)
                .filter(clock -> clock.getStage() == null
                        || Objects.equals(clock.getStage(), HrmAttendanceClockStageEnum.FIRST.getStage()))
                .map(HrmAttendanceClockDO::getType)
                .distinct()
                .count();
    }

    private int countClockByStatus(List<HrmAttendanceClockDO> clockList, Integer status) {
        return (int) clockList.stream().filter(clock -> Objects.equals(clock.getStatus(), status)).count();
    }

    private int sumClockExceptionMinutesByStatus(List<HrmAttendanceClockDO> clockList, Integer status) {
        return clockList.stream()
                .filter(clock -> Objects.equals(clock.getStatus(), status))
                .filter(clock -> clock.getAttendanceTime() != null && clock.getClockTime() != null)
                .mapToInt(clock -> Math.toIntExact(Math.abs(Duration.between(
                        clock.getAttendanceTime(), clock.getClockTime()).toMinutes())))
                .sum();
    }

    /**
     * 月度考勤计算所需的批量数据
     */
    @Getter
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class AttendanceMonthData {

        /**
         * 年份
         */
        private final Integer year;
        /**
         * 月份
         */
        private final Integer month;
        /**
         * 统一计算时间
         */
        private final LocalDateTime calculationTime;
        /**
         * 考勤日期列表
         */
        private final List<LocalDate> attendanceDateList;
        /**
         * 员工打卡记录 Map
         */
        private final Map<Long, List<HrmAttendanceClockDO>> employeeClockListMap;
        /**
         * 员工考勤组 Map
         */
        private final Map<Long, HrmAttendanceGroupDO> employeeAttendanceGroupMap;
        /**
         * 员工班次 Map
         */
        private final Map<Long, Map<LocalDate, HrmAttendanceGroupDO.Shift>> employeeShiftMap;
        /**
         * 员工有效请假记录 Map
         */
        private final Map<Long, List<HrmAttendanceLeaveDO>> employeeLeaveListMap;

        private List<HrmAttendanceClockDO> getEmployeeClockList(Long employeeId) {
            return employeeClockListMap.getOrDefault(employeeId, Collections.emptyList());
        }

        private HrmAttendanceGroupDO.DeductRule getEmployeeDeductRule(Long employeeId) {
            HrmAttendanceGroupDO attendanceGroup = employeeAttendanceGroupMap.get(employeeId);
            return attendanceGroup == null ? null : attendanceGroup.getDeductRule();
        }

        private HrmAttendanceGroupDO getEmployeeAttendanceGroup(Long employeeId) {
            return employeeAttendanceGroupMap.get(employeeId);
        }

        private Map<LocalDate, HrmAttendanceGroupDO.Shift> getEmployeeShiftMap(Long employeeId) {
            return employeeShiftMap.getOrDefault(employeeId, Collections.emptyMap());
        }

        private List<HrmAttendanceLeaveDO> getEmployeeLeaveList(Long employeeId) {
            return employeeLeaveListMap.getOrDefault(employeeId, Collections.emptyList());
        }

    }

    /**
     * 员工单日考勤计算结果
     */
    @Getter
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    static class AttendanceDayCalculation {

        /**
         * 应出勤分钟数
         */
        private final int scheduledMinutes;
        /**
         * 请假分钟数
         */
        private final int leaveMinutes;
        /**
         * 请假天数
         */
        private final BigDecimal leaveDays;
        /**
         * 迟到次数
         */
        private final int lateCount;
        /**
         * 迟到分钟数
         */
        private final int lateMinutes;
        /**
         * 早退次数
         */
        private final int earlyCount;
        /**
         * 早退分钟数
         */
        private final int earlyMinutes;
        /**
         * 缺卡次数
         */
        private final int misscardCount;
        /**
         * 旷工分钟数
         */
        private final int absenteeismMinutes;
        /**
         * 旷工天数
         */
        private final BigDecimal absenteeismDays;
        /**
         * 实际打卡次数
         */
        private final int actualClockCount;
        /**
         * 考勤结果
         */
        private final String attendanceResult;

        private static AttendanceDayCalculation rest(int actualClockCount) {
            return new AttendanceDayCalculation(0, 0, BigDecimal.ZERO, 0, 0,
                    0, 0, 0, 0, BigDecimal.ZERO, actualClockCount,
                    HrmAttendanceResultEnum.REST.getFormat());
        }

    }

}
