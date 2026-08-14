package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.taxrule.HrmSalaryTaxRuleRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.taxrule.HrmSalaryTaxRuleSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 计税规则")
@RestController
@RequestMapping("/hrm/salary/tax-rule")
@Validated
public class HrmSalaryTaxRuleController {

    @Resource
    private HrmSalaryTaxRuleService salaryTaxRuleService;
    @Resource
    private HrmSalaryGroupService salaryGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建计税规则")
    @PreAuthorize("@ss.hasPermission('hrm:salary:tax-rule:create')")
    public CommonResult<Long> createSalaryTaxRule(@Valid @RequestBody HrmSalaryTaxRuleSaveReqVO reqVO) {
        return success(salaryTaxRuleService.createSalaryTaxRule(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改计税规则")
    @PreAuthorize("@ss.hasPermission('hrm:salary:tax-rule:update')")
    public CommonResult<Boolean> updateSalaryTaxRule(@Valid @RequestBody HrmSalaryTaxRuleSaveReqVO reqVO) {
        salaryTaxRuleService.updateSalaryTaxRule(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除计税规则")
    @Parameter(name = "id", description = "计税规则编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:tax-rule:delete')")
    public CommonResult<Boolean> deleteSalaryTaxRule(@RequestParam("id") Long id) {
        salaryTaxRuleService.deleteSalaryTaxRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得计税规则")
    @Parameter(name = "id", description = "计税规则编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:tax-rule:query')")
    public CommonResult<HrmSalaryTaxRuleRespVO> getSalaryTaxRule(@RequestParam("id") Long id) {
        HrmSalaryTaxRuleDO taxRule = salaryTaxRuleService.getSalaryTaxRule(id);
        return success(BeanUtils.toBean(taxRule, HrmSalaryTaxRuleRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得计税规则列表")
    @PreAuthorize("@ss.hasPermission('hrm:salary:tax-rule:query')")
    public CommonResult<List<HrmSalaryTaxRuleRespVO>> getSalaryTaxRuleList() {
        // 1. 获得计税规则及其使用数量
        List<HrmSalaryTaxRuleDO> taxRules = salaryTaxRuleService.getSalaryTaxRuleList();
        Map<Long, Long> usedGroupCountMap = salaryGroupService.getSalaryGroupCountMapByTaxRuleIds(
                convertSet(taxRules, HrmSalaryTaxRuleDO::getId));

        // 2. 拼接响应
        return success(BeanUtils.toBean(taxRules, HrmSalaryTaxRuleRespVO.class,
                rule -> rule.setUsedGroupCount(usedGroupCountMap.getOrDefault(rule.getId(), 0L))));
    }

}
