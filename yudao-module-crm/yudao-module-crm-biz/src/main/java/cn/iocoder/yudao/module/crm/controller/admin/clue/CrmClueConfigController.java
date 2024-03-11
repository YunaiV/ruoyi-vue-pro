package cn.iocoder.yudao.module.crm.controller.admin.clue;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.clueconfig.CrmClueConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueConfigDO;
import cn.iocoder.yudao.module.crm.service.clue.CrmClueConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - CRM 线索配置")
@RestController
@RequestMapping("/crm/clue-config")
@Validated
public class CrmClueConfigController {

    @Resource
    private CrmClueConfigService clueConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取线索规则设置")
    @PreAuthorize("@ss.hasPermission('crm:clue-config:query')")
    public CommonResult<CrmClueConfigDO> getCustomerPoolConfig() {
        CrmClueConfigDO clueConfig = clueConfigService.getCrmClueConfig();
        return success(BeanUtils.toBean(clueConfig, CrmClueConfigDO.class));
    }

    @PutMapping("/save")
    @Operation(summary = "更新线索规则设置")
    @PreAuthorize("@ss.hasPermission('crm:clue-config:update')")
    public CommonResult<Boolean> saveCustomerPoolConfig(@Valid @RequestBody CrmClueConfigSaveReqVO updateReqVO) {
        clueConfigService.saveClueConfig(updateReqVO);
        return success(true);
    }

}
