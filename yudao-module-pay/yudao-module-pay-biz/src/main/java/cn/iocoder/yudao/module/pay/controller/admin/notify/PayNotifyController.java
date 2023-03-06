package cn.iocoder.yudao.module.pay.controller.admin.notify;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayNotifyReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayRefundNotifyRespDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_CHANNEL_CLIENT_NOT_FOUND;

@Tag(name = "管理后台 - 支付通知")
@RestController
@RequestMapping("/pay/notify")
@Validated
@Slf4j
public class PayNotifyController {

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayRefundService refundService;

    @Resource
    private PayClientFactory payClientFactory;

    /**
     * 统一的跳转页面，支付宝跳转参数说明
     *
     * <a href="https://opendocs.alipay.com/open/203/105285#前台回跳参数说明">支付宝 - 前台回跳参数说明</a>
     *
     * @param channelId 渠道编号
     * @return 返回跳转页面
     */
    @GetMapping(value = "/return/{channelId}")
    @Operation(summary = "渠道统一的支付成功返回地址")
    @Deprecated // TODO yunai：如果是 way 的情况，应该是跳转回前端地址
    public String returnCallback(@PathVariable("channelId") Long channelId,
                                 @RequestParam Map<String, String> params) {
        log.info("[returnCallback][app_id({}) 跳转]", params.get("app_id"));
        return String.format("渠道[%s]支付成功", channelId);
    }

    /**
     * 统一的渠道支付回调，支付宝的退款回调
     *
     * @param channelId 渠道编号
     * @param params form 参数
     * @param body request body
     * @return 成功返回 "success"
     */
    @PostMapping(value = "/callback/{channelId}")
    @Operation(summary = "支付渠道的统一回调接口 - 包括支付回调，退款回调")
    @PermitAll
    @OperateLog(enable = false) // 回调地址，无需记录操作日志
    public String notifyCallback(@PathVariable("channelId") Long channelId,
                                 @RequestParam(required = false) Map<String, String> params,
                                 @RequestBody(required = false) String body) {
        log.info("[notifyCallback][channelId({}) 回调数据({}/{})]", channelId, params, body);
        // 1. 校验支付渠道是否存在
        PayClient payClient = payClientFactory.getPayClient(channelId);
        if (payClient == null) {
            log.error("[notifyCallback][渠道编号({}) 找不到对应的支付客户端]", channelId);
            throw exception(PAY_CHANNEL_CLIENT_NOT_FOUND);
        }

        // 2. 解析通知数据
        PayNotifyReqDTO rawNotify = PayNotifyReqDTO.builder().params(params).body(body).build();
        Object notify = payClient.parseNotify(rawNotify);

        // 3. 处理通知
        // 3.1：退款通知
        if (notify instanceof PayRefundNotifyRespDTO) {
            refundService.notifyPayRefund(channelId, (PayRefundNotifyRespDTO) notify, rawNotify);
            return "success";
        }
        // 3.2：支付通知
        if (notify instanceof PayOrderNotifyRespDTO) {
            orderService.notifyPayOrder(channelId, (PayOrderNotifyRespDTO) notify, rawNotify);
            return "success";
        }
        throw new UnsupportedOperationException("未知通知：" + toJsonString(notify));
    }

}
