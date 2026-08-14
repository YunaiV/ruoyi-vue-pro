package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changerecord.HrmSalaryChangeRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
import cn.iocoder.yudao.module.hrm.service.salary.employee.HrmSalaryChangeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - HRM 定薪/调薪记录")
@RestController
@RequestMapping("/hrm/salary/change-record")
@Validated
public class HrmSalaryChangeRecordController {

    @Resource
    private HrmSalaryChangeRecordService salaryChangeRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得定薪/调薪记录")
    @Parameter(name = "id", description = "记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:query')")
    public CommonResult<HrmSalaryChangeRecordRespVO> getSalaryChangeRecord(@RequestParam("id") Long id) {
        return success(buildSalaryChangeRecordRespVO(
                salaryChangeRecordService.getSalaryChangeRecord(id)));
    }

    @GetMapping("/list")
    @Operation(summary = "获得定薪/调薪记录列表")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:query')")
    public CommonResult<List<HrmSalaryChangeRecordRespVO>> getSalaryChangeRecordList(
            @RequestParam("employeeId") Long employeeId) {
        return success(convertList(salaryChangeRecordService.getSalaryChangeRecordList(employeeId),
                this::buildSalaryChangeRecordRespVO));
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消定薪/调薪记录")
    @Parameter(name = "id", description = "记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:update')")
    public CommonResult<Boolean> cancelSalaryChangeRecord(@RequestParam("id") Long id) {
        salaryChangeRecordService.cancelSalaryChangeRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除定薪/调薪记录")
    @Parameter(name = "id", description = "记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:change-record:delete')")
    public CommonResult<Boolean> deleteSalaryChangeRecord(@RequestParam("id") Long id) {
        salaryChangeRecordService.deleteSalaryChangeRecord(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private HrmSalaryChangeRecordRespVO buildSalaryChangeRecordRespVO(
            HrmSalaryChangeRecordDO changeRecord) {
        if (changeRecord == null) {
            return null;
        }
        HrmSalaryChangeRecordRespVO respVO = BeanUtils.toBean(changeRecord, HrmSalaryChangeRecordRespVO.class);
        respVO.setRecordType(changeRecord.getType());
        respVO.setChangeReason(changeRecord.getReason());
        HrmSalaryChangeRecordTypeEnum recordType =
                HrmSalaryChangeRecordTypeEnum.valueOf(changeRecord.getType());
        respVO.setRecordTypeName(recordType == null ? null : recordType.getName());
        return respVO;
    }

}
