package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sale.ErpSaleSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sale.ErpSaleTimeSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpSaleStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.date.DatePattern.NORM_MONTH_PATTERN;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 销售统计")
@RestController
@RequestMapping("/erp/sale-statistics")
@Validated
public class ErpSaleStatisticsController {

    @Resource
    private ErpSaleStatisticsService saleStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获得销售统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpSaleSummaryRespVO> getSaleSummary() {
        LocalDateTime today = LocalDateTimeUtils.getToday();
        LocalDateTime yesterday = LocalDateTimeUtils.getYesterday();
        LocalDateTime month = LocalDateTimeUtils.getMonth();
        LocalDateTime year = LocalDateTimeUtils.getYear();
        ErpSaleSummaryRespVO summary = new ErpSaleSummaryRespVO()
                .setTodayPrice(saleStatisticsService.getSalePrice(today, null))
                .setYesterdayPrice(saleStatisticsService.getSalePrice(yesterday, today))
                .setMonthPrice(saleStatisticsService.getSalePrice(month, null))
                .setYearPrice(saleStatisticsService.getSalePrice(year, null));
        return success(summary);
    }

    @GetMapping("/time-summary")
    @Operation(summary = "获得销售时间段统计")
    @Parameter(name = "count", description = "时间段数量", example = "6")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<List<ErpSaleTimeSummaryRespVO>> getSaleTimeSummary(
            @RequestParam(value = "count", defaultValue = "6") Integer count) {
        List<ErpSaleTimeSummaryRespVO> summaryList = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            LocalDateTime startTime = LocalDateTimeUtils.beginOfMonth(LocalDateTime.now().minusMonths(i));
            LocalDateTime endTime = LocalDateTimeUtils.endOfMonth(startTime);
            summaryList.add(new ErpSaleTimeSummaryRespVO()
                    .setTime(LocalDateTimeUtil.format(startTime, NORM_MONTH_PATTERN))
                    .setPrice(saleStatisticsService.getSalePrice(startTime, endTime)));
        }
        return success(summaryList);
    }

}
