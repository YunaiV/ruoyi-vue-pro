package cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule;

import cn.iocoder.yudao.module.erp.aop.SynExternalData;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.module.erp.aop.SynExternalDataAspect.ERP_CUSTOM_RULE;
import static cn.iocoder.yudao.module.erp.aop.SynExternalDataAspect.ERP_SUPPLIER_PRODUCT;

import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleService;

@Tag(name = "管理后台 - ERP 海关规则")
@RestController
@RequestMapping("/erp/custom-rule")
@Validated
public class ErpCustomRuleController {

    @Resource
    private ErpCustomRuleService customRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建ERP 海关规则")
    @PreAuthorize("@ss.hasPermission('erp:custom-rule:create')")
    @SynExternalData(table = ERP_CUSTOM_RULE)
    public CommonResult<Long> createCustomRule(@Valid @RequestBody ErpCustomRuleSaveReqVO createReqVO) {
        return success(customRuleService.createCustomRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP 海关规则")
    @PreAuthorize("@ss.hasPermission('erp:custom-rule:update')")
    @SynExternalData(table = ERP_CUSTOM_RULE, inClazz = ErpCustomRuleSaveReqVO.class)
    public CommonResult<Boolean> updateCustomRule(@Valid @RequestBody ErpCustomRuleSaveReqVO updateReqVO) {
        customRuleService.updateCustomRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 海关规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:custom-rule:delete')")
    public CommonResult<Boolean> deleteCustomRule(@RequestParam("id") Long id) {
        customRuleService.deleteCustomRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP 海关规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:custom-rule:query')")
    public CommonResult<ErpCustomRuleRespVO> getCustomRule(@RequestParam("id") Long id) {
        ErpCustomRuleDO customRule = customRuleService.getCustomRule(id);
        return success(BeanUtils.toBean(customRule, ErpCustomRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP 海关规则分页")
    @PreAuthorize("@ss.hasPermission('erp:custom-rule:query')")
    public CommonResult<PageResult<ErpCustomRuleRespVO>> getCustomRulePage(@Valid ErpCustomRulePageReqVO pageReqVO) {
        PageResult<ErpCustomRuleDO> pageResult = customRuleService.getCustomRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpCustomRuleRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP 海关规则 Excel")
    @PreAuthorize("@ss.hasPermission('erp:custom-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomRuleExcel(@Valid ErpCustomRulePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpCustomRuleDO> list = customRuleService.getCustomRulePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ERP 海关规则.xls", "数据", ErpCustomRuleRespVO.class,
                        BeanUtils.toBean(list, ErpCustomRuleRespVO.class));
    }

}