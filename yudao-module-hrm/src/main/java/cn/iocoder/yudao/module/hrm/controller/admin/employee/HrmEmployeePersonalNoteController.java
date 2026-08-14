package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.personalnote.HrmEmployeePersonalNoteCreateReqVO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeePersonalNoteService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工个人备忘")
@RestController
@RequestMapping("/hrm/employee/personal-note")
@Validated
public class HrmEmployeePersonalNoteController {

    @Resource
    private HrmEmployeePersonalNoteService personalNoteService;
    @Resource
    private HrmEmployeeService employeeService;

    @PostMapping("/create")
    @Operation(summary = "创建员工个人备忘")
    @PreAuthorize("@ss.hasPermission('hrm:employee:personal-note:create')")
    public CommonResult<Long> createPersonalNote(
            @Valid @RequestBody HrmEmployeePersonalNoteCreateReqVO reqVO) {
        Long employeeId = employeeService.validateEmployeeBySelf(getLoginUserId()).getId();
        return success(personalNoteService.createPersonalNote(employeeId, reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工个人备忘")
    @Parameter(name = "id", description = "员工个人备忘编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:personal-note:delete')")
    public CommonResult<Boolean> deletePersonalNote(@RequestParam("id") Long id) {
        Long employeeId = employeeService.validateEmployeeBySelf(getLoginUserId()).getId();
        personalNoteService.deletePersonalNote(employeeId, id);
        return success(true);
    }

}
