package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.*;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单的统计 Service 接口
 *
 * @author owen
 */
public interface TradeOrderStatisticsService {

    /**
     * 获取订单统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 订单统计结果
     */
    TradeOrderSummaryRespBO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime);

    // TODO 芋艿：已经 review
    /**
     * 获取地区订单统计
     *
     * @return 订单统计结果
     */
    List<MemberAreaStatisticsRespVO> getSummaryListByAreaId();

    // TODO 芋艿：已经 review
    /**
     * 获取下单用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 下单用户数量
     */
    Integer getOrderUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    // TODO 芋艿：已经 review
    /**
     * 获取支付用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 支付用户数量
     */
    Integer getPayUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    // TODO 芋艿：已经 review
    /**
     * 获取支付金额
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 支付用户金额
     */
    Integer getOrderPayPrice(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易订单数量
     *
     * @return 订单数量
     */
    TradeOrderCountRespVO getOrderCount();

    /**
     * 交易订单销售额对照
     *
     * @return 销售额对照
     */
    TradeStatisticsComparisonRespVO<TradeOrderSummaryRespVO> getOrderComparison();

    /**
     * 获得订单量趋势统计
     *
     * @param reqVO 统计参数
     * @return 订单量趋势统计
     */
    List<TradeStatisticsComparisonRespVO<TradeOrderTrendRespVO>> getOrderCountTrendComparison(TradeOrderTrendReqVO reqVO);

}
