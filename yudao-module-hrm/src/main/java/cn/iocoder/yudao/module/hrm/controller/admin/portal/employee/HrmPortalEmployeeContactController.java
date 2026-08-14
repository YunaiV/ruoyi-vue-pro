package cn.iocoder.yudao.module.hrm.controller.admin.portal.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contact.HrmEmployeeContactRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeContactDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeContactService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
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

@Tag(name = "管理后台 - HRM 员工端联系人")
@RestController
@RequestMapping("/hrm/portal/employee/contact")
@Validated
public class HrmPortalEmployeeContactController {

    @Resource
    private HrmEmployeeContactService contactService;
    @Resource
    private HrmEmployeeService employeeService;

    @GetMapping("/list")
    @Operation(summary = "获得我的联系人列表")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmEmployeeContactRespVO>> getContactList() {
        Long employeeId = employeeService.validateEmployeeBySelf(getLoginUserId()).getId();
        List<HrmEmployeeContactDO> contacts = contactService.getContactListByEmployeeId(employeeId);
        return success(BeanUtils.toBean(contacts, HrmEmployeeContactRespVO.class));
    }

}
