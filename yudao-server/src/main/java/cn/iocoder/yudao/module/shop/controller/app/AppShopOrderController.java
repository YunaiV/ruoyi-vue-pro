package cn.iocoder.yudao.module.shop.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.pay.service.notify.vo.PayNotifyOrderReqVO;
import cn.iocoder.yudao.module.pay.service.notify.vo.PayRefundOrderReqVO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.util.PaySeqUtils;
import cn.iocoder.yudao.module.shop.controller.app.vo.AppShopOrderCreateRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.Duration;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

@Api(tags = "用户 APP - 商城订单")
@RestController
@RequestMapping("/shop/order")
@Validated
@Slf4j
public class AppShopOrderController {

    @Resource
    private PayOrderService payOrderService;

    @PostMapping("/create")
    @ApiOperation("创建商城订单")
//    @PreAuthenticated // TODO 暂时不加登陆验证，前端暂时没做好
    public CommonResult<AppShopOrderCreateRespVO> create() {
        // 假装创建商城订单
        Long shopOrderId = System.currentTimeMillis();

        // 创建对应的支付订单
        PayOrderCreateReqDTO reqDTO = new PayOrderCreateReqDTO();
        reqDTO.setAppId(6L);
        reqDTO.setUserIp(getClientIP());
        reqDTO.setMerchantOrderId(PaySeqUtils.genMerchantOrderNo());
        reqDTO.setSubject("标题：" + shopOrderId);
        reqDTO.setBody("内容：" + shopOrderId);
        reqDTO.setAmount(200); // 单位：分
        reqDTO.setExpireTime(DateUtils.addTime(Duration.ofDays(1)));
        Long payOrderId = payOrderService.createPayOrder(reqDTO);

        // 拼接返回
        return success(AppShopOrderCreateRespVO.builder().id(shopOrderId)
                .payOrderId(payOrderId).build());
    }

    @PostMapping("/pay-notify")
    @ApiOperation("支付回调")
    public CommonResult<Boolean> payNotify(@RequestBody @Valid PayNotifyOrderReqVO reqVO) {
        log.info("[payNotify][回调成功]");
        return success(true);
    }

    @PostMapping("/refund-notify")
    @ApiOperation("退款回调")
    public CommonResult<Boolean> refundNotify(@RequestBody @Valid PayRefundOrderReqVO reqVO) {
        log.info("[refundNotify][回调成功]");
        return success(true);
    }

}
