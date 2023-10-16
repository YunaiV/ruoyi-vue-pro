package cn.iocoder.yudao.module.statistics.controller.admin.member;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.statistics.service.member.MemberStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 会员统计")
@RestController
@RequestMapping("/statistics/member")
@Validated
@Slf4j
public class MemberStatisticsController {

    @Resource
    private MemberStatisticsService memberStatisticsService;

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
        return success(memberStatisticsService.getMemberAnalyse(
                ArrayUtil.get(reqVO.getTimes(), 0), ArrayUtil.get(reqVO.getTimes(), 1)));
    }

    // TODO 芋艿：已经 review
    @GetMapping("/get-area-statistics-list")
    @Operation(summary = "按照省份，获得会员统计列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberAreaStatisticsRespVO>> getMemberAreaStatisticsList() {
        return success(memberStatisticsService.getMemberAreaStatisticsList());
    }

    // TODO 芋艿：已经 review
    @GetMapping("/get-sex-statistics-list")
    @Operation(summary = "按照性别，获得会员统计列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberSexStatisticsRespVO>> getMemberSexStatisticsList() {
        return success(memberStatisticsService.getMemberSexStatisticsList());
    }

    @GetMapping("/get-terminal-statistics-list")
    @Operation(summary = "按照终端，获得会员统计列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberTerminalStatisticsRespVO>> getMemberTerminalStatisticsList() {
        // TODO 疯狂：这个可以晚点写，因为 user = = 上还没记录 terminal
        // TODO @疯狂：在 member_user 上，增加 registerTerminal 字段，基于它来统计哈；
        List<MemberTerminalStatisticsRespVO> list = convertList(TerminalEnum.values(),
                t -> new MemberTerminalStatisticsRespVO()
                        .setTerminal(t.getTerminal()).setUserCount(t.getTerminal() * 100));
        return success(list);
    }

    @GetMapping("/user-count-comparison")
    @Operation(summary = "获得用户数量对照")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<DataComparisonRespVO<MemberCountRespVO>> getUserCountComparison() {
        return success(memberStatisticsService.getUserCountComparison());
    }

    @GetMapping("/register-count-list")
    @Operation(summary = "获得会员注册数量列表")
    @PreAuthorize("@ss.hasPermission('statistics:member:query')")
    public CommonResult<List<MemberRegisterCountRespVO>> getMemberRegisterCountList(MemberAnalyseReqVO reqVO) {
        return success(memberStatisticsService.getMemberRegisterCountList(
                ArrayUtil.get(reqVO.getTimes(), 0), ArrayUtil.get(reqVO.getTimes(), 1)));
    }

}
