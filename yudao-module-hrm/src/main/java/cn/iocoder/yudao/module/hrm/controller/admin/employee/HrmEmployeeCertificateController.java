package cn.iocoder.yudao.module.hrm.controller.admin.employee;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate.HrmEmployeeCertificateRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate.HrmEmployeeCertificateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeCertificateDO;
import cn.iocoder.yudao.module.hrm.service.employee.experience.HrmEmployeeCertificateService;
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

@Tag(name = "管理后台 - HRM 员工证书")
@RestController
@RequestMapping("/hrm/employee/certificate")
@Validated
public class HrmEmployeeCertificateController {

    @Resource
    private HrmEmployeeCertificateService certificateService;

    @GetMapping("/list")
    @Operation(summary = "获得员工证书列表")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:query')")
    public CommonResult<List<HrmEmployeeCertificateRespVO>> getCertificateList(
            @RequestParam("employeeId") Long employeeId) {
        List<HrmEmployeeCertificateDO> certificates = certificateService.getCertificateListByEmployeeId(employeeId);
        return success(BeanUtils.toBean(certificates, HrmEmployeeCertificateRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建员工证书")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Long> createCertificate(
            @Valid @RequestBody HrmEmployeeCertificateSaveReqVO reqVO) {
        return success(certificateService.createCertificate(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新员工证书")
    @PreAuthorize("@ss.hasPermission('hrm:employee:update')")
    public CommonResult<Boolean> updateCertificate(
            @Valid @RequestBody HrmEmployeeCertificateSaveReqVO reqVO) {
        certificateService.updateCertificate(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工证书")
    @Parameter(name = "id", description = "证书编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:employee:delete')")
    public CommonResult<Boolean> deleteCertificate(@RequestParam("id") Long id) {
        certificateService.deleteCertificate(id);
        return success(true);
    }

}
