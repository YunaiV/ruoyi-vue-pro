package cn.iocoder.yudao.module.statistics.dal.mysql.trade;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface TradeStatisticsMapper extends BaseMapperX<TradeStatisticsDO> {

    // TODO @疯狂：这个要不要也挪到 xml 里，保持统一？
    @Select("SELECT IFNULL(SUM(order_create_count), 0) AS count, IFNULL(SUM(order_pay_price), 0) AS summary " +
            "FROM trade_statistics " +
            "WHERE time BETWEEN #{beginTime} AND #{endTime} AND deleted = FALSE")
    TradeSummaryRespBO selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                                 @Param("endTime") LocalDateTime endTime);

    TradeTrendSummaryRespVO selectByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                @Param("endTime") LocalDateTime endTime);

    List<TradeTrendSummaryRespVO> selectListByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                          @Param("endTime") LocalDateTime endTime);

    Integer selectExpensePriceByTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                            @Param("endTime") LocalDateTime endTime);

}
