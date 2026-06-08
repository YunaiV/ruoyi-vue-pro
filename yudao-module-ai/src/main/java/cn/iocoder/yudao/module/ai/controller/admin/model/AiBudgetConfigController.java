package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import cn.iocoder.yudao.module.ai.service.billing.AiBudgetConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 预算配置")
@RestController
@RequestMapping("/ai/budget-config")
@Validated
public class AiBudgetConfigController {

    @Resource
    private AiBudgetConfigService budgetConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建预算配置")
    @PreAuthorize("@ss.hasPermission('ai:budget-config:create')")
    public CommonResult<Long> createBudgetConfig(@Valid @RequestBody AiBudgetConfigSaveReqVO createReqVO) {
        return success(budgetConfigService.createBudgetConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新预算配置")
    @PreAuthorize("@ss.hasPermission('ai:budget-config:update')")
    public CommonResult<Boolean> updateBudgetConfig(@Valid @RequestBody AiBudgetConfigSaveReqVO updateReqVO) {
        budgetConfigService.updateBudgetConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除预算配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:budget-config:delete')")
    public CommonResult<Boolean> deleteBudgetConfig(@RequestParam("id") Long id) {
        budgetConfigService.deleteBudgetConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得预算配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:budget-config:query')")
    public CommonResult<AiBudgetConfigRespVO> getBudgetConfig(@RequestParam("id") Long id) {
        AiBudgetConfigDO config = budgetConfigService.getBudgetConfig(id);
        return success(convertToRespVO(config));
    }

    @GetMapping("/page")
    @Operation(summary = "获得预算配置分页")
    @PreAuthorize("@ss.hasPermission('ai:budget-config:query')")
    public CommonResult<PageResult<AiBudgetConfigRespVO>> getBudgetConfigPage(@Valid AiBudgetConfigPageReqVO pageReqVO) {
        PageResult<AiBudgetConfigDO> pageResult = budgetConfigService.getBudgetConfigPage(pageReqVO);
        return success(new PageResult<>(
                pageResult.getList().stream().map(this::convertToRespVO).toList(),
                pageResult.getTotal()));
    }

    private AiBudgetConfigRespVO convertToRespVO(AiBudgetConfigDO config) {
        if (config == null) {
            return null;
        }
        AiBudgetConfigRespVO respVO = new AiBudgetConfigRespVO();
        respVO.setId(config.getId());
        respVO.setUserId(config.getUserId());
        respVO.setPeriodType(config.getPeriodType());
        respVO.setCurrency(config.getCurrency());
        respVO.setBudgetAmount(config.getBudgetAmount());
        respVO.setBudgetAmountYuan(config.getBudgetAmount() != null ? config.getBudgetAmount() / 1_000_000.0 : null);
        respVO.setAlertThresholds(config.getAlertThresholds());
        respVO.setStatus(config.getStatus());
        respVO.setCreateTime(config.getCreateTime());
        return respVO;
    }

}
