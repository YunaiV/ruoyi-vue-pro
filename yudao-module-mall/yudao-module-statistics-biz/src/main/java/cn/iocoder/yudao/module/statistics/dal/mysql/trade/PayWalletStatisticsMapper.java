package cn.iocoder.yudao.module.statistics.dal.mysql.trade;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.service.trade.bo.WalletSummaryRespBO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 交易统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface PayWalletStatisticsMapper extends BaseMapperX<TradeStatisticsDO> {

    WalletSummaryRespBO selectRechargeSummaryByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("payStatus") Boolean payStatus);

    WalletSummaryRespBO selectRechargeSummaryByRefundTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                 @Param("endTime") LocalDateTime endTime,
                                                                 @Param("refundStatus") Integer refundStatus);

    Integer selectPriceSummaryByBizTypeAndCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("bizType") Integer bizType);
}
