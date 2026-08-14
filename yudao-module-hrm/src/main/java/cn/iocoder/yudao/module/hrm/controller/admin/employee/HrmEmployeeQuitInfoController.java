package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.quitinfo.HrmEmployeeQuitInfoRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeQuitInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 员工离职信息")
@RestController
@RequestMapping("/hrm/employee/quit-info")
@Validated
public class HrmEmployeeQuitInfoController {

    @Resource
    private HrmEmployeeQuitInfoService quitInfoService;

    @GetMapping("/get")
    @Operation(summary = "获得员工离职信息")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:query')")
    public CommonResult<HrmEmployeeQuitInfoRespVO> getQuitInfo(
            @RequestParam("employeeId") Long employeeId) {
        HrmEmployeeQuitInfoDO quitInfo = quitInfoService.getQuitInfoByEmployeeId(employeeId);
        return success(BeanUtils.toBean(quitInfo, HrmEmployeeQuitInfoRespVO.class));
    }

}
