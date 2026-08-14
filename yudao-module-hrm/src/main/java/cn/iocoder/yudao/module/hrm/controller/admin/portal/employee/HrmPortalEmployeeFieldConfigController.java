package cn.iocoder.yudao.module.hrm.controller.admin.portal.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config.HrmEmployeeFieldConfigRespVO;
import cn.iocoder.yudao.module.hrm.service.employee.config.HrmEmployeeFieldConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 员工端档案字段配置")
@RestController
@RequestMapping("/hrm/portal/employee/field-config")
@Validated
public class HrmPortalEmployeeFieldConfigController {

    @Resource
    private HrmEmployeeFieldConfigService employeeFieldConfigService;

    @GetMapping("/list")
    @Operation(summary = "获得我的员工档案字段配置")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmEmployeeFieldConfigRespVO>> getEmployeeFieldConfigList() {
        return success(employeeFieldConfigService.getEmployeeArchiveFieldConfigList());
    }

}
