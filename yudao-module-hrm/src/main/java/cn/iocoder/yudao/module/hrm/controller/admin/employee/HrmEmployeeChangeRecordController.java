package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeChangeRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - HRM 员工异动记录")
@RestController
@RequestMapping("/hrm/employee/change-record")
@Validated
public class HrmEmployeeChangeRecordController {

    @Resource
    private HrmEmployeeChangeRecordService changeRecordService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/list")
    @Operation(summary = "获得员工异动记录列表")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:query')")
    public CommonResult<List<HrmEmployeeChangeRecordRespVO>> getEmployeeChangeRecordList(
            @RequestParam("employeeId") Long employeeId) {
        return success(buildEmployeeChangeRecordRespVOList(
                changeRecordService.getEmployeeChangeRecordListByEmployeeId(employeeId)));
    }

    // ==================== 拼接 VO ====================

    private List<HrmEmployeeChangeRecordRespVO> buildEmployeeChangeRecordRespVOList(
            List<HrmEmployeeChangeRecordDO> changeRecords) {
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(changeRecords, changeRecord -> Stream.of(changeRecord.getOldDeptId(), changeRecord.getNewDeptId())));
        Map<Long, HrmEmployeeDO> leaderEmployeeMap = employeeService.getEmployeeMap(
                convertSetByFlatMap(changeRecords, changeRecord -> Stream.of(changeRecord.getOldLeaderEmployeeId(), changeRecord.getNewLeaderEmployeeId())));
        return BeanUtils.toBean(changeRecords, HrmEmployeeChangeRecordRespVO.class, changeRecord -> {
            MapUtils.findAndThen(deptMap, changeRecord.getOldDeptId(),
                    dept -> changeRecord.setOldDeptName(dept.getName()));
            MapUtils.findAndThen(deptMap, changeRecord.getNewDeptId(),
                    dept -> changeRecord.setNewDeptName(dept.getName()));
            MapUtils.findAndThen(leaderEmployeeMap, changeRecord.getOldLeaderEmployeeId(),
                    leader -> changeRecord.setOldLeaderEmployeeName(leader.getName()));
            MapUtils.findAndThen(leaderEmployeeMap, changeRecord.getNewLeaderEmployeeId(),
                    leader -> changeRecord.setNewLeaderEmployeeName(leader.getName()));
        });
    }

}
