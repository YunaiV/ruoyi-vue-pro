package cn.iocoder.yudao.module.oa.service.attendance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.count;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ATTENDANCE_CLOCK_TIME_INVALID;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ATTENDANCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ATTENDANCE_STATUS_INVALID;

/**
 * OA 考勤 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaAttendanceServiceImpl implements OaAttendanceService {

    @Resource
    private OaAttendanceMapper attendanceMapper;

    @Resource
    private OaProperties properties;

    @Resource
    private OaLeaveApplyService leaveApplyService;
    @Resource
    private OaTravelApplyService travelApplyService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public void createApplyAttendance(Long userId, OaAttendanceTypeEnum type, LocalDateTime startTime) {
        // 1. 请假、出差记录沿用申请开始时间
        Integer status = type == OaAttendanceTypeEnum.LEAVE
                ? OaAttendanceStatusEnum.LEAVE.getStatus() : OaAttendanceStatusEnum.TRAVEL.getStatus();

        // 2. 保存考勤明细，不增加上班或下班打卡次数
        attendanceMapper.insert(new OaAttendanceDO().setUserId(userId).setType(type.getType())
                .setStatus(status).setAttendanceTime(startTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long clockAttendance(Long userId, String attendanceIp) {
        // 1. 仅允许最早打卡时间之后、标准下班时间之前打卡，不包含边界
        LocalDateTime clockTime = LocalDateTime.now().withNano(0);
        if (!clockTime.toLocalTime().isAfter(properties.getAttendance().getClockBeginTime())
                || !clockTime.toLocalTime().isBefore(properties.getAttendance().getWorkEndTime())) {
            throw exception(ATTENDANCE_CLOCK_TIME_INVALID,
                    properties.getAttendance().getClockBeginTime(), properties.getAttendance().getWorkEndTime());
        }

        // 2. 查询用户当天打卡记录，判断当前操作是上班打卡还是下班打卡
        List<OaAttendanceDO> attendances = getAttendanceListByUserIdAndDate(userId, clockTime.toLocalDate());
        OaAttendanceDO clockIn = CollUtil.findOne(attendances,
                attendance -> ObjUtil.equals(attendance.getType(), OaAttendanceTypeEnum.CLOCK_IN.getType()));

        // 3.1 情况一：首次打卡创建上班记录
        if (clockIn == null) {
            OaAttendanceDO attendance = new OaAttendanceDO().setUserId(userId)
                    .setType(OaAttendanceTypeEnum.CLOCK_IN.getType())
                    .setStatus(calculateAttendanceStatus(OaAttendanceTypeEnum.CLOCK_IN, clockTime))
                    .setAttendanceTime(clockTime).setAttendanceIp(attendanceIp);
            attendanceMapper.insert(attendance);
            return attendance.getId();
        }

        // 3.2 情况二：首次下班打卡创建下班记录
        OaAttendanceDO clockOut = CollUtil.findOne(attendances,
                attendance -> ObjUtil.equals(attendance.getType(), OaAttendanceTypeEnum.CLOCK_OUT.getType()));
        if (clockOut == null) {
            OaAttendanceDO attendance = new OaAttendanceDO().setUserId(userId)
                    .setType(OaAttendanceTypeEnum.CLOCK_OUT.getType())
                    .setStatus(calculateAttendanceStatus(OaAttendanceTypeEnum.CLOCK_OUT, clockTime))
                    .setAttendanceTime(clockTime).setAttendanceIp(attendanceIp);
            attendanceMapper.insert(attendance);
            return attendance.getId();
        }

        // 3.3 情况三：重复下班打卡刷新下班时间和状态
        OaAttendanceDO updateObj = new OaAttendanceDO().setId(clockOut.getId())
                .setStatus(calculateAttendanceStatus(OaAttendanceTypeEnum.CLOCK_OUT, clockTime))
                .setAttendanceTime(clockTime).setAttendanceIp(attendanceIp);
        attendanceMapper.updateById(updateObj);
        return clockOut.getId();
    }

    @Override
    public void updateAttendance(OaAttendanceUpdateReqVO updateReqVO, Long operatorId) {
        // 1.1 校验考勤记录存在，且属于当前用户管理范围
        OaAttendanceDO attendance = validateAttendanceExists(updateReqVO.getId(), operatorId);
        // 1.2 校验考勤类型和状态组合
        validateAttendanceStatus(attendance.getType(), updateReqVO.getStatus());

        // 2. 修改考勤状态和备注
        attendanceMapper.updateById(BeanUtils.toBean(updateReqVO, OaAttendanceDO.class));
    }

    @Override
    public void deleteAttendance(Long id, Long operatorId) {
        // 1. 校验考勤记录存在且属于当前用户管理范围
        validateAttendanceExists(id, operatorId);

        // 2. 删除考勤记录
        attendanceMapper.deleteById(id);
    }

    @Override
    public OaAttendanceDO getAttendance(Long id, Long operatorId) {
        return validateAttendanceExists(id, operatorId);
    }

    @Override
    public PageResult<OaAttendanceDO> getAttendancePage(OaAttendancePageReqVO pageReqVO, Long operatorId) {
        // 1. 查询管理范围与员工筛选的交集
        List<Long> userIds = getManagedUserIds(operatorId, pageReqVO.getUserId());
        if (CollUtil.isEmpty(userIds)) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 2. 查询范围内的考勤记录
        return attendanceMapper.selectPageByUserIds(pageReqVO, userIds);
    }

    @Override
    public PageResult<OaAttendanceDO> getMyAttendancePage(OaAttendancePageReqVO pageReqVO, Long userId) {
        return attendanceMapper.selectPageByUserId(pageReqVO, userId);
    }

    @Override
    public List<OaAttendanceDO> getTodayAttendanceList(Long userId) {
        return getAttendanceListByUserIdAndDate(userId, LocalDate.now());
    }

    @Override
    public List<OaAttendanceWeekReportRespVO> getAttendanceWeekReport(OaAttendanceWeekReportReqVO reqVO, Long operatorId) {
        // 1. 查询管理范围内的用户，保留没有打卡记录的用户
        List<Long> userIds = getManagedUserIds(operatorId, reqVO.getUserId());
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        // 2. 查询指定周的考勤记录
        LocalDate startDate = reqVO.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);
        List<OaAttendanceDO> attendances = attendanceMapper.selectListByUserIdsAndAttendanceTime(
                userIds, LocalDateTimeUtils.getDateTimeRange(startDate, endDate));

        // 3. 按用户和日期组装周报
        Map<Long, List<OaAttendanceDO>> userAttendanceMap = convertMultiMap(attendances, OaAttendanceDO::getUserId);
        List<OaAttendanceWeekReportRespVO> reports = new ArrayList<>(userIds.size());
        for (Long userId : userIds) {
            Map<LocalDate, List<OaAttendanceDO>> dailyAttendanceMap = convertMultiMap(
                    userAttendanceMap.getOrDefault(userId, Collections.emptyList()),
                    attendance -> attendance.getAttendanceTime().toLocalDate());
            List<OaAttendanceWeekReportRespVO.DailyAttendance> dailyAttendances = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                dailyAttendances.add(buildDailyAttendance(
                        date, dailyAttendanceMap.getOrDefault(date, Collections.emptyList())));
            }
            reports.add(new OaAttendanceWeekReportRespVO().setUserId(userId).setDailyAttendances(dailyAttendances));
        }
        return reports;
    }

    @Override
    public List<OaAttendanceMonthReportRespVO> getAttendanceMonthReport(OaAttendanceMonthReportReqVO reqVO, Long operatorId) {
        // 1. 查询管理范围内的用户，保留没有打卡记录的用户
        List<Long> userIds = getManagedUserIds(operatorId, reqVO.getUserId());
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        // 2.1 查询指定月份的考勤记录
        LocalDateTime[] monthTime = LocalDateTimeUtils.getMonthDateTimeRange(reqVO.getYear(), reqVO.getMonth());
        List<OaAttendanceDO> attendances = attendanceMapper.selectListByUserIdsAndAttendanceTime(userIds, monthTime);
        // 2.2 批量汇总审批通过的请假和出差天数，整笔归入申请开始月份
        Map<Long, Integer> leaveDaysMap = leaveApplyService.getApprovedLeaveDaysMap(userIds, monthTime);
        Map<Long, Integer> travelDaysMap = travelApplyService.getApprovedTravelDaysMap(userIds, monthTime);

        // 3. 按用户汇总
        boolean pastMonth = YearMonth.of(reqVO.getYear(), reqVO.getMonth())
                .isBefore(YearMonth.from(LocalDateTime.now()));
        Map<Long, List<OaAttendanceDO>> userAttendanceMap = convertMultiMap(attendances, OaAttendanceDO::getUserId);
        List<OaAttendanceMonthReportRespVO> reports = new ArrayList<>(userIds.size());
        for (Long userId : userIds) {
            List<OaAttendanceDO> userAttendances = userAttendanceMap.getOrDefault(userId, Collections.emptyList());
            reports.add(new OaAttendanceMonthReportRespVO().setUserId(userId)
                    .setClockInCount(countAttendanceByType(userAttendances, OaAttendanceTypeEnum.CLOCK_IN))
                    .setClockOutCount(countAttendanceByType(userAttendances, OaAttendanceTypeEnum.CLOCK_OUT))
                    .setNormalCount(countAttendanceByStatus(userAttendances, OaAttendanceStatusEnum.NORMAL))
                    .setLateCount(countAttendanceByStatus(userAttendances, OaAttendanceStatusEnum.LATE))
                    .setEarlyCount(countAttendanceByStatus(userAttendances, OaAttendanceStatusEnum.EARLY))
                    .setLeaveDays(leaveDaysMap.getOrDefault(userId, 0))
                    .setTravelDays(travelDaysMap.getOrDefault(userId, 0))
                    .setAbsentDays(pastMonth ? properties.getAttendance().getMonthlyWorkDays() - countAttendanceByType(userAttendances, OaAttendanceTypeEnum.CLOCK_OUT) : 0));
        }
        return reports;
    }

    /**
     * 校验考勤记录存在且属于当前用户管理范围
     *
     * @param id 考勤记录编号
     * @param userId 当前操作用户编号
     * @return 考勤记录
     */
    private OaAttendanceDO validateAttendanceExists(Long id, Long userId) {
        OaAttendanceDO attendance = attendanceMapper.selectById(id);
        if (attendance == null) {
            throw exception(ATTENDANCE_NOT_EXISTS);
        }
        // 重要：越界记录与不存在记录统一返回，避免泄露其他用户的考勤
        List<Long> managedUserIds = getManagedUserIds(userId, attendance.getUserId());
        if (CollUtil.isEmpty(managedUserIds)) {
            throw exception(ATTENDANCE_NOT_EXISTS);
        }
        return attendance;
    }

    /**
     * 获得当前用户管理范围内的用户编号
     *
     * @param operatorId 当前操作用户编号
     * @param filterUserId 员工筛选编号，未指定时返回全部下属用户
     * @return 管理范围与员工筛选的交集
     */
    private List<Long> getManagedUserIds(Long operatorId, Long filterUserId) {
        List<AdminUserRespDTO> users = adminUserApi.getUserListBySubordinate(operatorId);
        return convertList(users, AdminUserRespDTO::getId,
                user -> filterUserId == null || ObjUtil.equal(user.getId(), filterUserId));
    }

    /**
     * 校验考勤类型与状态组合是否合法
     *
     * @param type 考勤类型
     * @param status 考勤状态
     */
    private void validateAttendanceStatus(Integer type, Integer status) {
        boolean valid = ObjUtil.equals(type, OaAttendanceTypeEnum.CLOCK_IN.getType())
                && (ObjUtil.equals(status, OaAttendanceStatusEnum.NORMAL.getStatus())
                || ObjUtil.equals(status, OaAttendanceStatusEnum.LATE.getStatus()));
        valid = valid || ObjUtil.equals(type, OaAttendanceTypeEnum.CLOCK_OUT.getType())
                && (ObjUtil.equals(status, OaAttendanceStatusEnum.NORMAL.getStatus())
                || ObjUtil.equals(status, OaAttendanceStatusEnum.EARLY.getStatus()));
        valid = valid || ObjUtil.equals(type, OaAttendanceTypeEnum.LEAVE.getType())
                && ObjUtil.equals(status, OaAttendanceStatusEnum.LEAVE.getStatus());
        valid = valid || ObjUtil.equals(type, OaAttendanceTypeEnum.TRAVEL.getType())
                && ObjUtil.equals(status, OaAttendanceStatusEnum.TRAVEL.getStatus());
        if (!valid) {
            throw exception(ATTENDANCE_STATUS_INVALID);
        }
    }

    /**
     * 计算考勤状态
     *
     * @param type 考勤类型
     * @param attendanceTime 考勤时间
     * @return 考勤状态
     */
    private Integer calculateAttendanceStatus(OaAttendanceTypeEnum type, LocalDateTime attendanceTime) {
        // 上班打卡晚于标准上班时间时，状态为迟到
        if (type == OaAttendanceTypeEnum.CLOCK_IN
                && attendanceTime.toLocalTime().isAfter(properties.getAttendance().getWorkBeginTime())) {
            return OaAttendanceStatusEnum.LATE.getStatus();
        }
        // 下班打卡早于标准下班时间时，状态为早退
        if (type == OaAttendanceTypeEnum.CLOCK_OUT
                && attendanceTime.toLocalTime().isBefore(properties.getAttendance().getWorkEndTime())) {
            return OaAttendanceStatusEnum.EARLY.getStatus();
        }
        // 其余打卡均为正常状态
        return OaAttendanceStatusEnum.NORMAL.getStatus();
    }

    /**
     * 获得指定用户和日期的考勤记录列表
     *
     * @param userId 用户编号
     * @param date 日期
     * @return 考勤记录列表
     */
    private List<OaAttendanceDO> getAttendanceListByUserIdAndDate(Long userId, LocalDate date) {
        return attendanceMapper.selectListByUserIdAndAttendanceTime(
                userId, LocalDateTimeUtils.getDateTimeRange(date, date));
    }

    /**
     * 构建指定日期的考勤明细
     *
     * @param date 日期
     * @param attendances 当日考勤记录列表
     * @return 每日考勤明细
     */
    private OaAttendanceWeekReportRespVO.DailyAttendance buildDailyAttendance(
            LocalDate date, List<OaAttendanceDO> attendances) {
        OaAttendanceDO clockIn = CollUtil.findOne(attendances,
                attendance -> ObjUtil.equals(attendance.getType(), OaAttendanceTypeEnum.CLOCK_IN.getType()));
        OaAttendanceDO clockOut = CollUtil.findOne(attendances,
                attendance -> ObjUtil.equals(attendance.getType(), OaAttendanceTypeEnum.CLOCK_OUT.getType()));
        return new OaAttendanceWeekReportRespVO.DailyAttendance().setDate(date)
                .setClockInId(clockIn != null ? clockIn.getId() : null)
                .setClockInTime(clockIn != null ? clockIn.getAttendanceTime() : null)
                .setClockInStatus(clockIn != null ? clockIn.getStatus() : null)
                .setClockOutId(clockOut != null ? clockOut.getId() : null)
                .setClockOutTime(clockOut != null ? clockOut.getAttendanceTime() : null)
                .setClockOutStatus(clockOut != null ? clockOut.getStatus() : null);
    }

    /**
     * 统计指定考勤类型的记录数
     *
     * @param attendances 考勤记录列表
     * @param type 考勤类型
     * @return 考勤记录数
     */
    private int countAttendanceByType(List<OaAttendanceDO> attendances, OaAttendanceTypeEnum type) {
        return (int) count(attendances, attendance -> ObjUtil.equals(attendance.getType(), type.getType()));
    }

    /**
     * 统计指定考勤状态的记录数
     *
     * @param attendances 考勤记录列表
     * @param status 考勤状态
     * @return 考勤记录数
     */
    private int countAttendanceByStatus(List<OaAttendanceDO> attendances, OaAttendanceStatusEnum status) {
        return (int) count(attendances, attendance -> ObjUtil.equals(attendance.getStatus(), status.getStatus()));
    }

}
