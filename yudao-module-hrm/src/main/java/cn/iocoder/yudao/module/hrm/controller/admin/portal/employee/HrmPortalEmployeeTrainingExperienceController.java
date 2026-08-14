package cn.iocoder.yudao.module.hrm.controller.admin.portal.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.trainingexperience.HrmEmployeeTrainingExperienceRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeTrainingExperienceDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.employee.experience.HrmEmployeeTrainingExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端培训经历")
@RestController
@RequestMapping("/hrm/portal/employee/training-experience")
@Validated
public class HrmPortalEmployeeTrainingExperienceController {

    @Resource
    private HrmEmployeeTrainingExperienceService trainingExperienceService;
    @Resource
    private HrmEmployeeService employeeService;

    @GetMapping("/list")
    @Operation(summary = "获得我的培训经历列表")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmEmployeeTrainingExperienceRespVO>> getTrainingExperienceList() {
        Long employeeId = employeeService.validateEmployeeBySelf(getLoginUserId()).getId();
        List<HrmEmployeeTrainingExperienceDO> trainingExperiences =
                trainingExperienceService.getTrainingExperienceListByEmployeeId(employeeId);
        return success(BeanUtils.toBean(
                trainingExperiences, HrmEmployeeTrainingExperienceRespVO.class));
    }

}
