package cn.iocoder.yudao.module.statistics.controller.admin.member;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.statistics.convert.member.MemberStatisticsConvert;
import cn.iocoder.yudao.module.statistics.service.infra.ApiAccessLogStatisticsService;
import cn.iocoder.yudao.module.statistics.service.member.MemberStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.TradeOrderStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员统计")
@RestController
@RequestMapping("/statistics/member")
@Validated
@Slf4j
public class MemberStatisticsController {

    @Resource
    private MemberStatisticsService memberStatisticsService;
    @Resource
    private TradeOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private ApiAccessLogStatisticsService apiAccessLogStatisticsService;

    // TODO 芋艿：已经 review
    @GetMapping("/summary")
    @Operation(summary = "获得会员统计（实时统计）")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<MemberSummaryRespVO> getMemberSummary() {
        return success(memberStatisticsService.getMemberSummary());
    }

    // TODO 芋艿：已经 review
    @GetMapping("/analyse")
    @Operation(summary = "获得会员分析数据")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<MemberAnalyseRespVO> getMemberAnalyse(MemberAnalyseReqVO reqVO) {
        // 1. 查询数据
        LocalDateTime beginTime = ArrayUtil.get(reqVO.getTimes(), 0);
        LocalDateTime endTime = ArrayUtil.get(reqVO.getTimes(), 1);
        // 1.1 查询分析对照数据
        DataComparisonRespVO<MemberAnalyseDataRespVO> comparisonData = memberStatisticsService.getMemberAnalyseComparisonData(beginTime, endTime);
        // TODO @疯狂：这个可能有点特殊，要按照 create_time 来查询；不然它的漏斗就不统一；因为是访问数量 > 今日下单人 > 今日支付人；是一个统一的维度；
        // 1.2 查询成交用户数量
        Integer payUserCount = tradeOrderStatisticsService.getPayUserCount(beginTime, endTime);
        // 1.3 计算客单价
        int atv = 0;
        if (payUserCount != null && payUserCount > 0) {
            // TODO @疯狂：类似上面的 payUserCount
            Integer payPrice = tradeOrderStatisticsService.getOrderPayPrice(beginTime, endTime);
            atv = NumberUtil.div(payPrice, payUserCount).intValue();
        }
        // 1.4 查询访客数量
        Integer visitUserCount = apiAccessLogStatisticsService.getIpCount(UserTypeEnum.MEMBER.getValue(), beginTime, endTime);
        // 1.5 下单用户数量
        Integer orderUserCount = tradeOrderStatisticsService.getOrderUserCount(beginTime, endTime);

        // 2. 拼接返回
        return success(MemberStatisticsConvert.INSTANCE.convert(visitUserCount, orderUserCount, payUserCount, atv, comparisonData));
    }

    // TODO 芋艿：已经 review
    @GetMapping("/area-statistics-list")
    @Operation(summary = "按照省份，获得会员统计列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberAreaStatisticsRespVO>> getMemberAreaStatisticsList() {
        return success(memberStatisticsService.getMemberAreaStatisticsList());
    }

    // TODO 芋艿：已经 review
    @GetMapping("/sex-statistics-list")
    @Operation(summary = "按照性别，获得会员统计列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberSexStatisticsRespVO>> getMemberSexStatisticsList() {
        return success(memberStatisticsService.getMemberSexStatisticsList());
    }

    // TODO 芋艿：已经 review
    @GetMapping("/terminal-statistics-list")
    @Operation(summary = "按照终端，获得会员统计列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberTerminalStatisticsRespVO>> getMemberTerminalStatisticsList() {
        return success(memberStatisticsService.getMemberTerminalStatisticsList());
    }

    // TODO 芋艿：已经 review
    // TODO @疯狂：要注意 date 的排序；
    @GetMapping("/user-count-comparison")
    @Operation(summary = "获得用户数量对照")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<DataComparisonRespVO<MemberCountRespVO>> getUserCountComparison() {
        return success(memberStatisticsService.getUserCountComparison());
    }

    // TODO 芋艿：已经 review
    @GetMapping("/register-count-list")
    @Operation(summary = "获得会员注册数量列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberRegisterCountRespVO>> getMemberRegisterCountList(MemberAnalyseReqVO reqVO) {
        return success(memberStatisticsService.getMemberRegisterCountList(
                ArrayUtil.get(reqVO.getTimes(), 0), ArrayUtil.get(reqVO.getTimes(), 1)));
    }

}
