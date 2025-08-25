package cn.iocoder.yudao.module.coal.controller.admin.productionplan;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
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

import cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo.*;
import cn.iocoder.yudao.module.coal.dal.dataobject.productionplan.ProductionPlanDO;
import cn.iocoder.yudao.module.coal.service.productionplan.ProductionPlanService;

@Tag(name = "管理后台 - 生产计划")
@RestController
@RequestMapping("/coal/production-plan")
@Validated
public class ProductionPlanController {

    @Resource
    private ProductionPlanService productionPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建生产计划")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:create')")
    public CommonResult<Long> createProductionPlan(@Valid @RequestBody ProductionPlanSaveReqVO createReqVO) {
        return success(productionPlanService.createProductionPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产计划")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:update')")
    public CommonResult<Boolean> updateProductionPlan(@Valid @RequestBody ProductionPlanSaveReqVO updateReqVO) {
        productionPlanService.updateProductionPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('coal:production-plan:delete')")
    public CommonResult<Boolean> deleteProductionPlan(@RequestParam("id") Long id) {
        productionPlanService.deleteProductionPlan(id);
        return success(true);
    }


    @GetMapping("/get")
    @Operation(summary = "获得生产计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:query')")
    public CommonResult<ProductionPlanRespVO> getProductionPlan(@RequestParam("id") Long id) {
        ProductionPlanDO productionPlan = productionPlanService.getProductionPlan(id);
        return success(BeanUtils.toBean(productionPlan, ProductionPlanRespVO.class));
    }

    @DeleteMapping("/physical-delete-by-year")
    @Operation(summary = "物理删除年度计划（调试用）")
    @Parameter(name = "year", description = "年份", required = true, example = "2025")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:delete')") // 注意：权限保持一致
    public CommonResult<Boolean> physicalDeleteProductionPlanByYear(@RequestParam("year") Integer year) {
        productionPlanService.physicalDeleteProductionPlanByYear(year);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得生产计划列表")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:query')")
    public CommonResult<List<ProductionPlanRespVO>> getProductionPlanList(@Valid ProductionPlanListReqVO listReqVO) {
        List<ProductionPlanDO> list = productionPlanService.getProductionPlanList(listReqVO);
        return success(BeanUtils.toBean(list, ProductionPlanRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产计划 Excel")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductionPlanExcel(@Valid ProductionPlanListReqVO listReqVO,
              HttpServletResponse response) throws IOException {
        List<ProductionPlanDO> list = productionPlanService.getProductionPlanList(listReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "生产计划.xls", "数据", ProductionPlanRespVO.class,
                        BeanUtils.toBean(list, ProductionPlanRespVO.class));
    }

    @PostMapping("/generate-yearly-plan")
    @Operation(summary = "一键生成年度计划")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:create')")
    public CommonResult<Boolean> generateYearlyPlan(@Valid @RequestBody ProductionPlanSaveReqVO createReqVO) {
        productionPlanService.generateYearlyPlan(createReqVO);
        return success(true);
    }

    @DeleteMapping("/delete-by-year")
    @Operation(summary = "一键删除年度计划")
    @Parameter(name = "year", description = "年份", required = true, example = "2025")
    @PreAuthorize("@ss.hasPermission('coal:production-plan:delete')")
    public CommonResult<Boolean> deleteProductionPlanByYear(@RequestParam("year") Integer year) {
        productionPlanService.deleteProductionPlanByYear(year);
        return success(true);
    }

}