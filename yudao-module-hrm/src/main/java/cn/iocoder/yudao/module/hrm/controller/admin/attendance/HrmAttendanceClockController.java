package cn.iocoder.yudao.module.hrm.controller.admin.attendance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.TimeRange;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockShiftReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockShiftRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceClockService;
import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceGroupService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 打卡记录")
@RestController
@RequestMapping("/hrm/attendance/clock")
@Validated
public class HrmAttendanceClockController {

    @Resource
    private HrmAttendanceClockService attendanceClockService;
    @Resource
    private HrmAttendanceGroupService attendanceGroupService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建打卡记录")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:create')")
    public CommonResult<Long> createAttendanceClock(@Valid @RequestBody HrmAttendanceClockSaveReqVO createReqVO) {
        return success(attendanceClockService.createAttendanceClock(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改打卡记录")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:update')")
    public CommonResult<Boolean> updateAttendanceClock(@Valid @RequestBody HrmAttendanceClockSaveReqVO updateReqVO) {
        attendanceClockService.updateAttendanceClock(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除打卡记录")
    @Parameter(name = "id", description = "打卡记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:delete')")
    public CommonResult<Boolean> deleteAttendanceClock(@RequestParam("id") Long id) {
        attendanceClockService.deleteAttendanceClock(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除打卡记录")
    @Parameter(name = "ids", description = "打卡记录编号列表", required = true, example = "1024,1025")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:delete')")
    public CommonResult<Boolean> deleteAttendanceClockList(
            @RequestParam("ids") @NotEmpty(message = "打卡记录不能为空") List<Long> ids) {
        attendanceClockService.deleteAttendanceClockList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得打卡记录")
    @Parameter(name = "id", description = "打卡记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:query')")
    public CommonResult<HrmAttendanceClockRespVO> getAttendanceClock(@RequestParam("id") Long id) {
        HrmAttendanceClockDO attendanceClock = attendanceClockService.getAttendanceClock(id);
        return success(attendanceClock == null ? null
                : CollUtil.getFirst(buildAttendanceClockRespVOList(Collections.singletonList(attendanceClock))));
    }

    @GetMapping("/get-shift")
    @Operation(summary = "获得员工实际班次和允许打卡时间")
    @PreAuthorize("@ss.hasAnyPermissions('hrm:attendance:clock:create', 'hrm:attendance:clock:update')")
    public CommonResult<HrmAttendanceClockShiftRespVO> getAttendanceClockShift(
            @Valid HrmAttendanceClockShiftReqVO reqVO) {
        HrmAttendanceGroupDO.Shift shift = attendanceGroupService.getEmployeeAttendanceShift(
                reqVO.getEmployeeId(), reqVO.getAttendanceTime());
        return success(shift == null ? null : buildAttendanceClockShiftRespVO(reqVO, shift));
    }

    @GetMapping("/page")
    @Operation(summary = "获得打卡记录分页")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:query')")
    public CommonResult<PageResult<HrmAttendanceClockRespVO>> getAttendanceClockPage(
            @Validated HrmAttendanceClockPageReqVO pageReqVO) {
        PageResult<HrmAttendanceClockDO> pageResult = attendanceClockService.getAttendanceClockPage(pageReqVO);
        return success(new PageResult<>(buildAttendanceClockRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出打卡记录")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportAttendanceClock(@Validated HrmAttendanceClockPageReqVO exportReqVO,
                                      HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<HrmAttendanceClockRespVO> list = buildAttendanceClockRespVOList(
                attendanceClockService.getAttendanceClockList(exportReqVO));
        ExcelUtils.write(response, "打卡记录.xls", "数据", HrmAttendanceClockRespVO.class, list);
    }

    // ==================== 拼接 VO ====================

    private HrmAttendanceClockShiftRespVO buildAttendanceClockShiftRespVO(
            HrmAttendanceClockShiftReqVO reqVO, HrmAttendanceGroupDO.Shift shift) {
        if (shift.getStartTime() == null || shift.getEndTime() == null
                || shift.getClockInStartTime() == null || shift.getClockInEndTime() == null
                || shift.getClockOutStartTime() == null || shift.getClockOutEndTime() == null) {
            return null;
        }
        LocalDate attendanceDate = reqVO.getAttendanceTime().toLocalDate();
        TimeRange shiftTimeRange = CollUtil.getFirst(LocalDateTimeUtils.buildDailyTimeRanges(
                attendanceDate, attendanceDate, shift.getStartTime(), shift.getEndTime()));
        TimeRange clockInTimeRange = LocalDateTimeUtils.findDailyTimeRange(
                attendanceDate, shift.getClockInStartTime(),
                shift.getClockInEndTime(), shiftTimeRange.getStartTime());
        TimeRange clockOutTimeRange = LocalDateTimeUtils.findDailyTimeRange(
                attendanceDate, shift.getClockOutStartTime(),
                shift.getClockOutEndTime(), shiftTimeRange.getEndTime());
        if (clockInTimeRange == null || clockOutTimeRange == null) {
            return null;
        }
        return new HrmAttendanceClockShiftRespVO()
                .setStartTime(shiftTimeRange.getStartTime()).setEndTime(shiftTimeRange.getEndTime())
                .setClockInStartTime(clockInTimeRange.getStartTime()).setClockInEndTime(clockInTimeRange.getEndTime())
                .setClockOutStartTime(clockOutTimeRange.getStartTime()).setClockOutEndTime(clockOutTimeRange.getEndTime());
    }

    private List<HrmAttendanceClockRespVO> buildAttendanceClockRespVOList(List<HrmAttendanceClockDO> clocks) {
        if (CollUtil.isEmpty(clocks)) {
            return Collections.emptyList();
        }
        // 1. 获得员工和部门信息
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(clocks, HrmAttendanceClockDO::getEmployeeId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId));

        // 2. 拼接响应
        return BeanUtils.toBean(clocks, HrmAttendanceClockRespVO.class, respVO -> {
            MapUtils.findAndThen(employeeMap, respVO.getEmployeeId(), employee -> {
                respVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                        .setDeptId(employee.getDeptId()).setPostName(employee.getPostName());
            });
            MapUtils.findAndThen(deptMap, respVO.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
        });
    }

}
