package cn.iocoder.yudao.module.statistics.controller.admin.trade;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.*;
import cn.iocoder.yudao.module.statistics.convert.trade.TradeStatisticsConvert;
import cn.iocoder.yudao.module.statistics.service.trade.AfterSaleStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.BrokerageStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.TradeOrderStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.TradeStatisticsService;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 交易统计")
@RestController
@RequestMapping("/statistics/trade")
@Validated
@Slf4j
public class TradeStatisticsController {

    @Resource
    private TradeStatisticsService tradeStatisticsService;
    @Resource
    private TradeOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private AfterSaleStatisticsService afterSaleStatisticsService;
    @Resource
    private BrokerageStatisticsService brokerageStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获得交易统计")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<TradeStatisticsComparisonRespVO<TradeSummaryRespVO>> getTradeSummaryComparison() {
        return success(tradeStatisticsService.getTradeSummaryComparison());
    }

    @GetMapping("/trend/summary")
    @Operation(summary = "获得交易状况统计")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<TradeStatisticsComparisonRespVO<TradeTrendSummaryRespVO>> getTradeTrendSummaryComparison(
            TradeTrendReqVO reqVO) {
        return success(tradeStatisticsService.getTradeTrendSummaryComparison(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1)));
    }

    @GetMapping("/trend/list")
    @Operation(summary = "获得交易状况明细")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<List<TradeTrendSummaryRespVO>> getTradeStatisticsList(
            TradeTrendReqVO reqVO) {
        return success(tradeStatisticsService.getTradeStatisticsList(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1)));
    }

    @GetMapping("/trend/export-excel")
    @Operation(summary = "导出获得交易状况明细 Excel")
    @PreAuthorize("@ss.hasPermission('statistics:trade:export')")
    public void exportTradeStatisticsExcel(TradeTrendReqVO reqVO, HttpServletResponse response) throws IOException {
        List<TradeTrendSummaryRespVO> list = tradeStatisticsService.getTradeStatisticsList(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1));
        // 导出 Excel
        List<TradeTrendSummaryExcelVO> data = TradeStatisticsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "交易状况.xls", "数据", TradeTrendSummaryExcelVO.class, data);
    }

    @GetMapping("/order-count")
    @Operation(summary = "获得交易订单数量")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<TradeOrderCountRespVO> getOrderCount() {
        TradeOrderCountRespVO vo = tradeOrderStatisticsService.getOrderCount();
        vo.setAfterSaleApply(afterSaleStatisticsService.getCountByStatus(AfterSaleStatusEnum.APPLY))
                .setAuditingWithdraw(brokerageStatisticsService.getWithdrawCountByStatus(BrokerageWithdrawStatusEnum.AUDITING));
        return success(vo);
    }

    @GetMapping("/order-comparison")
    @Operation(summary = "获得交易订单数量")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<TradeStatisticsComparisonRespVO<TradeOrderSummaryRespVO>> getOrderComparison() {
        return success(tradeOrderStatisticsService.getOrderComparison());
    }

    @GetMapping("/order-count-trend")
    @Operation(summary = "获得订单量趋势统计")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<List<TradeStatisticsComparisonRespVO<TradeOrderTrendRespVO>>> getOrderCountTrendComparison(@Valid TradeOrderTrendReqVO reqVO) {
        return success(tradeOrderStatisticsService.getOrderCountTrendComparison(reqVO));
    }

}
