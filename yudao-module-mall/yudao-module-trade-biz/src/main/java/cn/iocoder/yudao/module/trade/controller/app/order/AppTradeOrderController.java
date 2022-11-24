package cn.iocoder.yudao.module.trade.controller.app.order;

import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderGetCreateInfoRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.TradeOrderRespVO;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "用户 App - 交易订单")
@RestController
@RequestMapping("/trade/order")
@Validated
@Slf4j
public class AppTradeOrderController {

    @Resource
    private TradeOrderService tradeOrderService;

    @GetMapping("/get-create-info")
    @ApiOperation("基于商品，确认创建订单")
    @PreAuthenticated
    public CommonResult<AppTradeOrderGetCreateInfoRespVO> getOrderCreateInfo(AppTradeOrderCreateReqVO createReqVO) {
//        return success(tradeOrderService.getOrderConfirmCreateInfo(UserSecurityContextHolder.getUserId(), skuId, quantity, couponCardId));
        return null;
    }

    @PostMapping("/create")
    @ApiOperation("创建订单")
    @PreAuthenticated
    public CommonResult<Long> createOrder(@RequestBody AppTradeOrderCreateReqVO createReqVO,
                                          HttpServletRequest servletRequest) {
        // 获取登录用户、用户 IP 地址
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        String clientIp = ServletUtil.getClientIP(servletRequest);
        // 创建交易订单，预支付记录
        Long orderId = tradeOrderService.createOrder(loginUserId, clientIp, createReqVO);
        return CommonResult.success(orderId);
    }

    @PostMapping("/update-paid")
    @ApiOperation(value = "更新订单为已支付", notes = "由 pay-module 支付服务，进行回调，可见 PayNotifyJob")
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        return null;
    }

    @GetMapping("/get")
    @ApiOperation("获得交易订单")
    @ApiImplicitParam(name = "tradeOrderId", value = "交易订单编号", required = true, dataTypeClass = Long.class)
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
