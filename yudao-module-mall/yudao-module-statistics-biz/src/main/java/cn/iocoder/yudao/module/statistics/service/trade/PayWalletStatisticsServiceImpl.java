package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.service.trade.bo.WalletSummaryRespBO;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.PayWalletStatisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 钱包统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class PayWalletStatisticsServiceImpl implements PayWalletStatisticsService {

    @Resource
    private PayWalletStatisticsMapper payWalletStatisticsMapper;

    @Override
    public WalletSummaryRespBO getWalletSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        WalletSummaryRespBO paySummary = payWalletStatisticsMapper.selectRechargeSummaryByPayTimeBetween(
                beginTime, endTime, true);
        WalletSummaryRespBO refundSummary = payWalletStatisticsMapper.selectRechargeSummaryByRefundTimeBetween(
                beginTime, endTime, PayRefundStatusEnum.SUCCESS.getStatus());
        Integer walletPayPrice = payWalletStatisticsMapper.selectPriceSummaryByBizTypeAndCreateTimeBetween(
                beginTime, endTime, PayWalletBizTypeEnum.PAYMENT.getType());

        paySummary.setOrderWalletPayPrice(walletPayPrice);
        paySummary.setRechargeRefundCount(refundSummary.getRechargeRefundCount());
        paySummary.setRechargeRefundPrice(refundSummary.getRechargeRefundPrice());

        return paySummary;
    }

}
