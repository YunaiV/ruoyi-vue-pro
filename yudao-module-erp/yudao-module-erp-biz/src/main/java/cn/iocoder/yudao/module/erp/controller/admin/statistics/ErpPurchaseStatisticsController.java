package cn.iocoder.yudao.module.erp.controller.admin.statistics;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.purchase.ErpPurchaseSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.purchase.ErpPurchaseTimeSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.statistics.ErpPurchaseStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.date.DatePattern.NORM_MONTH_PATTERN;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 采购统计")
@RestController
@RequestMapping("/erp/purchase-statistics")
@Validated
public class ErpPurchaseStatisticsController {

    @Resource
    private ErpPurchaseStatisticsService purchaseStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获得采购统计")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<ErpPurchaseSummaryRespVO> getPurchaseSummary() {
        LocalDateTime today = LocalDateTimeUtils.getToday();
        LocalDateTime yesterday = LocalDateTimeUtils.getYesterday();
        LocalDateTime month = LocalDateTimeUtils.getMonth();
        LocalDateTime year = LocalDateTimeUtils.getYear();
        ErpPurchaseSummaryRespVO summary = new ErpPurchaseSummaryRespVO()
                .setTodayPrice(purchaseStatisticsService.getPurchasePrice(today, null))
                .setYesterdayPrice(purchaseStatisticsService.getPurchasePrice(yesterday, today))
                .setMonthPrice(purchaseStatisticsService.getPurchasePrice(month, null))
                .setYearPrice(purchaseStatisticsService.getPurchasePrice(year, null));
        return success(summary);
    }

    @GetMapping("/time-summary")
    @Operation(summary = "获得采购时间段统计")
    @Parameter(name = "count", description = "时间段数量", example = "6")
    @PreAuthorize("@ss.hasPermission('erp:statistics:query')")
    public CommonResult<List<ErpPurchaseTimeSummaryRespVO>> getPurchaseTimeSummary(
            @RequestParam(value = "count", defaultValue = "6") Integer count) {
        List<ErpPurchaseTimeSummaryRespVO> summaryList = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            LocalDateTime startTime = LocalDateTimeUtils.beginOfMonth(LocalDateTime.now().minusMonths(i));
            LocalDateTime endTime = LocalDateTimeUtils.endOfMonth(startTime);
            summaryList.add(new ErpPurchaseTimeSummaryRespVO()
                    .setTime(LocalDateTimeUtil.format(startTime, NORM_MONTH_PATTERN))
                    .setPrice(purchaseStatisticsService.getPurchasePrice(startTime, endTime)));
        }
        return success(summaryList);
    }

}
