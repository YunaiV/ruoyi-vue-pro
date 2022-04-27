package cn.iocoder.yudao.module.pay.controller.app.order;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.module.pay.controller.app.order.vo.AppPayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.app.order.vo.AppPayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderSubmitReqDTO;
import cn.iocoder.yudao.module.pay.service.order.dto.PayOrderSubmitRespDTO;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

@Api(tags = "用户 APP - 支付订单")
@RestController
@RequestMapping("/pay/order")
@Validated
@Slf4j
public class AppPayOrderController {

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayRefundService refundService;

    @Resource
    private PayClientFactory payClientFactory;

    @PostMapping("/submit")
    @ApiOperation("提交支付订单")
//    @PreAuthenticated // TODO 暂时不加登陆验证，前端暂时没做好
    public CommonResult<AppPayOrderSubmitRespVO> submitPayOrder(@RequestBody AppPayOrderSubmitReqVO reqVO) {
        // 获得订单
        PayOrderDO payOrder = orderService.getOrder(reqVO.getId());

        // 提交支付
        PayOrderSubmitReqDTO reqDTO = new PayOrderSubmitReqDTO();
        BeanUtil.copyProperties(reqVO, reqDTO, false);
        reqDTO.setUserIp(getClientIP());
        reqDTO.setAppId(payOrder.getAppId());
        PayOrderSubmitRespDTO respDTO = orderService.submitPayOrder(reqDTO);

        // 拼接返回
        return success(AppPayOrderSubmitRespVO.builder().invokeResponse(respDTO.getInvokeResponse()).build());
    }

    // ========== 支付渠道的回调 ==========
    // TODO @芋艿：是不是放到 notify 模块更合适
    //TODO 芋道源码 换成了统一的地址了 /notify/{channelId}，测试通过可以删除
    @PostMapping("/notify/wx-pub/{channelId}")
    @ApiOperation("通知微信公众号支付的结果")
    public String notifyWxPayOrder(@PathVariable("channelId") Long channelId,
                                   @RequestBody String xmlData) throws Exception {
        orderService.notifyPayOrder(channelId,  PayNotifyDataDTO.builder().body(xmlData).build());
        return "success";
    }

    /**
     * 统一的跳转页面， 支付宝跳转参数说明
     * https://opendocs.alipay.com/open/203/105285#%E5%89%8D%E5%8F%B0%E5%9B%9E%E8%B7%B3%E5%8F%82%E6%95%B0%E8%AF%B4%E6%98%8E
     * @param channelId 渠道id
     * @return 返回跳转页面
     */
    @GetMapping(value = "/return/{channelId}")
    @ApiOperation("渠道统一的支付成功返回地址")
    public String returnAliPayOrder(@PathVariable("channelId") Long channelId, @RequestParam Map<String, String> params){
        //TODO 可以根据渠道和 app_id 返回不同的页面
        log.info("app_id  is {}", params.get("app_id"));
        return String.format("渠道[%s]支付成功", channelId);
    }

    /**
     * 统一的渠道支付回调，支付宝的退款回调
     *
     * @param channelId 渠道编号
     * @param params form 参数
     * @param originData http request body
     * @return 成功返回 "success"
     */
    @PostMapping(value = "/notify/{channelId}")
    @ApiOperation("渠道统一的支付成功,或退款成功 通知url")
    public String notifyChannelPay(@PathVariable("channelId") Long channelId,
                                   @RequestParam Map<String, String> params,
                                   @RequestBody String originData) throws Exception {
        // 校验支付渠道是否存在
        PayClient payClient = payClientFactory.getPayClient(channelId);
        if (payClient == null) {
            log.error("[notifyPayOrder][渠道编号({}) 找不到对应的支付客户端]", channelId);
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 校验通知数据是否合法
        PayNotifyDataDTO notifyData = PayNotifyDataDTO.builder().params(params).body(originData).build();
        payClient.verifyNotifyData(notifyData);

        // 如果是退款，则发起退款通知
        if (payClient.isRefundNotify(notifyData)) {
            refundService.notifyPayRefund(channelId, PayNotifyDataDTO.builder().params(params).body(originData).build());
            return "success";
        }

        // 如果非退款，则发起支付通知
        orderService.notifyPayOrder(channelId, PayNotifyDataDTO.builder().params(params).body(originData).build());
        return "success";
    }

}
