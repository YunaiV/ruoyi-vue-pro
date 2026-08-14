package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contract.HrmEmployeeContractRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contract.HrmEmployeeContractSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeContractDO;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeContractService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 员工合同")
@RestController
@RequestMapping("/hrm/employee/contract")
@Validated
public class HrmEmployeeContractController {

    @Resource
    private HrmEmployeeContractService contractService;

    @GetMapping("/list")
    @Operation(summary = "获得员工合同列表")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:query')")
    public CommonResult<List<HrmEmployeeContractRespVO>> getContractList(
            @RequestParam("employeeId") Long employeeId) {
        List<HrmEmployeeContractDO> contracts = contractService.getContractListByEmployeeId(employeeId);
        return success(BeanUtils.toBean(contracts, HrmEmployeeContractRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建员工合同")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Long> createContract(@Valid @RequestBody HrmEmployeeContractSaveReqVO reqVO) {
        return success(contractService.createContract(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新员工合同")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody HrmEmployeeContractSaveReqVO reqVO) {
        contractService.updateContract(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工合同")
    @Parameter(name = "id", description = "合同编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

}
