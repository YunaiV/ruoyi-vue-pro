package cn.iocoder.yudao.module.hrm.controller.admin.attendance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.service.attendance.config.HrmAttendanceGroupService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - HRM 考勤组")
@RestController
@RequestMapping("/hrm/attendance/group")
@Validated
public class HrmAttendanceGroupController {

    @Resource
    private HrmAttendanceGroupService attendanceGroupService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建考勤组")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:create')")
    public CommonResult<Long> createAttendanceGroup(@Valid @RequestBody HrmAttendanceGroupSaveReqVO createReqVO) {
        return success(attendanceGroupService.createAttendanceGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改考勤组")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:update')")
    public CommonResult<Boolean> updateAttendanceGroup(@Valid @RequestBody HrmAttendanceGroupSaveReqVO updateReqVO) {
        attendanceGroupService.updateAttendanceGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除考勤组")
    @Parameter(name = "id", description = "考勤组编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:delete')")
    public CommonResult<Boolean> deleteAttendanceGroup(@RequestParam("id") Long id) {
        attendanceGroupService.deleteAttendanceGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得考勤组")
    @Parameter(name = "id", description = "考勤组编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:query')")
    public CommonResult<HrmAttendanceGroupRespVO> getAttendanceGroup(@RequestParam("id") Long id) {
        return success(buildAttendanceGroupRespVO(attendanceGroupService.getAttendanceGroup(id)));
    }

    @GetMapping("/my")
    @Operation(summary = "获得员工所在考勤组")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:query')")
    public CommonResult<HrmAttendanceGroupRespVO> getMyAttendanceGroup(@RequestParam("employeeId") Long employeeId) {
        return success(buildAttendanceGroupRespVO(attendanceGroupService.getMyAttendanceGroup(employeeId)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得考勤组分页")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:query')")
    public CommonResult<PageResult<HrmAttendanceGroupRespVO>> getAttendanceGroupPage(
            @Validated HrmAttendanceGroupPageReqVO pageReqVO) {
        PageResult<HrmAttendanceGroupDO> pageResult = attendanceGroupService.getAttendanceGroupPage(pageReqVO);
        return success(new PageResult<>(
                buildAttendanceGroupRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得考勤组精简列表")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:group:query')")
    public CommonResult<List<HrmAttendanceGroupRespVO>> getAttendanceGroupSimpleList() {
        return success(convertList(attendanceGroupService.getAttendanceGroupList(),
                group -> new HrmAttendanceGroupRespVO().setId(group.getId()).setName(group.getName())));
    }

    // ==================== 拼接 VO ====================

    private HrmAttendanceGroupRespVO buildAttendanceGroupRespVO(HrmAttendanceGroupDO group) {
        if (group == null) {
            return null;
        }
        return buildAttendanceGroupRespVOList(Collections.singletonList(group)).get(0);
    }

    private List<HrmAttendanceGroupRespVO> buildAttendanceGroupRespVOList(List<HrmAttendanceGroupDO> list) {
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(list, HrmAttendanceGroupDO::getDeptIds, Collection::stream));
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSetByFlatMap(list, HrmAttendanceGroupDO::getEmployeeIds, Collection::stream));
        return convertList(list, group -> BeanUtils.toBean(group, HrmAttendanceGroupRespVO.class)
                .setDeptNames(convertList(group.getDeptIds(),
                        deptId -> deptMap.containsKey(deptId) ? deptMap.get(deptId).getName() : null))
                .setEmployeeNames(convertList(group.getEmployeeIds(),
                        employeeId -> employeeMap.containsKey(employeeId)
                                ? employeeMap.get(employeeId).getName() : null)));
    }

}
