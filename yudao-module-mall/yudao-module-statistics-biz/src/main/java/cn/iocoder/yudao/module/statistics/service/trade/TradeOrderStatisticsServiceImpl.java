package cn.iocoder.yudao.module.statistics.service.trade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.*;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeOrderStatisticsMapper;
import cn.iocoder.yudao.module.statistics.enums.TimeRangeTypeEnum;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 交易订单统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class TradeOrderStatisticsServiceImpl implements TradeOrderStatisticsService {

    @Resource
    private TradeOrderStatisticsMapper tradeOrderStatisticsMapper;

    @Override
    public TradeOrderSummaryRespBO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return new TradeOrderSummaryRespBO()
                .setOrderCreateCount(tradeOrderStatisticsMapper.selectCountByCreateTimeBetween(beginTime, endTime))
                .setOrderPayCount(tradeOrderStatisticsMapper.selectCountByPayTimeBetween(beginTime, endTime))
                .setOrderPayPrice(tradeOrderStatisticsMapper.selectSummaryPriceByPayTimeBetween(beginTime, endTime));
    }

    @Override
    public List<MemberAreaStatisticsRespVO> getSummaryListByAreaId() {
        return tradeOrderStatisticsMapper.selectSummaryListByAreaId();
    }

    @Override
    public Integer getOrderUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectUserCountByCreateTimeBetween(beginTime, endTime);
    }

    @Override
    public Integer getPayUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectUserCountByPayTimeBetween(beginTime, endTime);
    }

    @Override
    public Integer getOrderPayPrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectSummaryPriceByPayTimeBetween(beginTime, endTime);
    }

    @Override
    public TradeOrderCountRespVO getOrderCount() {
        // TODO 疯狂：这个可以根据 status + delivertyType 来过滤呀；ps：是不是搞个 service 方法，交给上层去聚合，这样 TradeOrderCountRespVO 可以更明确返回，不用搞 bo；
        Long undeliveredCount = tradeOrderStatisticsMapper.selectCountByStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus());
        Long pickUpCount = tradeOrderStatisticsMapper.selectCountByStatusAndPickUpStoreIdIsNotNull(TradeOrderStatusEnum.DELIVERED.getStatus());
        return new TradeOrderCountRespVO()
                .setPickUp(ObjUtil.defaultIfNull(pickUpCount, 0L))
                .setUndelivered(ObjUtil.defaultIfNull(undeliveredCount, 0L));
    }

    @Override
    public DataComparisonRespVO<TradeOrderSummaryRespVO> getOrderComparison() {
        return new DataComparisonRespVO<TradeOrderSummaryRespVO>()
                .setValue(getPayPriceSummary(LocalDateTime.now()))
                .setReference(getPayPriceSummary(LocalDateTime.now().minusDays(1)));
    }

    private TradeOrderSummaryRespVO getPayPriceSummary(LocalDateTime date) {
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(date);
        LocalDateTime endTime = LocalDateTimeUtil.beginOfDay(date);
        return tradeOrderStatisticsMapper.selectPaySummaryByStatusAndPayTimeBetween(
                PayOrderStatusEnum.SUCCESS.getStatus(), beginTime, endTime);
    }

    @Override
    public List<DataComparisonRespVO<TradeOrderTrendRespVO>> getOrderCountTrendComparison(TradeOrderTrendReqVO reqVO) {
        // 查询当前数据
        List<TradeOrderTrendRespVO> value = getOrderCountTrend(reqVO.getType(), reqVO.getBeginTime(), reqVO.getEndTime());
        // 查询对照数据
        LocalDateTime referenceEndTime = reqVO.getBeginTime().minusDays(1);
        LocalDateTime referenceBeginTime = referenceEndTime.minus(Duration.between(reqVO.getBeginTime(), reqVO.getEndTime()));
        List<TradeOrderTrendRespVO> reference = getOrderCountTrend(reqVO.getType(), referenceBeginTime, referenceEndTime);
        // 顺序对比返回
        return IntStream.range(0, value.size())
                .mapToObj(index -> new DataComparisonRespVO<TradeOrderTrendRespVO>()
                        .setValue(CollUtil.get(value, index))
                        .setReference(CollUtil.get(reference, index)))
                .collect(Collectors.toList());
    }

    private List<TradeOrderTrendRespVO> getOrderCountTrend(Integer timeRangeType, LocalDateTime beginTime, LocalDateTime endTime) {
        // 情况一：按年统计时，以月份分组
        if (TimeRangeTypeEnum.YEAR.getType().equals(timeRangeType)) {
            return tradeOrderStatisticsMapper.selectListByPayTimeBetweenAndGroupByMonth(beginTime, endTime);
        }
        // 情况二：其它以天分组（天、周、月）
        return tradeOrderStatisticsMapper.selectListByPayTimeBetweenAndGroupByDay(beginTime, endTime);
    }

}
