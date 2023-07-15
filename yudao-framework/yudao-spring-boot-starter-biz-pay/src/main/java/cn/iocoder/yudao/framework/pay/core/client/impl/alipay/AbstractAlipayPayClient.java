package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

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
     *
     * @param reqDTO 退款请求 request DTO
     * @return 退款请求 Response
     */
    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO)  {
        // 1.1 构建 AlipayTradeRefundModel 请求
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setOutRequestNo(reqDTO.getOutRefundNo());
        model.setRefundAmount(formatAmount(reqDTO.getPrice()));
//        model.setRefundAmount(formatAmount(reqDTO.getPrice() / 2));
        model.setRefundReason(reqDTO.getReason());
        // 1.2 构建 AlipayTradePayRequest 请求
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        try {
            // 2.1 执行请求
            AlipayTradeRefundResponse response =  client.execute(request);
            PayRefundRespDTO refund = new PayRefundRespDTO()
                    .setRawData(response);
            // 支付宝只要退款调用返回 success，就认为退款成功，不需要回调。具体可见 parseNotify 方法的说明。
            // 另外，支付宝没有退款单号，所以不用设置
            if (response.isSuccess()) {
                refund.setStatus(PayOrderStatusRespEnum.SUCCESS.getStatus())
                        .setSuccessTime(LocalDateTimeUtil.of(response.getGmtRefundPay()));
                Assert.notNull(refund.getSuccessTime(), "退款成功时间不能为空");
            } else {
                refund.setStatus(PayOrderStatusRespEnum.CLOSED.getStatus());
            }
            return refund;
        } catch (AlipayApiException e) {
            log.error("[doUnifiedRefund][request({}) 发起退款异常]", toJsonString(reqDTO), e);
            return null;
        }
    }

    @Override
    @SneakyThrows
    public Object parseNotify(Map<String, String> params, String body) {
        // 补充说明：支付宝退款时，没有回调，这点和微信支付是不同的。并且，退款分成部分退款、和全部退款。
        // ① 部分退款：是会有回调，但是它回调的是订单状态的同步回调，不是退款订单的回调
        // ② 全部退款：Wap 支付有订单状态的同步回调，但是 PC/扫码又没有
        // 所以，这里在解析时，即使是退款导致的订单状态同步，我们也忽略不做为“退款同步”，而是订单的回调。
        // 实际上，支付宝退款只要发起成功，就可以认为退款成功，不需要等待回调。

        // 1. 校验回调数据
        Map<String, String> bodyObj = HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
        AlipaySignature.rsaCheckV1(bodyObj, config.getAlipayPublicKey(),
                StandardCharsets.UTF_8.name(), config.getSignType());

        // 2. 解析订单的状态
        String tradeStatus = bodyObj.get("trade_status");
        PayOrderStatusRespEnum status = Objects.equals("WAIT_BUYER_PAY", tradeStatus) ? PayOrderStatusRespEnum.WAITING
                : Objects.equals("TRADE_SUCCESS", tradeStatus) ? PayOrderStatusRespEnum.SUCCESS
                : Objects.equals("TRADE_CLOSED", tradeStatus) ? PayOrderStatusRespEnum.CLOSED : null;
        Assert.notNull(status, (Supplier<Throwable>) () -> {
            throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", body));
        });
        return PayOrderRespDTO.builder()
                .status(Objects.requireNonNull(status).getStatus())
                .outTradeNo(bodyObj.get("out_trade_no"))
                .channelOrderNo(bodyObj.get("trade_no"))
                .channelUserId(bodyObj.get("seller_id"))
                .successTime(parseTime(params.get("gmt_payment")))
                .rawData(body)
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
