package cn.iocoder.yudao.module.crm.controller.admin.contract;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.config.CrmContractConfigRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.config.CrmContractConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractConfigDO;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - CRM 合同配置")
@RestController
@RequestMapping("/crm/contract-config")
@Validated
public class CrmContractConfigController {

    @Resource
    private CrmContractConfigService contractConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取合同配置")
    @PreAuthorize("@ss.hasPermission('crm:contract-config:query')")
    public CommonResult<CrmContractConfigRespVO> getCustomerPoolConfig() {
        CrmContractConfigDO config = contractConfigService.getContractConfig();
        return success(BeanUtils.toBean(config, CrmContractConfigRespVO.class));
    }

    @PutMapping("/save")
    @Operation(summary = "更新合同配置")
    @PreAuthorize("@ss.hasPermission('crm:contract-config:update')")
    public CommonResult<Boolean> saveCustomerPoolConfig(@Valid @RequestBody CrmContractConfigSaveReqVO updateReqVO) {
        contractConfigService.saveContractConfig(updateReqVO);
        return success(true);
    }

}
