package cn.iocoder.yudao.module.member.controller.app.brokerage;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.controller.app.brokerage.vo.user.AppBrokerageUserSummaryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

}
