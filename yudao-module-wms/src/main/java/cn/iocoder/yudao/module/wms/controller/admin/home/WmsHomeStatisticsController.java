package cn.iocoder.yudao.module.wms.controller.admin.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeInventorySummaryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderSummaryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderTrendRespVO;
import cn.iocoder.yudao.module.wms.service.home.WmsHomeStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - WMS 首页统计")
@RestController
@RequestMapping("/wms/home-statistics")
@Validated
public class WmsHomeStatisticsController {

    @Resource
    private WmsHomeStatisticsService homeStatisticsService;

    @GetMapping("/order-summary")
    @Operation(summary = "获得首页单据汇总统计")
    @Parameter(name = "warehouseId", description = "仓库编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:home:query')")
    public CommonResult<List<WmsHomeOrderSummaryRespVO>> getOrderSummary(
            @RequestParam(value = "warehouseId", required = false) Long warehouseId) {
        return success(homeStatisticsService.getOrderSummary(warehouseId));
    }

    @GetMapping("/order-trend")
    @Operation(summary = "获得首页单据趋势")
    @Parameter(name = "days", description = "天数", example = "7")
    @PreAuthorize("@ss.hasPermission('wms:home:query')")
    public CommonResult<List<WmsHomeOrderTrendRespVO>> getOrderTrend(
            @RequestParam(value = "days", defaultValue = "7") @Min(1) @Max(90) Integer days,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId) {
        return success(homeStatisticsService.getOrderTrend(days, warehouseId));
    }

    @GetMapping("/inventory-summary")
    @Operation(summary = "获得首页库存汇总统计")
    @Parameter(name = "warehouseId", description = "仓库编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:home:query')")
    public CommonResult<WmsHomeInventorySummaryRespVO> getInventorySummary(
            @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "goodsLimit", defaultValue = "5") @Min(1) @Max(20) Integer goodsLimit,
            @RequestParam(value = "warehouseLimit", defaultValue = "8") @Min(1) @Max(20) Integer warehouseLimit) {
        return success(homeStatisticsService.getInventorySummary(warehouseId, goodsLimit, warehouseLimit));
    }

}
