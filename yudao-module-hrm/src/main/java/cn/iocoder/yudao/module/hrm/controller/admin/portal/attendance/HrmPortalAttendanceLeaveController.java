package cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCancelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端请假")
@RestController
@RequestMapping("/hrm/portal/attendance/leave")
@Validated
public class HrmPortalAttendanceLeaveController {

    @Resource
    private HrmAttendanceLeaveService attendanceLeaveService;

    @GetMapping("/list")
    @Operation(summary = "获得我的请假申请列表")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmAttendanceLeaveRespVO>> getAttendanceLeaveList() {
        List<HrmAttendanceLeaveDO> list = attendanceLeaveService.getMyAttendanceLeaveList(getLoginUserId());
        return success(BeanUtils.toBean(list, HrmAttendanceLeaveRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建我的请假申请")
    @PreAuthorize("@ss.hasPermission('hrm:portal:attendance:leave')")
    public CommonResult<Long> createAttendanceLeave(
            @Valid @RequestBody HrmAttendanceLeaveCreateReqVO reqVO) {
        return success(attendanceLeaveService.createLeave(getLoginUserId(), reqVO));
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消我的请假申请")
    @PreAuthorize("@ss.hasPermission('hrm:portal:attendance:leave')")
    public CommonResult<Boolean> cancelAttendanceLeave(
            @Valid @RequestBody HrmAttendanceLeaveCancelReqVO reqVO) {
        attendanceLeaveService.cancelLeave(getLoginUserId(), reqVO);
        return success(true);
    }

}
