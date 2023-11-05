package cn.iocoder.yudao.module.statistics.dal.mysql.trade;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeOrderSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeOrderTrendRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.service.member.bo.MemberAreaStatisticsRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface TradeOrderStatisticsMapper extends BaseMapperX<TradeStatisticsDO> {

    // TODO 芋艿：已经 review
    List<MemberAreaStatisticsRespBO> selectSummaryListByAreaId();

    // TODO 芋艿：已经 review
    Integer selectCountByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                           @Param("endTime") LocalDateTime endTime);

    // TODO 芋艿：已经 review
    Integer selectCountByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                        @Param("endTime") LocalDateTime endTime);

    // TODO 芋艿：已经 review
    Integer selectSummaryPriceByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime);

    // TODO 芋艿：已经 review
    Integer selectUserCountByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime);

    // TODO 芋艿：已经 review
    Integer selectUserCountByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                            @Param("endTime") LocalDateTime endTime);

    // TODO 芋艿：已经 review
    /**
     * 按照支付时间统计订单（按天分组）
     *
     * @param beginTime 支付起始时间
     * @param endTime   支付截止时间
     * @return 订单统计列表
     */
    List<TradeOrderTrendRespVO> selectListByPayTimeBetweenAndGroupByDay(@Param("beginTime") LocalDateTime beginTime,
                                                                        @Param("endTime") LocalDateTime endTime);

    // TODO 芋艿：已经 review
    /**
     * 按照支付时间统计订单（按月分组）
     *
     * @param beginTime 支付起始时间
     * @param endTime   支付截止时间
     * @return 订单统计列表
     */
    List<TradeOrderTrendRespVO> selectListByPayTimeBetweenAndGroupByMonth(@Param("beginTime") LocalDateTime beginTime,
                                                                          @Param("endTime") LocalDateTime endTime);

    // TODO @芋艿：已经 review
    Long selectCountByStatusAndDeliveryType(@Param("status") Integer status, @Param("deliveryType") Integer deliveryType);

    // TODO 芋艿：已经 review
    TradeOrderSummaryRespVO selectPaySummaryByStatusAndPayTimeBetween(@Param("status") Integer status,
                                                                      @Param("beginTime") LocalDateTime beginTime,
                                                                      @Param("endTime") LocalDateTime endTime);

}
