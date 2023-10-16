package cn.iocoder.yudao.module.statistics.service.member;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.convert.member.MemberStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.mysql.member.MemberStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.infra.ApiAccessLogStatisticsService;
import cn.iocoder.yudao.module.statistics.service.pay.PayWalletStatisticsService;
import cn.iocoder.yudao.module.statistics.service.pay.bo.RechargeSummaryRespBO;
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
        RechargeSummaryRespBO rechargeSummary = payWalletStatisticsService.getUserRechargeSummary(null, null);
        // TODO @疯狂：1）这里是实时统计，不好走走 TradeStatistics 表；2）因为这个放在商城下，所以只考虑订单数据，即按照 trade_order 的 pay_price 并且已支付来计算；
        Integer expensePrice = tradeStatisticsService.getExpensePrice(null, null);
        Integer userCount = memberStatisticsMapper.selectUserCount(null, null);
        return MemberStatisticsConvert.INSTANCE.convert(rechargeSummary, expensePrice, userCount);
    }

    @Override
    public List<MemberAreaStatisticsRespVO> getMemberAreaStatisticsList() {
        // 统计用户
        // TODO @疯狂：要处理下，未知省份；就是没填写省份的情况；
        // TODO @疯狂：可能得把每个省的用户，都查询出来，然后去 order 那边 in；因为要按照这些人为基础来计算；；用户规模量大可能不太好，但是暂时就先这样搞吧 = =
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

    // TODO @疯狂：这个方法，要不拆成：1）controller 调用 getMemberAnalyseComparisonData；2）tradeOrderStatisticsService.getPayUserCount；3）tradeOrderStatisticsService.getOrderPayPrice；4）。。。
    // TODO 就是说：分析交给 controller 去组合；
    @Override
    public MemberAnalyseRespVO getMemberAnalyse(LocalDateTime beginTime, LocalDateTime endTime) {
        // 对照数据
        MemberAnalyseComparisonRespVO vo = getMemberAnalyseComparisonData(beginTime, endTime);
        // TODO @疯狂：如果时间段这么处理，会不会 beginTime 重叠了。因为是 <= 一个时间；如果数据库插入的是 ，xxxx-yy-zz 00:00:00 的话，它既满足 >= ? 也满足 <= ；（如果不好理解，微信聊)
        LocalDateTime referenceBeginTime = beginTime.minus(Duration.between(beginTime, endTime));
        MemberAnalyseComparisonRespVO reference = getMemberAnalyseComparisonData(referenceBeginTime, beginTime);

        // 计算客单价
        // TODO @疯狂：这个可能有点特殊，要按照 create_time 来查询；不然它的漏斗就不统一；因为是访问数量 > 今日下单人 > 今日支付人；是一个统一的维度；
        Integer payUserCount = tradeOrderStatisticsService.getPayUserCount(beginTime, endTime);
        int atv = 0;
        if (payUserCount != null && payUserCount > 0) {
            // TODO @疯狂：类似上面的 payUserCount
            Integer payPrice = tradeOrderStatisticsService.getOrderPayPrice(beginTime, endTime);
            atv = NumberUtil.div(payPrice, payUserCount).intValue();
        }
        return new MemberAnalyseRespVO()
                .setVisitorCount(apiAccessLogStatisticsService.getVisitorUserCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime))
                .setOrderUserCount(tradeOrderStatisticsService.getOrderUserCount(beginTime, endTime))
                .setPayUserCount(payUserCount)
                .setAtv(atv)
                .setComparison(new TradeStatisticsComparisonRespVO<>(vo, reference));
    }

    private MemberAnalyseComparisonRespVO getMemberAnalyseComparisonData(LocalDateTime beginTime, LocalDateTime endTime) {
        Integer rechargeUserCount = Optional.ofNullable(payWalletStatisticsService.getUserRechargeSummary(beginTime, endTime))
                .map(RechargeSummaryRespBO::getRechargeUserCount).orElse(0);
        return new MemberAnalyseComparisonRespVO()
                .setUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setActiveUserCount(apiAccessLogStatisticsService.getActiveUserCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime))
                .setRechargeUserCount(rechargeUserCount);
    }

    @Override
    public List<MemberSexStatisticsRespVO> getMemberSexStatisticsList() {
        // TODO @疯狂：需要考虑，用户性别为空，则是“未知”
        return memberStatisticsMapper.selectSummaryListBySex();
    }

    @Override
    public List<MemberRegisterCountRespVO> getMemberRegisterCountList(LocalDateTime beginTime, LocalDateTime endTime) {
        return memberStatisticsMapper.selectListByCreateTimeBetween(beginTime, endTime);
    }

    @Override
    public TradeStatisticsComparisonRespVO<MemberCountRespVO> getUserCountComparison() {
        // 今日时间范围
        LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(beginOfToday);
        // 昨日时间范围
        LocalDateTime beginOfYesterday = LocalDateTimeUtil.beginOfDay(beginOfToday.minusDays(1));
        LocalDateTime endOfYesterday = LocalDateTimeUtil.endOfDay(beginOfYesterday);
        return new TradeStatisticsComparisonRespVO<MemberCountRespVO>()
                .setValue(getUserCount(beginOfToday, endOfToday))
                .setReference(getUserCount(beginOfYesterday, endOfYesterday));
    }

    private MemberCountRespVO getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return new MemberCountRespVO()
                .setCreateUserCount(memberStatisticsMapper.selectUserCount(beginTime, endTime))
                .setVisitUserCount(apiAccessLogStatisticsService.getVisitorUserCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime));
    }

}
