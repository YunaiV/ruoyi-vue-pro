package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.config.HrmSalaryConfigUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 薪资配置")
@RestController
@RequestMapping("/hrm/salary/config")
@Validated
public class HrmSalaryConfigController {

    @Resource
    private HrmSalaryConfigService salaryConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建薪资配置")
    @PreAuthorize("@ss.hasPermission('hrm:salary:config:update')")
    public CommonResult<Long> createSalaryConfig(@Valid @RequestBody HrmSalaryConfigCreateReqVO reqVO) {
        return success(salaryConfigService.createSalaryConfig(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新薪资配置")
    @PreAuthorize("@ss.hasPermission('hrm:salary:config:update')")
    public CommonResult<Boolean> updateSalaryConfig(@Valid @RequestBody HrmSalaryConfigUpdateReqVO reqVO) {
        salaryConfigService.updateSalaryConfig(reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得薪资配置")
    @PreAuthorize("@ss.hasPermission('hrm:salary:config:query')")
    public CommonResult<HrmSalaryConfigRespVO> getSalaryConfig() {
        HrmSalaryConfigDO config = salaryConfigService.getSalaryConfig();
        return success(BeanUtils.toBean(config, HrmSalaryConfigRespVO.class));
    }

}
