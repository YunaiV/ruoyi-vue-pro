package cn.iocoder.yudao.module.statistics.controller.admin.trade;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.*;
import cn.iocoder.yudao.module.statistics.convert.trade.TradeStatisticsConvert;
import cn.iocoder.yudao.module.statistics.service.trade.TradeStatisticsService;
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

    @GetMapping("/summary")
    @Operation(summary = "获得交易统计")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<TradeStatisticsComparisonRespVO<TradeSummaryRespVO>> getTradeSummaryComparison() {
        // TODO @疯狂：这个要不要 tradeStatisticsService 调用里面的多个方法，组合出最终的 TradeSummaryRespVO；
        return success(tradeStatisticsService.getTradeSummaryComparison());
    }

    // TODO @疯狂：【晚点再改和讨论；等首页的接口出来】这个要不还是叫 analyse，对比选中的时间段，和上一个时间段；类似 MemberStatisticsController 的 getMemberAnalyse
    @GetMapping("/trend/summary")
    @Operation(summary = "获得交易状况统计")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<TradeStatisticsComparisonRespVO<TradeTrendSummaryRespVO>> getTradeTrendSummaryComparison(
            TradeTrendReqVO reqVO) {
        return success(tradeStatisticsService.getTradeTrendSummaryComparison(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1)));
    }

    // TODO 芋艿：已经 review
    // TODO @疯狂：这个要不直接叫 list；它即使就是把每条统计拿出来
    @GetMapping("/trend/list")
    @Operation(summary = "获得交易状况明细")
    @PreAuthorize("@ss.hasPermission('statistics:trade:query')")
    public CommonResult<List<TradeTrendSummaryRespVO>> getTradeStatisticsList(
            TradeTrendReqVO reqVO) {
        return success(tradeStatisticsService.getTradeStatisticsList(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1)));
    }

    // TODO @疯狂：这个要不直接叫 export；它即使就是把每条统计导出
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

}
