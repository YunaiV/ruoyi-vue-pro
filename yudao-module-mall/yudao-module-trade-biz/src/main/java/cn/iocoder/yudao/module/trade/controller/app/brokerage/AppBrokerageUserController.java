package cn.iocoder.yudao.module.trade.controller.app.brokerage;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.*;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageRecordConvert;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageUserConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageRecordService;
import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageUserService;
import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static java.util.Arrays.asList;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/trade/brokerage-user")
@Validated
@Slf4j
public class AppBrokerageUserController {
    @Resource
    private BrokerageUserService brokerageUserService;
    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private BrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/get")
    @Operation(summary = "获得个人分销信息")
    @PreAuthenticated
    public CommonResult<AppBrokerageUserRespVO> getBrokerageUser() {
        Optional<BrokerageUserDO> user = Optional.ofNullable(brokerageUserService.getBrokerageUser(getLoginUserId()));
        AppBrokerageUserRespVO respVO = new AppBrokerageUserRespVO()
                .setBrokerageEnabled(user.map(BrokerageUserDO::getBrokerageEnabled).orElse(false))
                .setBrokeragePrice(user.map(BrokerageUserDO::getBrokeragePrice).orElse(0))
                .setFrozenPrice(user.map(BrokerageUserDO::getFrozenPrice).orElse(0));
        return success(respVO);
    }

    @PutMapping("/bind")
    @Operation(summary = "绑定推广员")
    @PreAuthenticated
    public CommonResult<Boolean> bindBrokerageUser(@Valid @RequestBody AppBrokerageUserBindReqVO reqVO) {
        return success(brokerageUserService.bindBrokerageUser(getLoginUserId(), reqVO.getBindUserId(), false));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得个人分销统计")
    @PreAuthenticated
    public CommonResult<AppBrokerageUserMySummaryRespVO> getBrokerageUserSummary() {
        Long userId = getLoginUserId();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(yesterday);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(yesterday);

        AppBrokerageUserMySummaryRespVO respVO = new AppBrokerageUserMySummaryRespVO()
                .setYesterdayPrice(brokerageRecordService.getSummaryPriceByUserId(userId, BrokerageRecordBizTypeEnum.ORDER.getType(), beginTime, endTime))
                .setWithdrawPrice(brokerageWithdrawService.getSummaryPriceByUserIdAndStatus(userId, BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus()))
                .setBrokeragePrice(0)
                .setFrozenPrice(0)
                .setFirstBrokerageUserCount(brokerageUserService.getBrokerageUserCountByBindUserId(userId, 1))
                .setSecondBrokerageUserCount(brokerageUserService.getBrokerageUserCountByBindUserId(userId, 2));
        Optional.ofNullable(brokerageUserService.getBrokerageUser(userId))
                .ifPresent(user -> respVO.setBrokeragePrice(user.getBrokeragePrice()).setFrozenPrice(user.getFrozenPrice()));
        return success(respVO);
    }

    @GetMapping("/rank-page-by-user-count")
    @Operation(summary = "获得分销用户排行分页（基于用户量）")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageUserRankByUserCountRespVO>> getBrokerageUserRankPageByUserCount(AppBrokerageUserRankPageReqVO pageReqVO) {
        // 分页查询
        PageResult<AppBrokerageUserRankByUserCountRespVO> pageResult = brokerageUserService.getBrokerageUserRankPageByUserCount(pageReqVO);
        // 拼接数据
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(convertSet(pageResult.getList(), AppBrokerageUserRankByUserCountRespVO::getId));
        return success(BrokerageUserConvert.INSTANCE.convertPage03(pageResult, userMap));
    }

    @GetMapping("/rank-page-by-price")
    @Operation(summary = "获得分销用户排行分页（基于佣金）")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageUserRankByPriceRespVO>> getBrokerageUserChildSummaryPageByPrice(AppBrokerageUserRankPageReqVO pageReqVO) {
        // 分页查询
        PageResult<AppBrokerageUserRankByPriceRespVO> pageResult = brokerageRecordService.getBrokerageUserChildSummaryPageByPrice(pageReqVO);
        // 拼接数据
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(convertSet(pageResult.getList(), AppBrokerageUserRankByPriceRespVO::getId));
        return success(BrokerageRecordConvert.INSTANCE.convertPage03(pageResult, userMap));
    }

    // TODO 芋艿：临时 mock =>
    @GetMapping("/child-summary-page")
    @Operation(summary = "获得下级分销统计分页")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageUserChildSummaryRespVO>> getBrokerageUserChildSummaryPage(
            AppBrokerageUserChildSummaryPageReqVO pageReqVO) {
        AppBrokerageUserChildSummaryRespVO vo1 = new AppBrokerageUserChildSummaryRespVO()
                .setId(1L).setNickname("芋1**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokeragePrice(10).setBrokeragePrice(20).setBrokerageOrderCount(30)
                .setBrokerageTime(LocalDateTime.now());
        AppBrokerageUserChildSummaryRespVO vo2 = new AppBrokerageUserChildSummaryRespVO()
                .setId(1L).setNickname("芋2**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokeragePrice(20).setBrokeragePrice(30).setBrokerageOrderCount(40)
                .setBrokerageTime(LocalDateTime.now());
        return success(new PageResult<>(asList(vo1, vo2), 10L));
    }

    @GetMapping("/get-rank-by-price")
    @Operation(summary = "获得分销用户排行（基于佣金）")
    @Parameter(name = "times", description = "时间段", required = true)
    public CommonResult<Integer> getRankByPrice(
            @RequestParam("times") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime[] times) {
        return success(brokerageRecordService.getUserRankByPrice(getLoginUserId(), times));
    }

}
