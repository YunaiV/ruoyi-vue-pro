package cn.iocoder.yudao.module.statistics.dal.mysql.trade;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 交易统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface TradeOrderStatisticsMapper extends BaseMapperX<TradeStatisticsDO> {

    TradeOrderSummaryRespBO selectSummaryByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                          @Param("endTime") LocalDateTime endTime);

}
