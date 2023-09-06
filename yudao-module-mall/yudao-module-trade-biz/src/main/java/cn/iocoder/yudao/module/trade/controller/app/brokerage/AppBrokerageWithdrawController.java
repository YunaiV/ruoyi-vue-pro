package cn.iocoder.yudao.module.trade.controller.app.brokerage;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static java.util.Arrays.asList;

@Tag(name = "用户 APP - 分销提现")
@RestController
@RequestMapping("/trade/brokerage-withdraw")
@Validated
@Slf4j
public class AppBrokerageWithdrawController {

    // TODO 芋艿：临时 mock =>
    @GetMapping("/page")
    @Operation(summary = "获得分销提现分页")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageWithdrawRespVO>> getBrokerageWithdrawPage() {
        AppBrokerageWithdrawRespVO vo1 = new AppBrokerageWithdrawRespVO()
                .setId(1L).setStatus(10).setPrice(10).setStatusName("审批通过").setCreateTime(LocalDateTime.now());
        AppBrokerageWithdrawRespVO vo2 = new AppBrokerageWithdrawRespVO()
                .setId(2L).setStatus(0).setPrice(20).setStatusName("审批中").setCreateTime(LocalDateTime.now());
        return success(new PageResult<>(asList(vo1, vo2), 10L));
    }

}
