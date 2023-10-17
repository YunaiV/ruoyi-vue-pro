package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeOrderStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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

}
