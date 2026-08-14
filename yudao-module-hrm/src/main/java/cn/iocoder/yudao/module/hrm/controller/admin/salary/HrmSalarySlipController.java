package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipOptionRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipRemarkReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.slip.HrmSalarySlipService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 工资条")
@RestController
@RequestMapping("/hrm/salary/slip")
@Validated
public class HrmSalarySlipController {

    @Resource
    private HrmSalarySlipService salarySlipService;
    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private DeptApi deptApi;

    @GetMapping("/page")
    @Operation(summary = "获得工资条分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:query')")
    public CommonResult<PageResult<HrmSalarySlipRespVO>> getSalarySlipPage(
            @Validated HrmSalarySlipPageReqVO reqVO) {
        // 1. 根据员工展示条件获得员工编号
        if (StrUtil.isNotBlank(reqVO.getSearch()) || reqVO.getDeptId() != null) {
            HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO();
            employeeReqVO.setSearch(reqVO.getSearch());
            employeeReqVO.setDeptId(reqVO.getDeptId());
            Set<Long> employeeIds = convertSet(
                    employeeService.getEmployeeList(employeeReqVO), HrmEmployeeDO::getId);
            if (CollUtil.isNotEmpty(reqVO.getEmployeeIds())) {
                employeeIds.retainAll(reqVO.getEmployeeIds());
            }
            if (CollUtil.isEmpty(employeeIds)) {
                return success(PageResult.empty());
            }
            reqVO.setEmployeeIds(new ArrayList<>(employeeIds));
        }

        // 2. 查询工资条并拼接响应
        PageResult<HrmSalarySlipDO> pageResult = salarySlipService.getSalarySlipPage(reqVO);
        return success(new PageResult<>(buildSalarySlipRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工资条")
    @Parameter(name = "id", description = "工资条编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:query')")
    public CommonResult<HrmSalarySlipRespVO> getSalarySlip(@RequestParam("id") Long id) {
        return success(buildSalarySlipRespVO(salarySlipService.getSalarySlip(id)));
    }

    @PutMapping("/remark")
    @Operation(summary = "修改工资条备注")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:update')")
    public CommonResult<Boolean> updateSalarySlipRemark(
            @Valid @RequestBody HrmSalarySlipRemarkReqVO reqVO) {
        salarySlipService.updateSalarySlipRemark(reqVO);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private HrmSalarySlipRespVO buildSalarySlipRespVO(HrmSalarySlipDO salarySlip) {
        if (salarySlip == null) {
            return null;
        }
        return CollUtil.getFirst(buildSalarySlipRespVOList(Collections.singletonList(salarySlip)));
    }

    private List<HrmSalarySlipRespVO> buildSalarySlipRespVOList(List<HrmSalarySlipDO> salarySlips) {
        if (CollUtil.isEmpty(salarySlips)) {
            return Collections.emptyList();
        }
        // 1. 获取员工、部门信息
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(salarySlips, HrmSalarySlipDO::getEmployeeId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId));

        // 2. 拼接响应
        return convertList(salarySlips, salarySlip -> {
            HrmSalarySlipRespVO respVO = BeanUtils.toBean(salarySlip, HrmSalarySlipRespVO.class);
            HrmEmployeeDO employee = employeeMap.get(salarySlip.getEmployeeId());
            if (employee != null) {
                respVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                        .setMobile(employee.getMobile()).setDeptId(employee.getDeptId())
                        .setPostName(employee.getPostName());
                MapUtils.findAndThen(deptMap, employee.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            }
            respVO.setOptions(BeanUtils.toBean(salarySlip.getOptions(), HrmSalarySlipOptionRespVO.class));
            return respVO;
        });
    }

}
