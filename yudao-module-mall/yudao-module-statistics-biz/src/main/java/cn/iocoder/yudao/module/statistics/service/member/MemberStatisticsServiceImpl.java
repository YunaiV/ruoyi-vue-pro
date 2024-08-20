package cn.iocoder.yudao.module.statistics.service.member;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.statistics.convert.member.MemberStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.mysql.member.MemberStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.infra.ApiAccessLogStatisticsService;
import cn.iocoder.yudao.module.statistics.service.member.bo.MemberAreaStatisticsRespBO;
import cn.iocoder.yudao.module.statistics.service.pay.PayWalletStatisticsService;
import cn.iocoder.yudao.module.statistics.service.pay.bo.RechargeSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.TradeOrderStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.TradeStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
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
        RechargeSummaryRespBO rechargeSummary = payWalletStatisticsService.getUserRechargeSummary(null, null);
        // TODO @疯狂：1）这里是实时统计，不好走走 TradeStatistics 表；2）因为这个放在商城下，所以只考虑订单数据，即按照 trade_order 的 pay_price 并且已支付来计算；
        Integer expensePrice = tradeStatisticsService.getExpensePrice(null, null);
        Integer userCount = memberStatisticsMapper.selectUserCount(null, null);
        return MemberStatisticsConvert.INSTANCE.convert(rechargeSummary, expensePrice, userCount);
    }

    @Override
    public List<MemberAreaStatisticsRespVO> getMemberAreaStatisticsList() {
        // 统计用户
        // TODO @疯狂：可能得把每个省的用户，都查询出来，然后去 order 那边 in；因为要按照这些人为基础来计算；；用户规模量大可能不太好，但是暂时就先这样搞吧 = =
        Map<Integer, Integer> userCountMap = convertMap(memberStatisticsMapper.selectSummaryListByAreaId(),
                vo -> AreaUtils.getParentIdByType(vo.getAreaId(), AreaTypeEnum.PROVINCE),
                MemberAreaStatisticsRespBO::getUserCount, Integer::sum);
        // 统计订单
        Map<Integer, MemberAreaStatisticsRespBO> orderMap = convertMap(tradeOrderStatisticsService.getSummaryListByAreaId(),
                bo -> AreaUtils.getParentIdByType(bo.getAreaId(), AreaTypeEnum.PROVINCE),
                bo -> bo,
                (a, b) -> new MemberAreaStatisticsRespBO()
                        .setOrderCreateUserCount(
                                (a.getOrderCreateUserCount() != null ? a.getOrderCreateUserCount() : 0) +
                                        (b.getOrderCreateUserCount() != null ? b.getOrderCreateUserCount() : 0)
                        )
                        .setOrderPayUserCount(
                                (a.getOrderPayUserCount() != null ? a.getOrderPayUserCount() : 0) +
                                        (b.getOrderPayUserCount() != null ? b.getOrderPayUserCount() : 0)
                        )
                        .setOrderPayPrice(
                                (a.getOrderPayPrice() != null ? a.getOrderPayPrice() : 0.0) +
                                        (b.getOrderPayPrice() != null ? b.getOrderPayPrice() : 0.0)
                        )
        // 拼接数据
        List<Area> areaList = AreaUtils.getByType(AreaTypeEnum.PROVINCE, area -> area);
        areaList.add(new Area().setId(null).setName("未知"));
        return MemberStatisticsConvert.INSTANCE.convertList(areaList, userCountMap, orderMap);
    }

    @Override
    public DataComparisonRespVO<MemberAnalyseDataRespVO> getMemberAnalyseComparisonData(LocalDateTime beginTime, LocalDateTime endTime) {
        // 当前数据
        MemberAnalyseDataRespVO vo = getMemberAnalyseData(beginTime, endTime);
        // 对照数据
        LocalDateTime referenceEndDate = beginTime.minusDays(1); // 减少1天，防止出现时间重叠
        LocalDateTime referenceBeginDate = referenceEndDate.minus(Duration.between(beginTime, endTime));
        MemberAnalyseDataRespVO reference = getMemberAnalyseData(
                LocalDateTimeUtil.beginOfDay(referenceBeginDate), LocalDateTimeUtil.endOfDay(referenceEndDate));
        return new DataComparisonRespVO<>(vo, reference);
    }

    private MemberAnalyseDataRespVO getMemberAnalyseData(LocalDateTime beginTime, LocalDateTime endTime) {
        Integer rechargeUserCount = Optional.ofNullable(payWalletStatisticsService.getUserRechargeSummary(beginTime, endTime))
                .map(RechargeSummaryRespBO::getRechargeUserCount).orElse(0);
        return new MemberAnalyseDataRespVO()
                .setRegisterUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setVisitUserCount(apiAccessLogStatisticsService.getUserCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime))
                .setRechargeUserCount(rechargeUserCount);
    }

    @Override
    public List<MemberSexStatisticsRespVO> getMemberSexStatisticsList() {
        return memberStatisticsMapper.selectSummaryListBySex();
    }

    @Override
    public List<MemberTerminalStatisticsRespVO> getMemberTerminalStatisticsList() {
        return memberStatisticsMapper.selectSummaryListByRegisterTerminal();
    }

    @Override
    public List<MemberRegisterCountRespVO> getMemberRegisterCountList(LocalDateTime beginTime, LocalDateTime endTime) {
        return memberStatisticsMapper.selectListByCreateTimeBetween(beginTime, endTime);
    }

    @Override
    public DataComparisonRespVO<MemberCountRespVO> getUserCountComparison() {
        // 今日时间范围
        LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(beginOfToday);
        // 昨日时间范围
        LocalDateTime beginOfYesterday = LocalDateTimeUtil.beginOfDay(beginOfToday.minusDays(1));
        LocalDateTime endOfYesterday = LocalDateTimeUtil.endOfDay(beginOfYesterday);
        return new DataComparisonRespVO<MemberCountRespVO>()
                .setValue(getUserCount(beginOfToday, endOfToday))
                .setReference(getUserCount(beginOfYesterday, endOfYesterday));
    }

    private MemberCountRespVO getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return new MemberCountRespVO()
                .setRegisterUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setVisitUserCount(apiAccessLogStatisticsService.getIpCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime));
    }

}
