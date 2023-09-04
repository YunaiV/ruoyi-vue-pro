package cn.iocoder.yudao.module.member.controller.app.brokerage;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.brokerage.vo.user.AppBrokerageUserRankRespVO;
import cn.iocoder.yudao.module.member.controller.app.brokerage.vo.user.AppBrokerageUserSummaryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static java.util.Arrays.asList;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/member/brokerage-user")
@Validated
@Slf4j
public class AppBrokerageUserController {

    // TODO 芋艿：临时 mock =>
    @GetMapping("/get-summary")
    @Operation(summary = "获得个人分销统计")
    @PreAuthenticated
    public CommonResult<AppBrokerageUserSummaryRespVO> getBrokerageUserSummary() {
        AppBrokerageUserSummaryRespVO respVO = new AppBrokerageUserSummaryRespVO()
                .setYesterdayBrokeragePrice(1)
                .setBrokeragePrice(2)
                .setFrozenBrokeragePrice(3)
                .setWithdrawBrokeragePrice(4);
        return success(respVO);
    }

    @GetMapping("/rank-page")
    @Operation(summary = "获得分销用户排行分页")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageUserRankRespVO>> getBrokerageUserRankPage(AppBrokerageUserRankPageReqVO pageReqVO) {
        AppBrokerageUserRankRespVO vo1 = new AppBrokerageUserRankRespVO()
                .setUserId(1L).setNickname("芋1**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(10);
        AppBrokerageUserRankRespVO vo2 = new AppBrokerageUserRankRespVO()
                .setUserId(2L).setNickname("芋2**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(6);
        AppBrokerageUserRankRespVO vo3 = new AppBrokerageUserRankRespVO()
                .setUserId(3L).setNickname("芋3**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(4);
        AppBrokerageUserRankRespVO vo4 = new AppBrokerageUserRankRespVO()
                .setUserId(3L).setNickname("芋3**艿").setAvatar("http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg")
                .setBrokerageUserCount(4);
        return success(new PageResult<>(asList(vo1, vo2, vo3, vo4), 10L));
    }

}
