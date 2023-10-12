package cn.iocoder.yudao.module.statistics.service.member;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.convert.member.MemberStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.mysql.member.MemberStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.infra.ApiAccessLogStatisticsService;
import cn.iocoder.yudao.module.statistics.service.pay.PayWalletStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.TradeOrderStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.TradeStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 会员信息的统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class MemberStatisticsServiceImpl implements MemberStatisticsService {

    @Resource
    private MemberStatisticsMapper memberStatisticsMapper;

    @Resource
    private PayWalletStatisticsService payWalletStatisticsService;
    @Resource
    private TradeStatisticsService tradeStatisticsService;
    @Resource
    private TradeOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private ApiAccessLogStatisticsService apiAccessLogStatisticsService;

    @Override
    public MemberSummaryRespVO getMemberSummary() {
        MemberSummaryRespVO vo = payWalletStatisticsService.getUserRechargeSummary(null, null);
        Integer expensePrice = tradeStatisticsService.getExpensePrice(null, null);
        Integer userCount = memberStatisticsMapper.selectUserCount(null, null);
        // 拼接数据
        if (vo == null) {
            vo = new MemberSummaryRespVO().setRechargeUserCount(0).setRechargePrice(0);
        }
        return vo.setUserCount(userCount).setExpensePrice(expensePrice);
    }

    @Override
    public List<MemberAreaStatisticsRespVO> getMemberAreaStatisticsList() {
        // 统计用户
        Map<Integer, Integer> userCountMap = convertMap(memberStatisticsMapper.selectSummaryListByAreaId(),
                vo -> AreaUtils.getParentIdByType(vo.getAreaId(), AreaTypeEnum.PROVINCE),
                MemberAreaStatisticsRespVO::getUserCount, Integer::sum);
        // 统计订单
        Map<Integer, MemberAreaStatisticsRespVO> orderMap = convertMap(tradeOrderStatisticsService.getSummaryListByAreaId(),
                vo -> AreaUtils.getParentIdByType(vo.getAreaId(), AreaTypeEnum.PROVINCE),
                vo -> vo,
                (a, b) -> new MemberAreaStatisticsRespVO()
                        .setOrderCreateCount(a.getOrderCreateCount() + b.getOrderCreateCount())
                        .setOrderPayCount(a.getOrderPayCount() + b.getOrderPayCount())
                        .setOrderPayPrice(a.getOrderPayPrice() + b.getOrderPayPrice()));
        // 拼接数据
        return MemberStatisticsConvert.INSTANCE.convertList(AreaUtils.getByType(AreaTypeEnum.PROVINCE, area -> area), userCountMap, orderMap);
    }

    @Override
    public List<MemberSexStatisticsRespVO> getMemberSexStatisticsList() {
        return memberStatisticsMapper.selectSummaryListBySex();
    }

    @Override
    public MemberAnalyseRespVO getMemberAnalyse(LocalDateTime beginTime, LocalDateTime endTime) {
        // 对照数据
        MemberAnalyseComparisonRespVO vo = getMemberAnalyseComparisonData(beginTime, endTime);
        LocalDateTime referenceBeginTime = beginTime.minus(Duration.between(beginTime, endTime));
        MemberAnalyseComparisonRespVO reference = getMemberAnalyseComparisonData(referenceBeginTime, beginTime);

        Integer payUserCount = tradeOrderStatisticsService.getPayUserCount(beginTime, endTime);
        // 计算客单价
        int atv = 0;
        if (payUserCount != null && payUserCount > 0) {
            Integer payPrice = tradeOrderStatisticsService.getOrderPayPrice(beginTime, endTime);
            atv = NumberUtil.div(payPrice, payUserCount).intValue();
        }
        return new MemberAnalyseRespVO()
                .setVisitorCount(apiAccessLogStatisticsService.getVisitorUserCount(beginTime, endTime))
                .setOrderUserCount(tradeOrderStatisticsService.getOrderUserCount(beginTime, endTime))
                .setPayUserCount(payUserCount)
                .setAtv(atv)
                .setComparison(new TradeStatisticsComparisonRespVO<>(vo, reference));
    }

    private MemberAnalyseComparisonRespVO getMemberAnalyseComparisonData(LocalDateTime beginTime, LocalDateTime endTime) {
        Integer rechargeUserCount = Optional.ofNullable(payWalletStatisticsService.getUserRechargeSummary(beginTime, endTime))
                .map(MemberSummaryRespVO::getRechargeUserCount).orElse(0);
        return new MemberAnalyseComparisonRespVO()
                .setUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setActiveUserCount(apiAccessLogStatisticsService.getActiveUserCount(beginTime, endTime))
                .setRechargeUserCount(rechargeUserCount);
    }

}
