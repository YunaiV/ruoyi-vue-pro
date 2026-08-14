package cn.iocoder.yudao.module.hrm.controller.admin.portal.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee.HrmPortalEmployeeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee.HrmPortalEmployeeUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeArchiveFieldEnum;
import cn.iocoder.yudao.module.hrm.service.employee.config.HrmEmployeeFieldConfigService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getYearsBetween;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端档案")
@RestController
@RequestMapping("/hrm/portal/employee")
@Validated
public class HrmPortalEmployeeController {

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmEmployeeFieldConfigService employeeFieldConfigService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/get-bind-status")
    @Operation(summary = "获得当前账号的员工绑定状态")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<Boolean> getEmployeeBindStatus() {
        return success(employeeService.getEmployeeByUserId(getLoginUserId()) != null);
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的员工档案")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPortalEmployeeRespVO> getEmployee() {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        // 拼接 VO
        HrmPortalEmployeeRespVO respVO = BeanUtils.toBean(employee, HrmPortalEmployeeRespVO.class);
        LocalDate today = LocalDate.now();
        if (employee.getBirthday() != null) {
            respVO.setAge(getYearsBetween(employee.getBirthday().toLocalDate(), today));
        }
        if (employee.getCompanyAgeStartTime() != null) {
            respVO.setCompanyAge(getYearsBetween(employee.getCompanyAgeStartTime().toLocalDate(), today));
        }
        respVO.setEntryDay(employee.getEntryTime() == null ? 0L
                : Math.max(0L, ChronoUnit.DAYS.between(employee.getEntryTime().toLocalDate(), today) + 1));
        if (employee.getUserId() != null) {
            AdminUserRespDTO user = adminUserApi.getUser(employee.getUserId());
            respVO.setAvatar(user == null ? null : user.getAvatar());
        }
        if (employee.getDeptId() != null) {
            DeptRespDTO dept = deptApi.getDept(employee.getDeptId());
            respVO.setDeptName(dept == null ? null : dept.getName());
        }
        if (employee.getLeaderEmployeeId() != null) {
            HrmEmployeeDO leader = employeeService.getEmployee(employee.getLeaderEmployeeId());
            respVO.setLeaderEmployeeName(leader == null ? null : leader.getName());
        }
        hideInvisibleArchiveFields(respVO, employeeFieldConfigService.getVisibleArchiveFieldNames());
        return success(respVO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的员工档案")
    @PreAuthorize("@ss.hasPermission('hrm:portal:employee:update')")
    public CommonResult<Boolean> updateEmployee(
            @Valid @RequestBody HrmPortalEmployeeUpdateReqVO reqVO) {
        employeeService.updateEmployeeBySelf(getLoginUserId(), reqVO);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private void hideInvisibleArchiveFields(HrmPortalEmployeeRespVO respVO, Set<String> visibleFields) {
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.NAME.getName())) {
            respVO.setName(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.COUNTRY.getName())) {
            respVO.setCountry(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.NATION.getName())) {
            respVO.setNation(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.ID_TYPE.getName())) {
            respVO.setIdType(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.ID_NUMBER.getName())) {
            respVO.setIdNumber(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.SEX.getName())) {
            respVO.setSex(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.NATIVE_PLACE.getName())) {
            respVO.setNativePlace(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.BIRTHDAY.getName())) {
            respVO.setBirthday(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.AGE.getName())) {
            respVO.setAge(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.HIGHEST_EDUCATION.getName())) {
            respVO.setHighestEducation(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.MOBILE.getName())) {
            respVO.setMobile(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.EMAIL.getName())) {
            respVO.setEmail(null);
        }
        if (!visibleFields.contains(HrmEmployeeArchiveFieldEnum.ADDRESS.getName())) {
            respVO.setAddress(null);
        }
    }

}
