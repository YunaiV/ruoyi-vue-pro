package cn.iocoder.yudao.module.hrm.service.attendance.record;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.TimeRange;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockDetailRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.record.HrmAttendanceClockMapper;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockButtonStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockSourceEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStageEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockTypeEnum;
import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceGroupService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_GROUP_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_NON_MANUAL_MODIFY_FORBIDDEN;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_NOT_ALLOWED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_POINT_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_REST_DAY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_TIME_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_WIFI_INVALID;

/**
 * HRM 考勤打卡 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmAttendanceClockServiceImpl implements HrmAttendanceClockService {

    @Resource
    private HrmAttendanceClockMapper attendanceClockMapper;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmAttendanceGroupService attendanceGroupService;

    @Override
    public Long createAttendanceClock(HrmAttendanceClockSaveReqVO createReqVO) {
        // 1.1 校验员工存在
        employeeService.validateEmployeeExists(createReqVO.getEmployeeId());
        // 1.2 校验应打卡时间和实际打卡时间位于员工班次窗口内
        validateAttendanceClockTime(createReqVO);

        // 2. 创建手工打卡记录，来源和状态均由后端确定
        HrmAttendanceClockDO clock = BeanUtils.toBean(createReqVO, HrmAttendanceClockDO.class);
        fillAttendanceClockDefaultValue(clock, HrmAttendanceClockSourceEnum.MANUAL);
        attendanceClockMapper.insert(clock);
        return clock.getId();
    }

    @Override
    public HrmPortalAttendanceClockDetailRespVO getMyAttendanceClockDetail(Long userId) {
        // 1. 解析本人与考勤组
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(userId);
        HrmAttendanceGroupDO group = attendanceGroupService.getMyAttendanceGroup(employee.getId());
        if (group == null) {
            throw exception(ATTENDANCE_CLOCK_GROUP_NOT_EXISTS);
        }

        // 2. 解析当前班次日与班次窗口
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        HrmPortalAttendanceClockDetailRespVO detail = resolveAttendanceClockDetail(employee.getId(), now)
                .setGroup(group);

        // 3. 计算下一步动作和当日时间线
        List<HrmAttendanceClockDO> dayClocks = getAttendanceClockListByAttendanceTime(
                employee.getId(), detail.getOnDutyAttendanceTime(), detail.getOffDutyAttendanceTime());
        return detail.setNextClock(buildNextClock(detail, dayClocks, now))
                .setTimeline(buildTimeline(detail, dayClocks, now));
    }

    @Override
    public Long createMyAttendanceClock(Long userId, HrmPortalAttendanceClockCreateReqVO createReqVO) {
        // 1.1 解析本人、考勤组和下一步打卡动作
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(userId);
        HrmAttendanceGroupDO group = attendanceGroupService.getMyAttendanceGroup(employee.getId());
        if (group == null) {
            throw exception(ATTENDANCE_CLOCK_GROUP_NOT_EXISTS);
        }
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        HrmPortalAttendanceClockDetailRespVO detail = resolveAttendanceClockDetail(employee.getId(), now);
        if (detail.getShift() == null) {
            throw exception(ATTENDANCE_CLOCK_REST_DAY);
        }
        List<HrmAttendanceClockDO> dayClocks = getAttendanceClockListByAttendanceTime(
                employee.getId(), detail.getOnDutyAttendanceTime(), detail.getOffDutyAttendanceTime());
        HrmPortalAttendanceClockDetailRespVO.NextClock nextClock = buildNextClock(detail, dayClocks, now);
        HrmAttendanceClockButtonStatusEnum buttonStatus =
                HrmAttendanceClockButtonStatusEnum.valueOf(nextClock.getButtonStatus());
        if (buttonStatus == null || !buttonStatus.isCanClock()) {
            throw exception(ATTENDANCE_CLOCK_NOT_ALLOWED);
        }
        // 1.2 按考勤组开关校验定位 / WiFi
        validateMobileClockLocationAndWifi(group, createReqVO);

        // 2. 写入手机端打卡
        HrmAttendanceClockDO clock = new HrmAttendanceClockDO()
                .setEmployeeId(employee.getId()).setType(nextClock.getType())
                .setAttendanceTime(nextClock.getAttendanceTime()).setClockTime(now)
                .setAddress(createReqVO.getAddress()).setLongitude(createReqVO.getLongitude())
                .setLatitude(createReqVO.getLatitude())
                .setSsid(createReqVO.getSsid()).setMac(createReqVO.getMac());
        fillAttendanceClockDefaultValue(clock, HrmAttendanceClockSourceEnum.MOBILE);
        attendanceClockMapper.insert(clock);
        return clock.getId();
    }

    @Override
    public void updateAttendanceClock(HrmAttendanceClockSaveReqVO updateReqVO) {
        // 1.1 校验记录存在且为手工录入
        HrmAttendanceClockDO oldClock = validateAttendanceClockExists(updateReqVO.getId());
        validateAttendanceClockManual(oldClock);
        // 1.2 校验员工存在
        employeeService.validateEmployeeExists(updateReqVO.getEmployeeId());
        // 1.3 校验应打卡时间和实际打卡时间位于员工班次窗口内
        validateAttendanceClockTime(updateReqVO);

        // 2. 更新手工打卡记录，重新计算状态
        HrmAttendanceClockDO clock = BeanUtils.toBean(updateReqVO, HrmAttendanceClockDO.class);
        fillAttendanceClockDefaultValue(clock, HrmAttendanceClockSourceEnum.MANUAL);
        attendanceClockMapper.updateById(clock);
    }

    @Override
    public void deleteAttendanceClock(Long id) {
        // 1. 校验记录存在且为手工录入
        HrmAttendanceClockDO clock = validateAttendanceClockExists(id);
        validateAttendanceClockManual(clock);

        // 2. 删除手工打卡记录
        attendanceClockMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttendanceClockList(List<Long> ids) {
        // 1. 校验记录全部存在且均为手工录入，避免批量删除部分成功
        Set<Long> distinctIds = convertSet(ids);
        if (CollUtil.isEmpty(distinctIds)) {
            throw exception(ATTENDANCE_CLOCK_NOT_EXISTS);
        }
        List<HrmAttendanceClockDO> clocks = attendanceClockMapper.selectByIds(distinctIds);
        if (clocks.size() != distinctIds.size()) {
            throw exception(ATTENDANCE_CLOCK_NOT_EXISTS);
        }
        clocks.forEach(this::validateAttendanceClockManual);

        // 2. 删除手工打卡记录
        attendanceClockMapper.deleteByIds(distinctIds);
    }

    @Override
    public HrmAttendanceClockDO getAttendanceClock(Long id) {
        return attendanceClockMapper.selectById(id);
    }

    @Override
    public List<HrmAttendanceClockDO> getAttendanceClockListByEmployeeIdAndClockTime(
            Long employeeId, LocalDateTime[] clockTimes) {
        return attendanceClockMapper.selectListByEmployeeIdAndClockTime(employeeId, clockTimes);
    }

    @Override
    public List<HrmAttendanceClockDO> getAttendanceClockListByEmployeeIdsAndClockTime(
            Collection<Long> employeeIds, LocalDateTime[] clockTimes) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return attendanceClockMapper.selectListByEmployeeIdsAndClockTime(employeeIds, clockTimes);
    }

    @Override
    public PageResult<HrmAttendanceClockDO> getAttendanceClockPage(HrmAttendanceClockPageReqVO pageReqVO) {
        Collection<Long> employeeIds = getClockEmployeeIds(pageReqVO);
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return PageResult.empty();
        }
        pageReqVO.setEmployeeIds(employeeIds);
        return attendanceClockMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmAttendanceClockDO> getAttendanceClockList(HrmAttendanceClockPageReqVO reqVO) {
        Collection<Long> employeeIds = getClockEmployeeIds(reqVO);
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        reqVO.setEmployeeIds(employeeIds);
        return attendanceClockMapper.selectList(reqVO);
    }

    private HrmAttendanceClockDO validateAttendanceClockExists(Long id) {
        HrmAttendanceClockDO clock = id == null ? null : attendanceClockMapper.selectById(id);
        if (clock == null) {
            throw exception(ATTENDANCE_CLOCK_NOT_EXISTS);
        }
        return clock;
    }

    private void validateAttendanceClockManual(HrmAttendanceClockDO clock) {
        if (ObjUtil.notEqual(clock.getSourceType(), HrmAttendanceClockSourceEnum.MANUAL.getSource())) {
            throw exception(ATTENDANCE_CLOCK_NON_MANUAL_MODIFY_FORBIDDEN);
        }
    }

    private void fillAttendanceClockDefaultValue(HrmAttendanceClockDO clock,
                                                 HrmAttendanceClockSourceEnum sourceType) {
        clock.setSourceType(sourceType.getSource())
                .setStage(HrmAttendanceClockStageEnum.FIRST.getStage())
                .setStatus(calculateClockStatus(clock));
    }

    /**
     * 校验管理员补录的打卡时间
     *
     * @param reqVO 打卡信息
     */
    private void validateAttendanceClockTime(HrmAttendanceClockSaveReqVO reqVO) {
        LocalDate attendanceDate = reqVO.getAttendanceTime().toLocalDate();
        List<LocalDate> candidateDates = Arrays.asList(attendanceDate, attendanceDate.minusDays(1));
        if (candidateDates.stream().noneMatch(candidateDate ->
                isAttendanceClockTimeValid(reqVO, candidateDate))) {
            throw exception(ATTENDANCE_CLOCK_TIME_INVALID);
        }
    }

    /**
     * 校验打卡时间是否匹配员工当日班次及打卡时间段
     *
     * @param reqVO 打卡保存参数
     * @param attendanceDate 考勤日期
     * @return 是否有效
     */
    private boolean isAttendanceClockTimeValid(
            HrmAttendanceClockSaveReqVO reqVO, LocalDate attendanceDate) {
        // 1. 解析员工当天班次，休息日或班次时间不完整时不允许补录
        HrmAttendanceGroupDO.Shift shift = attendanceGroupService.getEmployeeAttendanceShift(
                reqVO.getEmployeeId(), attendanceDate.atStartOfDay());
        if (shift == null || shift.getStartTime() == null || shift.getEndTime() == null) {
            return false;
        }

        // 2. 校验应打卡时间与班次的上班或下班时间一致
        TimeRange shiftTimeRange = CollUtil.getFirst(LocalDateTimeUtils.buildDailyTimeRanges(
                attendanceDate, attendanceDate, shift.getStartTime(), shift.getEndTime()));
        boolean onDuty = ObjUtil.equals(reqVO.getType(), HrmAttendanceClockTypeEnum.ON_DUTY.getType());
        LocalDateTime scheduledTime = onDuty ? shiftTimeRange.getStartTime() : shiftTimeRange.getEndTime();
        if (ObjUtil.notEqual(reqVO.getAttendanceTime(), scheduledTime)) {
            return false;
        }

        // 3. 校验实际打卡时间位于对应的上班或下班打卡闭区间内
        LocalTime clockBeginTime = onDuty ? shift.getClockInStartTime() : shift.getClockOutStartTime();
        LocalTime clockEndTime = onDuty ? shift.getClockInEndTime() : shift.getClockOutEndTime();
        TimeRange clockTimeRange = LocalDateTimeUtils.findDailyTimeRange(
                attendanceDate, clockBeginTime, clockEndTime, scheduledTime);
        return clockTimeRange != null && LocalDateTimeUtils.isBetween(
                clockTimeRange.getStartTime(), clockTimeRange.getEndTime(), reqVO.getClockTime());
    }

    /**
     * 计算打卡状态
     *
     * @param clock 打卡记录
     * @return 打卡状态
     */
    private Integer calculateClockStatus(HrmAttendanceClockDO clock) {
        // 上班打卡晚于应打卡时间时，状态为迟到
        if (ObjUtil.equals(clock.getType(), HrmAttendanceClockTypeEnum.ON_DUTY.getType())
                && clock.getClockTime().isAfter(clock.getAttendanceTime())) {
            return HrmAttendanceClockStatusEnum.LATE.getStatus();
        }
        // 下班打卡早于应打卡时间时，状态为早退
        if (ObjUtil.equals(clock.getType(), HrmAttendanceClockTypeEnum.OFF_DUTY.getType())
                && clock.getClockTime().isBefore(clock.getAttendanceTime())) {
            return HrmAttendanceClockStatusEnum.EARLY.getStatus();
        }
        return HrmAttendanceClockStatusEnum.NORMAL.getStatus();
    }

    private Collection<Long> getClockEmployeeIds(HrmAttendanceClockPageReqVO reqVO) {
        if (StrUtil.isBlank(reqVO.getSearch()) && CollUtil.isEmpty(reqVO.getDeptIds())) {
            return null;
        }
        HrmEmployeeListReqVO listReqVO = new HrmEmployeeListReqVO()
                .setIds(reqVO.getEmployeeId() == null ? null : Collections.singletonList(reqVO.getEmployeeId()))
                .setSearch(reqVO.getSearch()).setDeptIds(reqVO.getDeptIds());
        List<HrmEmployeeDO> list = employeeService.getEmployeeList(listReqVO);
        return convertList(list, HrmEmployeeDO::getId);
    }

    /**
     * 解析员工当前应归属的考勤日与班次窗口
     *
     * 优先取今日班次；若当前仍落在昨日下班打卡窗口内，则继续归属昨日班次。
     */
    private HrmPortalAttendanceClockDetailRespVO resolveAttendanceClockDetail(
            Long employeeId, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);
        HrmPortalAttendanceClockDetailRespVO yesterdayDetail = buildAttendanceClockDetail(employeeId, yesterday);
        TimeRange yesterdayClockOutRange = yesterdayDetail.getClockOutRange();
        if (yesterdayDetail.getShift() != null && yesterdayClockOutRange != null
                && LocalDateTimeUtils.isBetween(yesterdayClockOutRange.getStartTime(), yesterdayClockOutRange.getEndTime(), now)
                && (yesterdayDetail.getClockInRange() == null || now.isAfter(yesterdayDetail.getClockInRange().getEndTime()))) {
            return yesterdayDetail;
        }
        return buildAttendanceClockDetail(employeeId, today);
    }

    /**
     * 构建员工指定考勤日的班次详情
     *
     * @param employeeId 员工编号
     * @param attendanceDate 考勤日期
     * @return 班次详情
     */
    private HrmPortalAttendanceClockDetailRespVO buildAttendanceClockDetail(
            Long employeeId, LocalDate attendanceDate) {
        // 实际班次由考勤组、特殊日期和节假日共同确定
        HrmAttendanceGroupDO.Shift shift = attendanceGroupService.getEmployeeAttendanceShift(
                employeeId, attendanceDate.atStartOfDay());
        HrmPortalAttendanceClockDetailRespVO detail = new HrmPortalAttendanceClockDetailRespVO()
                .setAttendanceDate(attendanceDate).setShift(shift).setRestDay(shift == null);
        if (shift == null || ObjUtil.hasEmpty(shift.getStartTime(), shift.getEndTime(),
                shift.getClockInStartTime(), shift.getClockInEndTime(),
                shift.getClockOutStartTime(), shift.getClockOutEndTime())) {
            return detail;
        }
        // 将班次时分转换为当前考勤日对应的实际日期时间范围
        TimeRange shiftRange = CollUtil.getFirst(LocalDateTimeUtils.buildDailyTimeRanges(
                attendanceDate, attendanceDate, shift.getStartTime(), shift.getEndTime()));
        // 以上下班应打卡时间为锚点，分别解析可能跨日的打卡窗口
        return detail.setOnDutyAttendanceTime(shiftRange.getStartTime()).setOffDutyAttendanceTime(shiftRange.getEndTime())
                .setClockInRange(LocalDateTimeUtils.findDailyTimeRange(attendanceDate,
                        shift.getClockInStartTime(), shift.getClockInEndTime(), shiftRange.getStartTime()))
                .setClockOutRange(LocalDateTimeUtils.findDailyTimeRange(attendanceDate,
                        shift.getClockOutStartTime(), shift.getClockOutEndTime(), shiftRange.getEndTime()));
    }

    private List<HrmAttendanceClockDO> getAttendanceClockListByAttendanceTime(
            Long employeeId, LocalDateTime beginTime, LocalDateTime endTime) {
        if (ObjUtil.hasEmpty(beginTime, endTime)) {
            return Collections.emptyList();
        }
        return attendanceClockMapper.selectList(new HrmAttendanceClockPageReqVO().setEmployeeId(employeeId)
                .setAttendanceTime(new LocalDateTime[]{beginTime, endTime}));
    }

    /**
     * 构建下一次打卡动作
     *
     * @param detail 打卡详情
     * @param dayClocks 当日打卡记录
     * @param now 当前时间
     * @return 下一次打卡动作
     */
    private HrmPortalAttendanceClockDetailRespVO.NextClock buildNextClock(
            HrmPortalAttendanceClockDetailRespVO detail,
            List<HrmAttendanceClockDO> dayClocks, LocalDateTime now) {
        // 员工端当前只使用第一打卡阶段；无有效班次时不可打卡
        HrmPortalAttendanceClockDetailRespVO.NextClock nextClock =
                new HrmPortalAttendanceClockDetailRespVO.NextClock();
        nextClock.setStage(HrmAttendanceClockStageEnum.FIRST.getStage());
        if (ObjUtil.hasEmpty(detail.getShift(), detail.getOnDutyAttendanceTime(), detail.getOffDutyAttendanceTime())) {
            return nextClock.setType(HrmAttendanceClockTypeEnum.ON_DUTY.getType())
                    .setButtonStatus(HrmAttendanceClockButtonStatusEnum.NOT_YET.getStatus());
        }

        // 根据已存在的上下班卡，判断下一步应上班、下班还是更新打卡
        HrmAttendanceClockDO onDutyClock = CollUtil.findOne(dayClocks,
                clock -> ObjUtil.equals(clock.getType(), HrmAttendanceClockTypeEnum.ON_DUTY.getType()));
        HrmAttendanceClockDO offDutyClock = CollUtil.findOne(dayClocks,
                clock -> ObjUtil.equals(clock.getType(), HrmAttendanceClockTypeEnum.OFF_DUTY.getType()));
        TimeRange clockInRange = detail.getClockInRange();
        TimeRange clockOutRange = detail.getClockOutRange();
        LocalDateTime onDutyTime = detail.getOnDutyAttendanceTime();
        LocalDateTime offDutyTime = detail.getOffDutyAttendanceTime();

        // 尚未上班打卡：优先引导上班；已过上班窗口则引导下班
        if (onDutyClock == null) {
            nextClock.setType(HrmAttendanceClockTypeEnum.ON_DUTY.getType());
            nextClock.setAttendanceTime(onDutyTime);
            if (clockInRange != null && LocalDateTimeUtils.isBetween(
                    clockInRange.getStartTime(), clockInRange.getEndTime(), now)) {
                if (LocalDateTimeUtils.isBeforeOrEqual(now, onDutyTime)) {
                    return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.NORMAL.getStatus());
                }
                return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.LATE.getStatus());
            }
            if (clockOutRange != null
                    && LocalDateTimeUtils.isBetween(clockOutRange.getStartTime(), clockOutRange.getEndTime(), now)) {
                nextClock.setType(HrmAttendanceClockTypeEnum.OFF_DUTY.getType()).setAttendanceTime(offDutyTime);
                if (now.isBefore(offDutyTime)) {
                    return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.EARLY.getStatus());
                }
                return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.NORMAL.getStatus());
            }
            return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.NOT_YET.getStatus());
        }

        // 已打上班卡：引导下班；已打过下班卡时允许更新
        nextClock.setType(HrmAttendanceClockTypeEnum.OFF_DUTY.getType());
        nextClock.setAttendanceTime(offDutyTime);
        if (clockOutRange == null || LocalDateTimeUtils.isNotBetween(
                clockOutRange.getStartTime(), clockOutRange.getEndTime(), now)) {
            return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.NOT_YET.getStatus());
        }
        if (offDutyClock != null) {
            return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.UPDATE.getStatus());
        }
        if (now.isBefore(offDutyTime)) {
            return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.EARLY.getStatus());
        }
        return nextClock.setButtonStatus(HrmAttendanceClockButtonStatusEnum.NORMAL.getStatus());
    }

    private List<HrmPortalAttendanceClockDetailRespVO.TimelineItem> buildTimeline(
            HrmPortalAttendanceClockDetailRespVO detail,
            List<HrmAttendanceClockDO> dayClocks, LocalDateTime now) {
        if (ObjUtil.hasEmpty(detail.getShift(), detail.getOnDutyAttendanceTime(), detail.getOffDutyAttendanceTime())) {
            return Collections.emptyList();
        }
        HrmPortalAttendanceClockDetailRespVO.TimelineItem onDutyItem = buildTimelineItem(
                HrmAttendanceClockTypeEnum.ON_DUTY.getType(), detail.getOnDutyAttendanceTime(),
                CollUtil.findOne(dayClocks, clock -> ObjUtil.equals(
                        clock.getType(), HrmAttendanceClockTypeEnum.ON_DUTY.getType())), now);
        HrmPortalAttendanceClockDetailRespVO.TimelineItem offDutyItem = buildTimelineItem(
                HrmAttendanceClockTypeEnum.OFF_DUTY.getType(), detail.getOffDutyAttendanceTime(),
                CollUtil.findOne(dayClocks, clock -> ObjUtil.equals(
                        clock.getType(), HrmAttendanceClockTypeEnum.OFF_DUTY.getType())), now);
        return Arrays.asList(onDutyItem, offDutyItem);
    }

    /**
     * 构建打卡时间线条目
     *
     * @param type 打卡类型
     * @param attendanceTime 应打卡时间
     * @param clock 实际打卡记录
     * @param now 当前时间
     * @return 打卡时间线条目
     */
    private HrmPortalAttendanceClockDetailRespVO.TimelineItem buildTimelineItem(
            Integer type, LocalDateTime attendanceTime, HrmAttendanceClockDO clock, LocalDateTime now) {
        HrmPortalAttendanceClockDetailRespVO.TimelineItem item = new HrmPortalAttendanceClockDetailRespVO.TimelineItem()
                .setType(type).setAttendanceTime(attendanceTime);
        if (clock != null) {
            return item.setClockTime(clock.getClockTime()).setStatus(clock.getStatus())
                    .setMissCard(false).setAddress(clock.getAddress());
        }
        // 应打卡时间已过后仍无记录，展示缺卡
        boolean missCard = LocalDateTimeUtils.isAfterOrEqual(now, attendanceTime);
        return item.setMissCard(missCard)
                .setStatus(missCard ? HrmAttendanceClockStatusEnum.MISS_CARD.getStatus() : null);
    }

    /**
     * 校验手机打卡地点 / WiFi
     *
     * 仅开启定位时必须命中地点；仅开启 WiFi 时必须命中 WiFi；两者都开时满足其一即可。
     */
    private void validateMobileClockLocationAndWifi(
            HrmAttendanceGroupDO group, HrmPortalAttendanceClockCreateReqVO createReqVO) {
        boolean openPoint = Boolean.TRUE.equals(group.getOpenPointCard());
        boolean openWifi = Boolean.TRUE.equals(group.getOpenWifiCard());
        if (!openPoint && !openWifi) {
            return;
        }
        if ((openPoint && isPointMatched(group.getPoints(), createReqVO))
                || (openWifi && isWifiMatched(group.getWifis(), createReqVO))) {
            return;
        }
        if (openPoint) {
            throw exception(ATTENDANCE_CLOCK_POINT_INVALID);
        }
        throw exception(ATTENDANCE_CLOCK_WIFI_INVALID);
    }

    private boolean isPointMatched(List<HrmAttendanceGroupDO.Point> points,
                                   HrmPortalAttendanceClockCreateReqVO createReqVO) {
        if (ObjUtil.hasEmpty(points, createReqVO.getLatitude(), createReqVO.getLongitude())) {
            return false;
        }
        double latitude = createReqVO.getLatitude().doubleValue();
        double longitude = createReqVO.getLongitude().doubleValue();
        return CollUtil.anyMatch(points, point -> {
            // NumberUtils.getDistance 返回千米，考勤半径配置为米
            double distanceMeter = NumberUtils.getDistance(latitude, longitude,
                    point.getLatitude().doubleValue(), point.getLongitude().doubleValue()) * 1000;
            return distanceMeter <= point.getRadius();
        });
    }

    private boolean isWifiMatched(List<HrmAttendanceGroupDO.Wifi> wifis,
                                  HrmPortalAttendanceClockCreateReqVO createReqVO) {
        String requestMac = createReqVO.getMac();
        String requestSsid = createReqVO.getSsid();
        if (CollUtil.isEmpty(wifis) || ObjUtil.isAllEmpty(requestMac, requestSsid)) {
            return false;
        }
        return CollUtil.anyMatch(wifis, wifi -> ObjUtil.equals(requestMac, wifi.getMac())
                || ObjUtil.equals(requestSsid, wifi.getSsid()));
    }

}
