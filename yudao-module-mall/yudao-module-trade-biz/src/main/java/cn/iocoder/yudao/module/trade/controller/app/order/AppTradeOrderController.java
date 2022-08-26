package cn.iocoder.yudao.module.trade.controller.app.order;

import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderGetCreateInfoRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.TradeOrderRespVO;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "用户 App - 交易订单")
@RestController
@RequestMapping("/trade/order")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AppTradeOrderController {

    // TODO 在思考下；

    private final TradeOrderService tradeOrderService;


    @GetMapping("/get-create-info")
    @ApiOperation("基于商品，确认创建订单")
    @PreAuthenticated
    public CommonResult<AppTradeOrderGetCreateInfoRespVO> getTradeOrderCreateInfo(AppTradeOrderCreateReqVO createReqVO) {
//        return success(tradeOrderService.getOrderConfirmCreateInfo(UserSecurityContextHolder.getUserId(), skuId, quantity, couponCardId));
        return null;
    }

    @PostMapping("/create")
    @ApiOperation("创建订单")
    @PreAuthenticated
    public CommonResult<Long> createTradeOrder(@RequestBody AppTradeOrderCreateReqVO createReqVO,
                                               HttpServletRequest servletRequest) {
//        return success(tradeOrderService.createTradeOrder(UserSecurityContextHolder.getUserId(),
//                HttpUtil.getIp(servletRequest), createReqVO));
        // 获取登录用户
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 获取用户ip地址
        String clientIp = ServletUtil.getClientIP(servletRequest);
        // 创建交易订单，预支付记录
        Long result = tradeOrderService.createTradeOrder(loginUserId, clientIp, createReqVO);

        return CommonResult.success(result);
    }

    @GetMapping("/get")
    @ApiOperation("获得交易订单")
    @ApiImplicitParam(name = "tradeOrderId", value = "交易订单编号", required = true)
    public CommonResult<TradeOrderRespVO> getTradeOrder(@RequestParam("tradeOrderId") Integer tradeOrderId) {
//        return success(tradeOrderService.getTradeOrder(tradeOrderId));
        return null;
    }

    @GetMapping("/page")
    @ApiOperation("获得订单交易分页")
    public CommonResult<PageResult<TradeOrderRespVO>> pageTradeOrder(TradeOrderPageReqVO pageVO) {
//        return success(tradeOrderService.pageTradeOrder(UserSecurityContextHolder.getUserId(), pageVO));
        return null;
    }

}
