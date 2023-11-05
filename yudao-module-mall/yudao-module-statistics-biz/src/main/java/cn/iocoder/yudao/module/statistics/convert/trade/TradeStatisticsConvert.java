package cn.iocoder.yudao.module.statistics.convert.trade;

import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeOrderCountRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryExcelVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.AfterSaleSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.WalletSummaryRespBO;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
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

    default DataComparisonRespVO<TradeSummaryRespVO> convert(TradeSummaryRespBO yesterdayData,
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

    DataComparisonRespVO<TradeSummaryRespVO> convert(TradeSummaryRespVO value, TradeSummaryRespVO reference);

    DataComparisonRespVO<TradeTrendSummaryRespVO> convert(TradeTrendSummaryRespVO value,
                                                          TradeTrendSummaryRespVO reference);

    List<TradeTrendSummaryExcelVO> convertList02(List<TradeTrendSummaryRespVO> list);

    TradeStatisticsDO convert(LocalDateTime time, TradeOrderSummaryRespBO orderSummary,
                              AfterSaleSummaryRespBO afterSaleSummary, Integer brokerageSettlementPrice,
                              WalletSummaryRespBO walletSummary);

    @IterableMapping(qualifiedByName = "convert")
    List<TradeTrendSummaryRespVO> convertList(List<TradeStatisticsDO> list);

    TradeTrendSummaryRespVO convertA(TradeStatisticsDO tradeStatistics);

    @Named("convert")
    default TradeTrendSummaryRespVO convert(TradeStatisticsDO tradeStatistics) {
        TradeTrendSummaryRespVO vo = convertA(tradeStatistics);
        return vo
                .setDate(tradeStatistics.getTime().toLocalDate())
                // 营业额 = 商品支付金额 + 充值金额
                .setTurnoverPrice(tradeStatistics.getOrderPayPrice() + tradeStatistics.getRechargePayPrice())
                // 支出金额 = 余额支付金额 + 支付佣金金额 + 商品退款金额
                .setExpensePrice(tradeStatistics.getWalletPayPrice() + tradeStatistics.getBrokerageSettlementPrice() + tradeStatistics.getAfterSaleRefundPrice());
    }

    TradeOrderCountRespVO convert(Long undelivered, Long pickUp, Long afterSaleApply, Long auditingWithdraw);

}
