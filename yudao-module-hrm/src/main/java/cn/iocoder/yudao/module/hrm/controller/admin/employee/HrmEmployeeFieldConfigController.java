package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeArchiveFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeCreateFieldConfigSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigRespVO;
import cn.iocoder.yudao.module.hrm.service.employee.config.HrmEmployeeFieldConfigService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 员工字段配置")
@RestController
@RequestMapping("/hrm/employee/config")
@Validated
public class HrmEmployeeFieldConfigController {

    @Resource
    private HrmEmployeeFieldConfigService employeeFieldConfigService;

    @GetMapping("/create-field/list")
    @Operation(summary = "获得新建员工字段配置")
    @Parameter(name = "entryStatus", description = "入职状态：1 在职，2 待入职", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('hrm:employee:config:query') or @ss.hasPermission('hrm:employee:create')")
    public CommonResult<List<HrmEmployeeFieldConfigRespVO>> getEmployeeCreateFieldConfigList(
            @RequestParam("entryStatus") Integer entryStatus) {
        return success(employeeFieldConfigService.getEmployeeCreateFieldConfigList(entryStatus));
    }

    @PutMapping("/create-field/save")
    @Operation(summary = "保存新建员工字段配置")
    @PreAuthorize("@ss.hasPermission('hrm:employee:config:update')")
    public CommonResult<Boolean> saveEmployeeCreateFieldConfig(
            @Valid @RequestBody HrmEmployeeCreateFieldConfigSaveReqVO reqVO) {
        employeeFieldConfigService.saveEmployeeCreateFieldConfig(reqVO);
        return success(true);
    }

    @GetMapping("/archive-field/list")
    @Operation(summary = "获得员工档案字段配置")
    @PreAuthorize("@ss.hasPermission('hrm:employee:config:query')")
    public CommonResult<List<HrmEmployeeFieldConfigRespVO>> getEmployeeArchiveFieldConfigList() {
        return success(employeeFieldConfigService.getEmployeeArchiveFieldConfigList());
    }

    @PutMapping("/archive-field/save")
    @Operation(summary = "保存员工档案字段配置")
    @PreAuthorize("@ss.hasPermission('hrm:employee:config:update')")
    public CommonResult<Boolean> saveEmployeeArchiveFieldConfig(
            @Valid @RequestBody HrmEmployeeArchiveFieldConfigSaveReqVO reqVO) {
        employeeFieldConfigService.saveEmployeeArchiveFieldConfig(reqVO);
        return success(true);
    }

}
