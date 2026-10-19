package cn.iocoder.yudao.module.oa.controller.admin.attendance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceMonthReportReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceMonthReportRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendancePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceUpdateReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceWeekReportReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendanceWeekReportRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.attendance.OaAttendanceDO;
import cn.iocoder.yudao.module.oa.service.attendance.OaAttendanceService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 考勤")
@RestController
@RequestMapping("/oa/attendance")
@Validated
public class OaAttendanceController {

    @Resource
    private OaAttendanceService attendanceService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    // ==================== 我的考勤 ====================

    @PostMapping("/clock")
    @Operation(summary = "执行当前用户打卡")
    public CommonResult<Long> clockAttendance() {
        return success(attendanceService.clockAttendance(getLoginUserId(), getClientIP()));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得我的考勤分页列表")
    public CommonResult<PageResult<OaAttendanceRespVO>> getMyAttendancePage(
            @Valid OaAttendancePageReqVO pageReqVO) {
        PageResult<OaAttendanceDO> pageResult = attendanceService.getMyAttendancePage(
                pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildAttendanceRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/my-today-list")
    @Operation(summary = "获得我的今日考勤列表")
    public CommonResult<List<OaAttendanceRespVO>> getMyTodayAttendanceList() {
        return success(buildAttendanceRespVOList(
                attendanceService.getTodayAttendanceList(getLoginUserId())));
    }

    // ==================== 考勤管理 ====================

    @PutMapping("/update")
    @Operation(summary = "更新考勤记录")
    @PreAuthorize("@ss.hasPermission('oa:attendance:update')")
    public CommonResult<Boolean> updateAttendance(@Valid @RequestBody OaAttendanceUpdateReqVO updateReqVO) {
        attendanceService.updateAttendance(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除考勤记录")
    @Parameter(name = "id", description = "考勤记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:attendance:delete')")
    public CommonResult<Boolean> deleteAttendance(@RequestParam("id") Long id) {
        attendanceService.deleteAttendance(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得考勤详情")
    @Parameter(name = "id", description = "考勤记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:attendance:query')")
    public CommonResult<OaAttendanceRespVO> getAttendance(@RequestParam("id") Long id) {
        OaAttendanceDO attendance = attendanceService.getAttendance(id, getLoginUserId());
        return success(buildAttendanceRespVO(attendance));
    }

    @GetMapping("/page")
    @Operation(summary = "获得考勤分页列表")
    @PreAuthorize("@ss.hasPermission('oa:attendance:query')")
    public CommonResult<PageResult<OaAttendanceRespVO>> getAttendancePage(
            @Valid OaAttendancePageReqVO pageReqVO) {
        PageResult<OaAttendanceDO> pageResult = attendanceService.getAttendancePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildAttendanceRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 考勤报表 ====================

    @GetMapping("/week-report")
    @Operation(summary = "获得考勤周报")
    @PreAuthorize("@ss.hasPermission('oa:attendance:query')")
    public CommonResult<List<OaAttendanceWeekReportRespVO>> getAttendanceWeekReport(
            @Valid OaAttendanceWeekReportReqVO reqVO) {
        return success(buildAttendanceWeekReportRespVOList(
                attendanceService.getAttendanceWeekReport(reqVO, getLoginUserId())));
    }

    @GetMapping("/month-report")
    @Operation(summary = "获得考勤月报")
    @PreAuthorize("@ss.hasPermission('oa:attendance:query')")
    public CommonResult<List<OaAttendanceMonthReportRespVO>> getAttendanceMonthReport(
            @Valid OaAttendanceMonthReportReqVO reqVO) {
        return success(buildAttendanceMonthReportRespVOList(
                attendanceService.getAttendanceMonthReport(reqVO, getLoginUserId())));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接考勤详情
     *
     * @param attendance 考勤记录
     * @return 考勤响应
     */
    private OaAttendanceRespVO buildAttendanceRespVO(OaAttendanceDO attendance) {
        if (attendance == null) {
            return null;
        }
        return CollUtil.getFirst(buildAttendanceRespVOList(Collections.singletonList(attendance)));
    }

    /**
     * 拼接考勤响应列表
     *
     * @param attendances 考勤记录列表
     * @return 考勤响应列表
     */
    private List<OaAttendanceRespVO> buildAttendanceRespVOList(List<OaAttendanceDO> attendances) {
        if (CollUtil.isEmpty(attendances)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(attendances, OaAttendanceDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return BeanUtils.toBean(attendances, OaAttendanceRespVO.class, attendance ->
                fillAttendanceUserInfo(attendance, userMap, deptMap));
    }

    /**
     * 拼接考勤周报响应列表
     *
     * @param reports 考勤周报列表
     * @return 包含用户和部门信息的考勤周报列表
     */
    private List<OaAttendanceWeekReportRespVO> buildAttendanceWeekReportRespVOList(
            List<OaAttendanceWeekReportRespVO> reports) {
        if (CollUtil.isEmpty(reports)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(reports, OaAttendanceWeekReportRespVO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        reports.forEach(report -> fillAttendanceUserInfo(report, userMap, deptMap));
        return reports;
    }

    /**
     * 拼接考勤月报响应列表
     *
     * @param reports 考勤月报列表
     * @return 包含用户和部门信息的考勤月报列表
     */
    private List<OaAttendanceMonthReportRespVO> buildAttendanceMonthReportRespVOList(
            List<OaAttendanceMonthReportRespVO> reports) {
        if (CollUtil.isEmpty(reports)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(reports, OaAttendanceMonthReportRespVO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        reports.forEach(report -> fillAttendanceUserInfo(report, userMap, deptMap));
        return reports;
    }

    /**
     * 拼接考勤响应的用户和部门信息
     *
     * @param attendance 考勤响应
     * @param userMap 用户 Map
     * @param deptMap 部门 Map
     */
    private void fillAttendanceUserInfo(OaAttendanceRespVO attendance,
                                        Map<Long, AdminUserRespDTO> userMap,
                                        Map<Long, DeptRespDTO> deptMap) {
        MapUtils.findAndThen(userMap, attendance.getUserId(), user -> {
            attendance.setUserName(user.getNickname()).setDeptId(user.getDeptId());
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> attendance.setDeptName(dept.getName()));
        });
    }

    /**
     * 拼接考勤周报响应的用户和部门信息
     *
     * @param report 考勤周报响应
     * @param userMap 用户 Map
     * @param deptMap 部门 Map
     */
    private void fillAttendanceUserInfo(OaAttendanceWeekReportRespVO report,
                                        Map<Long, AdminUserRespDTO> userMap,
                                        Map<Long, DeptRespDTO> deptMap) {
        MapUtils.findAndThen(userMap, report.getUserId(), user -> {
            report.setUserName(user.getNickname()).setDeptId(user.getDeptId());
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> report.setDeptName(dept.getName()));
        });
    }

    /**
     * 拼接考勤月报响应的用户和部门信息
     *
     * @param report 考勤月报响应
     * @param userMap 用户 Map
     * @param deptMap 部门 Map
     */
    private void fillAttendanceUserInfo(OaAttendanceMonthReportRespVO report,
                                        Map<Long, AdminUserRespDTO> userMap,
                                        Map<Long, DeptRespDTO> deptMap) {
        MapUtils.findAndThen(userMap, report.getUserId(), user -> {
            report.setUserName(user.getNickname()).setDeptId(user.getDeptId());
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> report.setDeptName(dept.getName()));
        });
    }

}
