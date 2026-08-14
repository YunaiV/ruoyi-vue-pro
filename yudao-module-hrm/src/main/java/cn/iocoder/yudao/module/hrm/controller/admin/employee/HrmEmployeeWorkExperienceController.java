package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience.HrmEmployeeWorkExperienceRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience.HrmEmployeeWorkExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeWorkExperienceDO;
import cn.iocoder.yudao.module.hrm.service.employee.experience.HrmEmployeeWorkExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

@Tag(name = "管理后台 - HRM 员工工作经历")
@RestController
@RequestMapping("/hrm/employee/work-experience")
@Validated
public class HrmEmployeeWorkExperienceController {

    @Resource
    private HrmEmployeeWorkExperienceService workExperienceService;

    @GetMapping("/list")
    @Operation(summary = "获得员工工作经历列表")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:query')")
    public CommonResult<List<HrmEmployeeWorkExperienceRespVO>> getWorkExperienceList(
            @RequestParam("employeeId") Long employeeId) {
        List<HrmEmployeeWorkExperienceDO> workExperiences =
                workExperienceService.getWorkExperienceListByEmployeeId(employeeId);
        return success(BeanUtils.toBean(workExperiences, HrmEmployeeWorkExperienceRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建员工工作经历")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Long> createWorkExperience(
            @Valid @RequestBody HrmEmployeeWorkExperienceSaveReqVO reqVO) {
        return success(workExperienceService.createWorkExperience(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新员工工作经历")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Boolean> updateWorkExperience(
            @Valid @RequestBody HrmEmployeeWorkExperienceSaveReqVO reqVO) {
        workExperienceService.updateWorkExperience(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工工作经历")
    @Parameter(name = "id", description = "工作经历编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:delete')")
    public CommonResult<Boolean> deleteWorkExperience(@RequestParam("id") Long id) {
        workExperienceService.deleteWorkExperience(id);
        return success(true);
    }

}
