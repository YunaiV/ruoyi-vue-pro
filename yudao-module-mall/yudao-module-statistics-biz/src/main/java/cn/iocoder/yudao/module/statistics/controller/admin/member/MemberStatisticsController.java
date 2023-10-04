package cn.iocoder.yudao.module.statistics.controller.admin.member;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSexStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberTerminalStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理后台 - 会员统计")
@RestController
@RequestMapping("/statistics/member")
@Validated
@Slf4j
public class MemberStatisticsController {

    // TODO @疯狂：一个类似 getTradeTrendSummaryComparison 的接口
    // TODO @疯狂：一个类似 getTradeStatisticsList 的接口

    @GetMapping("/summary")
    @Operation(summary = "获得会员统计")
    public CommonResult<TradeStatisticsComparisonRespVO<MemberSummaryRespVO>> getMemberSummary() {
        // TODO 疯狂：目前先直接计算；
        return null;
    }

    @GetMapping("/get-area-statistics-list")
    @Operation(summary = "按照省份，获得会员统计列表")
    public CommonResult<List<MemberAreaStatisticsRespVO>> getMemberAreaStatisticsList() {
        // TODO 疯狂：目前先直接计算，进行统计；后续再考虑优化
        return null;
    }

    @GetMapping("/get-sex-statistics-list")
    @Operation(summary = "按照性别，获得会员统计列表")
    public CommonResult<List<MemberSexStatisticsRespVO>> getMemberSexStatisticsList() {
        // TODO 疯狂：目前先直接计算，进行统计；后续再考虑优化
        return null;
    }

    @GetMapping("/get-terminal-statistics-list")
    @Operation(summary = "按照终端，获得会员统计列表")
    public CommonResult<List<MemberTerminalStatisticsRespVO>> getMemberTerminalStatisticsList() {
        // TODO 疯狂：目前先直接计算，进行统计；后续再考虑优化
        // TODO 疯狂：这个可以晚点写，因为 user = = 上还没记录 terminal
        return null;
    }

}
