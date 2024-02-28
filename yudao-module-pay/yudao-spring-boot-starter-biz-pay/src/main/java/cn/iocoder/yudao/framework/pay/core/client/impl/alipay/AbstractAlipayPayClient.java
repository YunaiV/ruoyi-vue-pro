package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig.MODE_CERTIFICATE;

/**
 * 支付宝抽象类，实现支付宝统一的接口、以及部分实现（退款）
 *
 * @author jason
 */
@Slf4j
public abstract class AbstractAlipayPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    @Getter // 仅用于单测场景
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

    // ============ 支付相关 ==========

    /**
     * 构造支付关闭的 {@link PayOrderRespDTO} 对象
     *
     * @return 支付关闭的 {@link PayOrderRespDTO} 对象
     */
    protected PayOrderRespDTO buildClosedPayOrderRespDTO(PayOrderUnifiedReqDTO reqDTO, AlipayResponse response) {
        Assert.isFalse(response.isSuccess());
        return PayOrderRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                reqDTO.getOutTradeNo(), response);
    }

    @Override
    public PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body) throws Throwable {
        // 1. 校验回调数据
        Map<String, String> bodyObj = HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
        AlipaySignature.rsaCheckV1(bodyObj, config.getAlipayPublicKey(),
                StandardCharsets.UTF_8.name(), config.getSignType());

        // 2. 解析订单的状态
        // 额外说明：支付宝不仅仅支付成功会回调，再各种触发支付单数据变化时，都会进行回调，所以这里 status 的解析会写的比较复杂
        Integer status = parseStatus(bodyObj.get("trade_status"));
        // 特殊逻辑: 支付宝没有退款成功的状态，所以，如果有退款金额，我们认为是退款成功
        if (MapUtil.getDouble(bodyObj, "refund_fee", 0D) > 0) {
            status = PayOrderStatusRespEnum.REFUND.getStatus();
        }
        Assert.notNull(status, (Supplier<Throwable>) () -> {
            throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", body));
        });
        return PayOrderRespDTO.of(status, bodyObj.get("trade_no"), bodyObj.get("seller_id"), parseTime(params.get("gmt_payment")),
                bodyObj.get("out_trade_no"), body);
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) throws Throwable {
        // 1.1 构建 AlipayTradeRefundModel 请求
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(outTradeNo);
        // 1.2 构建 AlipayTradeQueryRequest 请求
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        AlipayTradeQueryResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)) {
            // 证书模式
            response = client.certificateExecute(request);
        } else {
            response = client.execute(request);
        }
        if (!response.isSuccess()) { // 不成功，例如说订单不存在
            return PayOrderRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                    outTradeNo, response);
        }
        // 2.2 解析订单的状态
        Integer status = parseStatus(response.getTradeStatus());
        Assert.notNull(status, () -> {
            throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", response.getBody()));
        });
        return PayOrderRespDTO.of(status, response.getTradeNo(), response.getBuyerUserId(), LocalDateTimeUtil.of(response.getSendPayDate()),
                outTradeNo, response);
    }

    private static Integer parseStatus(String tradeStatus) {
        return Objects.equals("WAIT_BUYER_PAY", tradeStatus) ? PayOrderStatusRespEnum.WAITING.getStatus()
                : ObjectUtils.equalsAny(tradeStatus, "TRADE_FINISHED", "TRADE_SUCCESS") ? PayOrderStatusRespEnum.SUCCESS.getStatus()
                : Objects.equals("TRADE_CLOSED", tradeStatus) ? PayOrderStatusRespEnum.CLOSED.getStatus() : null;
    }

    // ============ 退款相关 ==========

    /**
     * 支付宝统一的退款接口 alipay.trade.refund
     *
     * @param reqDTO 退款请求 request DTO
     * @return 退款请求 Response
     */
    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 构建 AlipayTradeRefundModel 请求
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setOutRequestNo(reqDTO.getOutRefundNo());
        model.setRefundAmount(formatAmount(reqDTO.getRefundPrice()));
        model.setRefundReason(reqDTO.getReason());
        // 1.2 构建 AlipayTradePayRequest 请求
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);

        // 2.1 执行请求
        AlipayTradeRefundResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)) {  // 证书模式
            response = client.certificateExecute(request);
        } else {
            response = client.execute(request);
        }
        if (!response.isSuccess()) {
            // 当出现 ACQ.SYSTEM_ERROR, 退款可能成功也可能失败。 返回 WAIT 状态. 后续 job 会轮询
            if (ObjectUtils.equalsAny(response.getSubCode(), "ACQ.SYSTEM_ERROR", "SYSTEM_ERROR")) {
                return PayRefundRespDTO.waitingOf(null, reqDTO.getOutRefundNo(), response);
            }
            return PayRefundRespDTO.failureOf(response.getSubCode(), response.getSubMsg(), reqDTO.getOutRefundNo(), response);
        }
        // 2.2 创建返回结果
        // 支付宝只要退款调用返回 success，就认为退款成功，不需要回调。具体可见 parseNotify 方法的说明。
        // 另外，支付宝没有退款单号，所以不用设置
        return PayRefundRespDTO.successOf(null, LocalDateTimeUtil.of(response.getGmtRefundPay()),
                reqDTO.getOutRefundNo(), response);
    }

    @Override
    public PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body) {
        // 补充说明：支付宝退款时，没有回调，这点和微信支付是不同的。并且，退款分成部分退款、和全部退款。
        // ① 部分退款：是会有回调，但是它回调的是订单状态的同步回调，不是退款订单的回调
        // ② 全部退款：Wap 支付有订单状态的同步回调，但是 PC/扫码又没有
        // 所以，这里在解析时，即使是退款导致的订单状态同步，我们也忽略不做为“退款同步”，而是订单的回调。
        // 实际上，支付宝退款只要发起成功，就可以认为退款成功，不需要等待回调。
        throw new UnsupportedOperationException("支付宝无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws AlipayApiException {
        // 1.1 构建 AlipayTradeFastpayRefundQueryModel 请求
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(outTradeNo);
        model.setOutRequestNo(outRefundNo);
        model.setQueryOptions(Collections.singletonList("gmt_refund_pay"));
        // 1.2 构建 AlipayTradeFastpayRefundQueryRequest 请求
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);

        // 2.1 执行请求
        AlipayTradeFastpayRefundQueryResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)) { // 证书模式
            response = client.certificateExecute(request);
        } else {
            response = client.execute(request);
        }
        if (!response.isSuccess()) {
            // 明确不存在的情况，应该就是失败，可进行关闭
            if (ObjectUtils.equalsAny(response.getSubCode(), "TRADE_NOT_EXIST", "ACQ.TRADE_NOT_EXIST")) {
                return PayRefundRespDTO.failureOf(outRefundNo, response);
            }
            // 可能存在“ACQ.SYSTEM_ERROR”系统错误等情况，所以返回 WAIT 继续等待
            return PayRefundRespDTO.waitingOf(null, outRefundNo, response);
        }
        // 2.2 创建返回结果
        if (Objects.equals(response.getRefundStatus(), "REFUND_SUCCESS")) {
            return PayRefundRespDTO.successOf(null, LocalDateTimeUtil.of(response.getGmtRefundPay()),
                    outRefundNo, response);
        }
        return PayRefundRespDTO.waitingOf(null, outRefundNo, response);
    }

    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 校验公钥类型 必须使用公钥证书模式
        if (!Objects.equals(config.getMode(), MODE_CERTIFICATE)) {
            throw exception0(ERROR_CONFIGURATION.getCode(), "支付宝单笔转账必须使用公钥证书模式");
        }
        // 1.2 构建 AlipayFundTransUniTransferModel
        AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
        // ① 通用的参数
        model.setTransAmount(formatAmount(reqDTO.getPrice())); // 转账金额
        model.setOrderTitle(reqDTO.getSubject());               // 转账业务的标题，用于在支付宝用户的账单里显示。
        model.setOutBizNo(reqDTO.getOutTransferNo());
        model.setProductCode("TRANS_ACCOUNT_NO_PWD");    // 销售产品码。单笔无密转账固定为 TRANS_ACCOUNT_NO_PWD
        model.setBizScene("DIRECT_TRANSFER");           // 业务场景 单笔无密转账固定为 DIRECT_TRANSFER
        if (reqDTO.getChannelExtras() != null) {
            model.setBusinessParams(JsonUtils.toJsonString(reqDTO.getChannelExtras()));
        }
        // ② 个性化的参数
        Participant payeeInfo = new Participant();
        PayTransferTypeEnum transferType = PayTransferTypeEnum.typeOf(reqDTO.getType());
        switch (transferType) {
            // TODO @jason：是不是不用传递 transferType 参数哈？因为应该已经明确是支付宝啦？
            // @芋艿。 是不是还要考虑转账到银行卡。所以传 transferType 但是转账到银行卡不知道要如何测试??
            case ALIPAY_BALANCE: {
                payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
                payeeInfo.setIdentity(reqDTO.getAlipayLogonId()); // 支付宝登录号
                payeeInfo.setName(reqDTO.getUserName()); // 支付宝账号姓名
                model.setPayeeInfo(payeeInfo);
                break;
            }
            case BANK_CARD: {
                payeeInfo.setIdentityType("BANKCARD_ACCOUNT");
                // TODO 待实现
                throw exception(NOT_IMPLEMENTED);
            }
            default: {
                throw exception0(BAD_REQUEST.getCode(), "不正确的转账类型: {}", transferType);
            }
        }
        // 1.3 构建 AlipayFundTransUniTransferRequest
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
        request.setBizModel(model);
        // 执行请求
        AlipayFundTransUniTransferResponse response = client.certificateExecute(request);
        // 处理结果
        if (!response.isSuccess()) {
            // 当出现 SYSTEM_ERROR, 转账可能成功也可能失败。 返回 WAIT 状态. 后续 job 会轮询，或相同 outBizNo 重新发起转账
            // 发现 outBizNo 相同 两次请求参数相同. 会返回 "PAYMENT_INFO_INCONSISTENCY", 不知道哪里的问题. 暂时返回 WAIT. 后续job 会轮询
            if (ObjectUtils.equalsAny(response.getSubCode(),"PAYMENT_INFO_INCONSISTENCY", "SYSTEM_ERROR", "ACQ.SYSTEM_ERROR")) {
                return PayTransferRespDTO.waitingOf(null, reqDTO.getOutTransferNo(), response);
            }
            return PayTransferRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                    reqDTO.getOutTransferNo(), response);
        } else {
            if (ObjectUtils.equalsAny(response.getStatus(), "REFUND", "FAIL")) { // 转账到银行卡会出现 "REFUND" "FAIL"
                return PayTransferRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                        reqDTO.getOutTransferNo(), response);
            }
            if (Objects.equals(response.getStatus(), "DEALING")) { // 转账到银行卡会出现 "DEALING"  处理中
                return PayTransferRespDTO.dealingOf(response.getOrderId(), reqDTO.getOutTransferNo(), response);
            }
            return PayTransferRespDTO.successOf(response.getOrderId(), parseTime(response.getTransDate()),
                    response.getOutBizNo(), response);
        }

    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo, PayTransferTypeEnum type) throws Throwable {
        // 1.1 构建 AlipayFundTransCommonQueryModel
        AlipayFundTransCommonQueryModel model = new AlipayFundTransCommonQueryModel();
        model.setProductCode(type == PayTransferTypeEnum.BANK_CARD ? "TRANS_BANKCARD_NO_PWD" : "TRANS_ACCOUNT_NO_PWD");
        model.setBizScene("DIRECT_TRANSFER"); //业务场景
        model.setOutBizNo(outTradeNo);
        // 1.2 构建 AlipayFundTransCommonQueryRequest
        AlipayFundTransCommonQueryRequest request = new AlipayFundTransCommonQueryRequest();
        request.setBizModel(model);

        // 2.1 执行请求
        AlipayFundTransCommonQueryResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)) { // 证书模式
            response = client.certificateExecute(request);
        } else {
            response = client.execute(request);
        }
        // 2.2 处理返回结果
        if (response.isSuccess()) {
            if (ObjectUtils.equalsAny(response.getStatus(), "REFUND", "FAIL")) { // 转账到银行卡会出现 "REFUND" "FAIL"
                return PayTransferRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                        outTradeNo, response);
            }
            if (Objects.equals(response.getStatus(), "DEALING")) { // 转账到银行卡会出现 "DEALING" 处理中
                return PayTransferRespDTO.dealingOf(response.getOrderId(), outTradeNo, response);
            }
            return PayTransferRespDTO.successOf(response.getOrderId(), parseTime(response.getPayDate()),
                    response.getOutBizNo(), response);
        } else {
            // 当出现 SYSTEM_ERROR, 转账可能成功也可能失败。 返回 WAIT 状态. 后续 job 会轮询, 或相同 outBizNo 重新发起转账
            // 当出现 ORDER_NOT_EXIST 可能是转账还在处理中,也可能是转账处理失败. 返回 WAIT 状态. 后续 job 会轮询, 或相同 outBizNo 重新发起转账
            if (ObjectUtils.equalsAny(response.getSubCode(), "ORDER_NOT_EXIST", "SYSTEM_ERROR", "ACQ.SYSTEM_ERROR")) {
                return PayTransferRespDTO.waitingOf(null, outTradeNo, response);
            }
            return PayTransferRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                    outTradeNo, response);
        }
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

}
