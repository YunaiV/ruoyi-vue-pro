package cn.iocoder.yudao.module.statistics.convert.trade;

import cn.iocoder.yudao.module.pay.api.wallet.dto.WalletSummaryRespDTO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryExcelVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import cn.iocoder.yudao.module.trade.api.aftersale.dto.AfterSaleSummaryRespDTO;
import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderSummaryRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Convert
 *
 * @author owen
 */
@Mapper
public interface TradeStatisticsConvert {

    TradeStatisticsConvert INSTANCE = Mappers.getMapper(TradeStatisticsConvert.class);

    default TradeStatisticsComparisonRespVO<TradeSummaryRespVO> convert(TradeSummaryRespBO yesterdayData,
                                                                        TradeSummaryRespBO beforeYesterdayData,
                                                                        TradeSummaryRespBO monthData,
                                                                        TradeSummaryRespBO lastMonthData) {
        return convert(convert(yesterdayData, monthData), convert(beforeYesterdayData, lastMonthData));
    }


    default TradeSummaryRespVO convert(TradeSummaryRespBO yesterdayData, TradeSummaryRespBO monthData) {
        return new TradeSummaryRespVO()
                .setYesterdayOrderCount(yesterdayData.getCount()).setYesterdayPayPrice(yesterdayData.getSummary())
                .setMonthOrderCount(monthData.getCount()).setMonthPayPrice(monthData.getSummary());
    }

    TradeStatisticsComparisonRespVO<TradeSummaryRespVO> convert(TradeSummaryRespVO value, TradeSummaryRespVO reference);

    TradeStatisticsComparisonRespVO<TradeTrendSummaryRespVO> convert(TradeTrendSummaryRespVO value,
                                                                     TradeTrendSummaryRespVO reference);

    List<TradeTrendSummaryExcelVO> convertList02(List<TradeTrendSummaryRespVO> list);

    TradeStatisticsDO convert(LocalDateTime time, TradeOrderSummaryRespDTO orderSummary,
                              AfterSaleSummaryRespDTO afterSaleSummary, Integer brokerageSettlementPrice,
                              WalletSummaryRespDTO walletSummary);

}
