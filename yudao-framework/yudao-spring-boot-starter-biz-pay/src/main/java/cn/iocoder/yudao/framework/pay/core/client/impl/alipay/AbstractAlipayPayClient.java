package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayNotifyReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayRefundNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayNotifyRefundStatusEnum;
import com.alipay.api.*;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.pay.core.enums.PayFrameworkErrorCodeConstants.ORDER_UNIFIED_ERROR;

/**
 * 支付宝抽象类，实现支付宝统一的接口、以及部分实现（退款）
 *
 * @author jason
 */
@Slf4j
public abstract class AbstractAlipayPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    protected DefaultAlipayClient client;

    public AbstractAlipayPayClient(Long channelId, String channelCode, AlipayPayClientConfig config) {
        super(channelId, channelCode, config);
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    /**
     * 支付宝统一的退款接口 alipay.trade.refund
     * @param reqDTO 退款请求 request DTO
     * @return 退款请求 Response
     */
    @Override
    protected PayRefundUnifiedRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO)  {
        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setTradeNo(reqDTO.getChannelOrderNo());
        model.setOutTradeNo(reqDTO.getPayTradeNo());

        model.setOutRequestNo(reqDTO.getMerchantRefundId());
        model.setRefundAmount(formatAmount(reqDTO.getAmount()));
        model.setRefundReason(reqDTO.getReason());

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        refundRequest.setBizModel(model);
        refundRequest.setNotifyUrl(reqDTO.getNotifyUrl());
        refundRequest.setReturnUrl(reqDTO.getNotifyUrl());
        try {
            AlipayTradeRefundResponse response =  client.execute(refundRequest);
            log.info("[doUnifiedRefund][response({}) 发起退款 渠道返回", toJsonString(response));
            if (response.isSuccess()) {
                //退款导致触发的异步通知是发送到支付接口中设置的notify_url
                //支付宝不返回退款单号，设置为空
                PayRefundUnifiedRespDTO respDTO = new PayRefundUnifiedRespDTO();
                respDTO.setChannelRefundId("");
//                return PayCommonResult.build(response.getCode(), response.getMsg(), respDTO, codeMapping); TODO
                return null;
            }
            // 失败。需要抛出异常
//            return PayCommonResult.build(response.getCode(), response.getMsg(), null, codeMapping); TODO
            return null;
        } catch (AlipayApiException e) {
            // TODO 记录异常日志
            log.error("[doUnifiedRefund][request({}) 发起退款失败,网络读超时，退款状态未知]", toJsonString(reqDTO), e);
//            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping); TODO
            return null;
        }
    }

    @Override
    @SneakyThrows
    public Object parseNotify(PayNotifyReqDTO rawNotify) {
        // 1. 校验回调数据
        String body = rawNotify.getBody();
        Map<String, String> params = rawNotify.getParams();
        Map<String, String> bodyObj = HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
        AlipaySignature.rsaCheckV1(bodyObj, config.getAlipayPublicKey(),
                StandardCharsets.UTF_8.name(), "RSA2");

        // 2.1 退款的情况
        if (bodyObj.containsKey("refund_fee")) {
            return PayRefundNotifyRespDTO.builder().channelOrderNo(bodyObj.get("trade_no"))
                    .tradeNo(bodyObj.get("out_trade_no")).reqNo(bodyObj.get("out_biz_no"))
                    .status(PayNotifyRefundStatusEnum.SUCCESS)
                    .refundSuccessTime(parseTime(params.get("gmt_refund")))
                    .build();
        }
        // 2.2 支付的情况
        return PayOrderNotifyRespDTO.builder()
                .orderExtensionNo(bodyObj.get("out_trade_no"))
                .channelOrderNo(bodyObj.get("trade_no"))
                .channelUserId(bodyObj.get("seller_id"))
                .successTime(parseTime(params.get("notify_time")))
                .build();
    }

    // ========== 各种工具方法 ==========

    protected String formatAmount(Integer amount) {
        return String.valueOf(amount / 100.0);
    }

    protected String formatTime(LocalDateTime time) {
        return LocalDateTimeUtil.format(time, NORM_DATETIME_FORMATTER);
    }

    protected LocalDateTime parseTime(String str) {
        return LocalDateTimeUtil.parse(str, NORM_DATETIME_FORMATTER);
    }

    /**
     * 校验支付宝统一下单的响应
     *
     * 如果校验不通过，则抛出 {@link cn.iocoder.yudao.framework.common.exception.ServiceException} 异常
     *
     * @param request 请求
     * @param response 响应
     */
    protected void validateUnifiedOrderResponse(Object request, AlipayResponse response) {
        if (response.isSuccess()) {
            return;
        }
        log.error("[validateUnifiedOrderResponse][发起支付失败，request({})，response({})]",
                JsonUtils.toJsonString(request), JsonUtils.toJsonString(response));
        throw exception0(ORDER_UNIFIED_ERROR.getCode(), response.getSubMsg());
    }

}
