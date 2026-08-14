package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard.HrmEmployeeSalaryCardRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard.HrmEmployeeSalaryCardSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeSalaryCardDO;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeSalaryCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 员工工资卡")
@RestController
@RequestMapping("/hrm/employee/salary-card")
@Validated
public class HrmEmployeeSalaryCardController {

    @Resource
    private HrmEmployeeSalaryCardService salaryCardService;

    @GetMapping("/get")
    @Operation(summary = "获得员工工资卡")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:query')")
    public CommonResult<HrmEmployeeSalaryCardRespVO> getEmployeeSalaryCard(
            @RequestParam("employeeId") Long employeeId) {
        HrmEmployeeSalaryCardDO salaryCard = salaryCardService.getSalaryCardByEmployeeId(employeeId);
        if (salaryCard == null) {
            return success(null);
        }
        HrmEmployeeSalaryCardRespVO respVO = BeanUtils.toBean(salaryCard, HrmEmployeeSalaryCardRespVO.class)
                .setBankAreaName(AreaUtils.format(salaryCard.getBankAreaId()));
        return success(respVO);
    }

    @PutMapping("/save")
    @Operation(summary = "保存员工工资卡")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Long> saveEmployeeSalaryCard(
            @Valid @RequestBody HrmEmployeeSalaryCardSaveReqVO reqVO) {
        return success(salaryCardService.saveSalaryCard(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工工资卡")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Boolean> deleteEmployeeSalaryCard(@RequestParam("employeeId") Long employeeId) {
        salaryCardService.deleteSalaryCardByEmployeeId(employeeId);
        return success(true);
    }

}
