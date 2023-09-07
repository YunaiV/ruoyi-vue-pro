package cn.iocoder.yudao.module.trade.controller.app.brokerage;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static java.util.Arrays.asList;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/trade/brokerage-user")
@Validated
@Slf4j
public class AppBrokerageUserController {

    // TODO 芋艿：临时 mock =>
    @GetMapping("/get")
    @Operation(summary = "获得个人分销信息")
    @PreAuthenticated
    public CommonResult<AppBrokerageUserRespVO> getBrokerageUser() {
        AppBrokerageUserRespVO respVO = new AppBrokerageUserRespVO()
                .setBrokeragePrice(2000)
                .setFrozenBrokeragePrice(3000);
        return success(respVO);
    }

    // TODO 芋艿：临时 mock =>
    @GetMapping("/get-summary")
    @Operation(summary = "获得个人分销统计")
    @PreAuthenticated
    public CommonResult<AppBrokerageUserMySummaryRespVO> getBrokerageUserSummary() {
        AppBrokerageUserMySummaryRespVO respVO = new AppBrokerageUserMySummaryRespVO()
                .setYesterdayBrokeragePrice(1)
                .setBrokeragePrice(2)
                .setFrozenBrokeragePrice(3)
                .setWithdrawBrokeragePrice(4)
                .setFirstBrokerageUserCount(166)
                .setSecondBrokerageUserCount(233);
        return success(respVO);
    }

    // TODO 芋艿：临时 mock =>
    @GetMapping("/rank-page-by-user-count")
    @Operation(summary = "获得分销用户排行分页（基于用户量）")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageUserRankByUserCountRespVO>> getBrokerageUserRankPageByUserCount(AppBrokerageUserRankPageReqVO pageReqVO) {
        AppBrokerageUserRankByUserCountRespVO vo1 = new AppBrokerageUserRankByUserCountRespVO()
                .setId(1L).setNickname("芋1**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(10);
        AppBrokerageUserRankByUserCountRespVO vo2 = new AppBrokerageUserRankByUserCountRespVO()
                .setId(2L).setNickname("芋2**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(6);
        AppBrokerageUserRankByUserCountRespVO vo3 = new AppBrokerageUserRankByUserCountRespVO()
                .setId(3L).setNickname("芋3**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(4);
        AppBrokerageUserRankByUserCountRespVO vo4 = new AppBrokerageUserRankByUserCountRespVO()
                .setId(3L).setNickname("芋3**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(4);
        return success(new PageResult<>(asList(vo1, vo2, vo3, vo4), 10L));
    }

    // TODO 芋艿：临时 mock =>
    @GetMapping("/rank-page-by-price")
    @Operation(summary = "获得分销用户排行分页（基于佣金）")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageUserRankByPriceRespVO>> getBrokerageUserChildSummaryPageByPrice(AppBrokerageUserRankPageReqVO pageReqVO) {
        AppBrokerageUserRankByPriceRespVO vo1 = new AppBrokerageUserRankByPriceRespVO()
                .setId(1L).setNickname("芋1**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokeragePrice(10);
        AppBrokerageUserRankByPriceRespVO vo2 = new AppBrokerageUserRankByPriceRespVO()
                .setId(2L).setNickname("芋2**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokeragePrice(6);
        AppBrokerageUserRankByPriceRespVO vo3 = new AppBrokerageUserRankByPriceRespVO()
                .setId(3L).setNickname("芋3**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokeragePrice(4);
        AppBrokerageUserRankByPriceRespVO vo4 = new AppBrokerageUserRankByPriceRespVO()
                .setId(3L).setNickname("芋3**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokeragePrice(4);
        return success(new PageResult<>(asList(vo1, vo2, vo3, vo4), 10L));
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

    // TODO 芋艿：临时 mock =>
    @GetMapping("/get-rank-by-price")
    @Operation(summary = "获得分销用户排行（基于佣金）")
    @Parameter(name = "times", description = "时间段", required = true)
    public CommonResult<Integer> getBrokerageUserRankByPrice(
            @RequestParam("times") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime[] times) {
        return success(1);
    }

}
