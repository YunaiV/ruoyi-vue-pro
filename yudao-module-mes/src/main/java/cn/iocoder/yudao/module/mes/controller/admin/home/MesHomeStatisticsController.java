package cn.iocoder.yudao.module.mes.controller.admin.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mes.controller.admin.home.vo.MesHomeProductionTrendRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.home.vo.MesHomeSummaryRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.home.vo.MesHomeWorkOrderStatusRespVO;
import cn.iocoder.yudao.module.mes.service.home.MesHomeStatisticsService;
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

@Tag(name = "管理后台 - MES 首页统计")
@RestController
@RequestMapping("/mes/home-statistics")
@Validated
public class MesHomeStatisticsController {

    @Resource
    private MesHomeStatisticsService homeStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获得首页汇总统计")
    @PreAuthorize("@ss.hasPermission('mes:home:query')")
    public CommonResult<MesHomeSummaryRespVO> getHomeSummary() {
        return success(homeStatisticsService.getHomeSummary());
    }

    @GetMapping("/work-order-status")
    @Operation(summary = "获得工单状态分布")
    @PreAuthorize("@ss.hasPermission('mes:home:query')")
    public CommonResult<List<MesHomeWorkOrderStatusRespVO>> getWorkOrderStatusDistribution() {
        return success(homeStatisticsService.getWorkOrderStatusDistribution());
    }

    @GetMapping("/production-trend")
    @Operation(summary = "获得生产趋势")
    @Parameter(name = "days", description = "天数", example = "7")
    @PreAuthorize("@ss.hasPermission('mes:home:query')")
    public CommonResult<List<MesHomeProductionTrendRespVO>> getProductionTrend(
            @RequestParam(value = "days", defaultValue = "7") @Min(1) @Max(90) Integer days) {
        return success(homeStatisticsService.getProductionTrend(days));
    }

}
