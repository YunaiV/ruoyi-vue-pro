package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import cn.iocoder.yudao.module.ai.service.billing.AiModelPricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 模型计费配置")
@RestController
@RequestMapping("/ai/model-pricing")
@Validated
public class AiModelPricingController {

    @Resource
    private AiModelPricingService modelPricingService;

    @PostMapping("/create")
    @Operation(summary = "创建模型计费配置")
    @PreAuthorize("@ss.hasPermission('ai:model-pricing:create')")
    public CommonResult<Long> createModelPricing(@Valid @RequestBody AiModelPricingSaveReqVO createReqVO) {
        return success(modelPricingService.createModelPricing(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新模型计费配置")
    @PreAuthorize("@ss.hasPermission('ai:model-pricing:update')")
    public CommonResult<Boolean> updateModelPricing(@Valid @RequestBody AiModelPricingSaveReqVO updateReqVO) {
        modelPricingService.updateModelPricing(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型计费配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:model-pricing:delete')")
    public CommonResult<Boolean> deleteModelPricing(@RequestParam("id") Long id) {
        modelPricingService.deleteModelPricing(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型计费配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:model-pricing:query')")
    public CommonResult<AiModelPricingRespVO> getModelPricing(@RequestParam("id") Long id) {
        AiModelPricingDO pricing = modelPricingService.getModelPricing(id);
        return success(convertToRespVO(pricing));
    }

    @GetMapping("/page")
    @Operation(summary = "获得模型计费配置分页")
    @PreAuthorize("@ss.hasPermission('ai:model-pricing:query')")
    public CommonResult<PageResult<AiModelPricingRespVO>> getModelPricingPage(@Valid AiModelPricingPageReqVO pageReqVO) {
        PageResult<AiModelPricingDO> pageResult = modelPricingService.getModelPricingPage(pageReqVO);
        return success(convertToPageRespVO(pageResult));
    }

    // ========== 转换方法 ==========

    private AiModelPricingRespVO convertToRespVO(AiModelPricingDO pricing) {
        if (pricing == null) {
            return null;
        }
        AiModelPricingRespVO respVO = new AiModelPricingRespVO();
        respVO.setId(pricing.getId());
        respVO.setModelId(pricing.getModelId());
        respVO.setCurrency(pricing.getCurrency());
        respVO.setPriceInPer1m(pricing.getPriceInPer1m());
        respVO.setPriceCachedPer1m(pricing.getPriceCachedPer1m());
        respVO.setPriceOutPer1m(pricing.getPriceOutPer1m());
        respVO.setPriceReasoningPer1m(pricing.getPriceReasoningPer1m());
        respVO.setStrategyType(pricing.getStrategyType());
        respVO.setStrategyConfig(pricing.getStrategyConfig());
        respVO.setStatus(pricing.getStatus());
        respVO.setCreateTime(pricing.getCreateTime());
        // 微元转元，方便前端展示
        respVO.setPriceInPer1mYuan(microToYuan(pricing.getPriceInPer1m()));
        respVO.setPriceCachedPer1mYuan(microToYuan(pricing.getPriceCachedPer1m()));
        respVO.setPriceOutPer1mYuan(microToYuan(pricing.getPriceOutPer1m()));
        respVO.setPriceReasoningPer1mYuan(microToYuan(pricing.getPriceReasoningPer1m()));
        return respVO;
    }

    private PageResult<AiModelPricingRespVO> convertToPageRespVO(PageResult<AiModelPricingDO> pageResult) {
        return new PageResult<>(
                pageResult.getList().stream().map(this::convertToRespVO).toList(),
                pageResult.getTotal()
        );
    }

    /**
     * 微元转元
     */
    private Double microToYuan(Long micro) {
        if (micro == null || micro == 0) {
            return 0.0;
        }
        return micro / 1_000_000.0;
    }

}
