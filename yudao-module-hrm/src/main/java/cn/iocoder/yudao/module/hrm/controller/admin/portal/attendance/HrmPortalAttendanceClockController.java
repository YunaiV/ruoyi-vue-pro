package cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceClockService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端考勤打卡")
@RestController
@RequestMapping("/hrm/portal/attendance/clock")
@Validated
public class HrmPortalAttendanceClockController {

    private static final DateTimeFormatter SHIFT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmAttendanceClockService attendanceClockService;

    @GetMapping("/get-detail")
    @Operation(summary = "获得我的打卡详情")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPortalAttendanceClockDetailRespVO> getAttendanceClockDetail() {
        HrmPortalAttendanceClockDetailRespVO detail =
                attendanceClockService.getMyAttendanceClockDetail(getLoginUserId());
        return success(buildAttendanceClockDetailRespVO(detail));
    }

    @PostMapping("/create")
    @Operation(summary = "手机端打卡")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<Long> createAttendanceClock(
            @Valid @RequestBody HrmPortalAttendanceClockCreateReqVO createReqVO) {
        return success(attendanceClockService.createMyAttendanceClock(getLoginUserId(), createReqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得我的考勤记录")
    @Parameters({
            @Parameter(name = "year", description = "年份", example = "2026"),
            @Parameter(name = "month", description = "月份", example = "8")
    })
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmPortalAttendanceRecordRespVO>> getAttendanceRecordList(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        // 1. 计算所选月份的闭区间
        YearMonth queryMonth = year == null || month == null
                ? YearMonth.now() : YearMonth.of(year, month);
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                queryMonth.getYear(), queryMonth.getMonthValue());
        // 2. 查询当前员工在所选月份的打卡记录
        List<HrmAttendanceClockDO> list = attendanceClockService.getAttendanceClockListByEmployeeIdAndClockTime(
                        employee.getId(), monthTimes);
        return success(BeanUtils.toBean(list, HrmPortalAttendanceRecordRespVO.class));
    }

    // ==================== 拼接 VO ====================

    private HrmPortalAttendanceClockDetailRespVO buildAttendanceClockDetailRespVO(
            HrmPortalAttendanceClockDetailRespVO detail) {
        HrmAttendanceGroupDO group = detail.getGroup();
        detail.setGroupName(group.getName())
                .setOpenPointCard(Boolean.TRUE.equals(group.getOpenPointCard()))
                .setOpenWifiCard(Boolean.TRUE.equals(group.getOpenWifiCard()))
                .setShiftTitle(buildShiftTitle(detail.getShift()))
                .setPoints(Boolean.TRUE.equals(group.getOpenPointCard())
                        ? BeanUtils.toBean(CollUtil.emptyIfNull(group.getPoints()),
                        HrmPortalAttendanceClockDetailRespVO.Point.class) : Collections.emptyList())
                .setWifis(Boolean.TRUE.equals(group.getOpenWifiCard())
                        ? BeanUtils.toBean(CollUtil.emptyIfNull(group.getWifis()),
                        HrmPortalAttendanceClockDetailRespVO.Wifi.class) : Collections.emptyList());
        return detail;
    }

    /**
     * 构建班次展示标题
     *
     * @param shift 班次配置
     * @return 班次标题
     */
    private String buildShiftTitle(HrmAttendanceGroupDO.Shift shift) {
        if (shift == null || shift.getStartTime() == null || shift.getEndTime() == null) {
            return "休息";
        }
        String title = shift.getStartTime().format(SHIFT_TIME_FORMATTER)
                + "-" + shift.getEndTime().format(SHIFT_TIME_FORMATTER);
        if (shift.getRestStartTime() != null && shift.getRestEndTime() != null) {
            title = title + " 休息:" + shift.getRestStartTime().format(SHIFT_TIME_FORMATTER)
                    + "-" + shift.getRestEndTime().format(SHIFT_TIME_FORMATTER);
        }
        return title;
    }

}
