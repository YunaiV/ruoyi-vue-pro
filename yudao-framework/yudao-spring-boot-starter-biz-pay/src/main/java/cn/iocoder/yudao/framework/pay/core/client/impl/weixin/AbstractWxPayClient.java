package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.TemporalAccessorUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
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
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.*;
import com.github.binarywang.wxpay.bean.result.*;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

import static cn.hutool.core.date.DatePattern.*;
import static cn.iocoder.yudao.framework.pay.core.client.impl.weixin.WxPayClientConfig.API_VERSION_V2;

/**
 * 微信支付抽象类，实现微信统一的接口、以及部分实现（退款）
 *
 * @author 遇到源码
 */
@Slf4j
public abstract class AbstractWxPayClient extends AbstractPayClient<WxPayClientConfig> {

    protected WxPayService client;

    public AbstractWxPayClient(Long channelId, String channelCode, WxPayClientConfig config) {
        super(channelId, channelCode, config);
    }

    /**
     * 初始化 client 客户端
     *
     * @param tradeType 交易类型
     */
    protected void doInit(String tradeType) {
        // 创建 config 配置
        WxPayConfig payConfig = new WxPayConfig();
        BeanUtil.copyProperties(config, payConfig, "keyContent", "privateKeyContent", "privateCertContent");
        payConfig.setTradeType(tradeType);
        // weixin-pay-java 无法设置内容，只允许读取文件，所以这里要创建临时文件来解决
        if (Base64.isBase64(config.getKeyContent())) {
            payConfig.setKeyPath(FileUtils.createTempFile(Base64.decode(config.getKeyContent())).getPath());
        }
        if (StrUtil.isNotEmpty(config.getPrivateKeyContent())) {
            payConfig.setPrivateKeyPath(FileUtils.createTempFile(config.getPrivateKeyContent()).getPath());
        }
        if (StrUtil.isNotEmpty(config.getPrivateCertContent())) {
            payConfig.setPrivateCertPath(FileUtils.createTempFile(config.getPrivateCertContent()).getPath());
        }

        // 创建 client 客户端
        client = new WxPayServiceImpl();
        client.setConfig(payConfig);
    }

    // ============ 支付相关 ==========

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Exception {
        try {
            switch (config.getApiVersion()) {
                case API_VERSION_V2:
                    return doUnifiedOrderV2(reqDTO);
                case WxPayClientConfig.API_VERSION_V3:
                    return doUnifiedOrderV3(reqDTO);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            String errorCode = getErrorCode(e);
            String errorMessage = getErrorMessage(e);
            return PayOrderRespDTO.closedOf(errorCode, errorMessage,
                    reqDTO.getOutTradeNo(), e.getXmlString());
        }
    }

    /**
     * 【V2】调用支付渠道，统一下单
     *
     * @param reqDTO 下单信息
     * @return 各支付渠道的返回结果
     */
    protected abstract PayOrderRespDTO doUnifiedOrderV2(PayOrderUnifiedReqDTO reqDTO)
            throws Exception;

    /**
     * 【V3】调用支付渠道，统一下单
     *
     * @param reqDTO 下单信息
     * @return 各支付渠道的返回结果
     */
    protected abstract PayOrderRespDTO doUnifiedOrderV3(PayOrderUnifiedReqDTO reqDTO)
            throws WxPayException;

    /**
     * 【V2】创建微信下单请求
     *
     * @param reqDTO 下信息
     * @return 下单请求
     */
    protected WxPayUnifiedOrderRequest buildPayUnifiedOrderRequestV2(PayOrderUnifiedReqDTO reqDTO) {
        return WxPayUnifiedOrderRequest.newBuilder()
                .outTradeNo(reqDTO.getOutTradeNo())
                .body(reqDTO.getSubject())
                .detail(reqDTO.getBody())
                .totalFee(reqDTO.getPrice()) // 单位分
                .timeExpire(formatDateV2(reqDTO.getExpireTime()))
                .spbillCreateIp(reqDTO.getUserIp())
                .notifyUrl(reqDTO.getNotifyUrl())
                .build();
    }

    /**
     * 【V3】创建微信下单请求
     *
     * @param reqDTO 下信息
     * @return 下单请求
     */
    protected WxPayUnifiedOrderV3Request buildPayUnifiedOrderRequestV3(PayOrderUnifiedReqDTO reqDTO) {
        WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
        request.setOutTradeNo(reqDTO.getOutTradeNo());
        request.setDescription(reqDTO.getSubject());
        request.setAmount(new WxPayUnifiedOrderV3Request.Amount().setTotal(reqDTO.getPrice())); // 单位分
        request.setTimeExpire(formatDateV3(reqDTO.getExpireTime()));
        request.setSceneInfo(new WxPayUnifiedOrderV3Request.SceneInfo().setPayerClientIp(reqDTO.getUserIp()));
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        return request;
    }

    @Override
    public PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body) throws WxPayException {
        switch (config.getApiVersion()) {
            case API_VERSION_V2:
                return doParseOrderNotifyV2(body);
            case WxPayClientConfig.API_VERSION_V3:
                return doParseOrderNotifyV3(body);
            default:
                throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
        }
    }

    private PayOrderRespDTO doParseOrderNotifyV2(String body) throws WxPayException {
        // 1. 解析回调
        WxPayOrderNotifyResult response = client.parseOrderNotifyResult(body);
        // 2. 构建结果
        // V2 微信支付的回调，只有 SUCCESS 支付成功、CLOSED 支付失败两种情况，无需像支付宝一样解析的比较复杂
        Integer status = Objects.equals(response.getResultCode(), "SUCCESS") ?
                PayOrderStatusRespEnum.SUCCESS.getStatus() : PayOrderStatusRespEnum.CLOSED.getStatus();
        return PayOrderRespDTO.of(status, response.getTransactionId(), response.getOpenid(), parseDateV2(response.getTimeEnd()),
                response.getOutTradeNo(), body);
    }

    private PayOrderRespDTO doParseOrderNotifyV3(String body) throws WxPayException {
        // 1. 解析回调
        WxPayNotifyV3Result response = client.parseOrderNotifyV3Result(body, null);
        WxPayNotifyV3Result.DecryptNotifyResult result = response.getResult();
        // 2. 构建结果
        Integer status = parseStatus(result.getTradeState());
        String openid = result.getPayer() != null ? result.getPayer().getOpenid() : null;
        return PayOrderRespDTO.of(status, result.getTransactionId(), openid, parseDateV3(result.getSuccessTime()),
                result.getOutTradeNo(), body);
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) throws Throwable {
        try {
            switch (config.getApiVersion()) {
                case API_VERSION_V2:
                    return doGetOrderV2(outTradeNo);
                case WxPayClientConfig.API_VERSION_V3:
                    return doGetOrderV3(outTradeNo);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            if (ObjectUtils.equalsAny(e.getErrCode(), "ORDERNOTEXIST", "ORDER_NOT_EXIST")) {
                String errorCode = getErrorCode(e);
                String errorMessage = getErrorMessage(e);
                return PayOrderRespDTO.closedOf(errorCode, errorMessage,
                        outTradeNo, e.getXmlString());
            }
            throw e;
        }
    }

    private PayOrderRespDTO doGetOrderV2(String outTradeNo) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayOrderQueryRequest request = WxPayOrderQueryRequest.newBuilder()
                .outTradeNo(outTradeNo).build();
        // 执行请求
        WxPayOrderQueryResult response = client.queryOrder(request);

        // 转换结果
        Integer status = parseStatus(response.getTradeState());
        return PayOrderRespDTO.of(status, response.getTransactionId(), response.getOpenid(), parseDateV2(response.getTimeEnd()),
                outTradeNo, response);
    }

    private PayOrderRespDTO doGetOrderV3(String outTradeNo) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayOrderQueryV3Request request = new WxPayOrderQueryV3Request()
                .setOutTradeNo(outTradeNo);
        // 执行请求
        WxPayOrderQueryV3Result response = client.queryOrderV3(request);

        // 转换结果
        Integer status = parseStatus(response.getTradeState());
        String openid = response.getPayer() != null ? response.getPayer().getOpenid() : null;
        return PayOrderRespDTO.of(status, response.getTransactionId(), openid, parseDateV3(response.getSuccessTime()),
                outTradeNo, response);
    }

    private static Integer parseStatus(String tradeState) {
        switch (tradeState) {
            case "NOTPAY":
            case "USERPAYING": // 支付中，等待用户输入密码（条码支付独有）
                return PayOrderStatusRespEnum.WAITING.getStatus();
            case "SUCCESS":
                return PayOrderStatusRespEnum.SUCCESS.getStatus();
            case "REFUND":
                return PayOrderStatusRespEnum.REFUND.getStatus();
            case "CLOSED":
            case "REVOKED": // 已撤销（刷卡支付独有）
            case "PAYERROR": // 支付失败（其它原因，如银行返回失败）
                return PayOrderStatusRespEnum.CLOSED.getStatus();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知的支付状态({})", tradeState));
        }
    }

    // ============ 退款相关 ==========

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        try {
            switch (config.getApiVersion()) {
                case API_VERSION_V2:
                    return doUnifiedRefundV2(reqDTO);
                case WxPayClientConfig.API_VERSION_V3:
                    return doUnifiedRefundV3(reqDTO);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            String errorCode = getErrorCode(e);
            String errorMessage = getErrorMessage(e);
            return PayRefundRespDTO.failureOf(errorCode, errorMessage,
                    reqDTO.getOutTradeNo(), e.getXmlString());
        }
    }

    private PayRefundRespDTO doUnifiedRefundV2(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        // 1. 构建 WxPayRefundRequest 请求
        WxPayRefundRequest request = new WxPayRefundRequest()
                .setOutTradeNo(reqDTO.getOutTradeNo())
                .setOutRefundNo(reqDTO.getOutRefundNo())
                .setRefundFee(reqDTO.getRefundPrice())
                .setRefundDesc(reqDTO.getReason())
                .setTotalFee(reqDTO.getPayPrice())
                .setNotifyUrl(reqDTO.getNotifyUrl());
        // 2.1 执行请求
        WxPayRefundResult response = client.refundV2(request);
        // 2.2 创建返回结果
        if (Objects.equals("SUCCESS", response.getResultCode())) { // V2 情况下，不直接返回退款成功，而是等待异步通知
            return PayRefundRespDTO.waitingOf(response.getRefundId(),
                    reqDTO.getOutRefundNo(), response);
        }
        return PayRefundRespDTO.failureOf(reqDTO.getOutRefundNo(), response);
    }

    private PayRefundRespDTO doUnifiedRefundV3(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        // 1. 构建 WxPayRefundRequest 请求
        WxPayRefundV3Request request = new WxPayRefundV3Request()
                .setOutTradeNo(reqDTO.getOutTradeNo())
                .setOutRefundNo(reqDTO.getOutRefundNo())
                .setAmount(new WxPayRefundV3Request.Amount().setRefund(reqDTO.getRefundPrice())
                        .setTotal(reqDTO.getPayPrice()).setCurrency("CNY"))
                .setReason(reqDTO.getReason())
                .setNotifyUrl(reqDTO.getNotifyUrl());
        // 2.1 执行请求
        WxPayRefundV3Result response = client.refundV3(request);
        // 2.2 创建返回结果
        if (Objects.equals("SUCCESS", response.getStatus())) {
            return PayRefundRespDTO.successOf(response.getRefundId(), parseDateV3(response.getSuccessTime()),
                    reqDTO.getOutRefundNo(), response);
        }
        if (Objects.equals("PROCESSING", response.getStatus())) {
            return PayRefundRespDTO.waitingOf(response.getRefundId(),
                    reqDTO.getOutRefundNo(), response);
        }
        return PayRefundRespDTO.failureOf(reqDTO.getOutRefundNo(), response);
    }

    @Override
    public PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body) throws WxPayException {
        switch (config.getApiVersion()) {
            case API_VERSION_V2:
                return doParseRefundNotifyV2(body);
            case WxPayClientConfig.API_VERSION_V3:
                return parseRefundNotifyV3(body);
            default:
                throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
        }
    }

    private PayRefundRespDTO doParseRefundNotifyV2(String body) throws WxPayException {
        // 1. 解析回调
        WxPayRefundNotifyResult response = client.parseRefundNotifyResult(body);
        WxPayRefundNotifyResult.ReqInfo result = response.getReqInfo();
        // 2. 构建结果
        if (Objects.equals("SUCCESS", result.getRefundStatus())) {
            return PayRefundRespDTO.successOf(result.getRefundId(), parseDateV2B(result.getSuccessTime()),
                    result.getOutRefundNo(), response);
        }
        return PayRefundRespDTO.failureOf(result.getOutRefundNo(), response);
    }

    private PayRefundRespDTO parseRefundNotifyV3(String body) throws WxPayException {
        // 1. 解析回调
        WxPayRefundNotifyV3Result response = client.parseRefundNotifyV3Result(body, null);
        WxPayRefundNotifyV3Result.DecryptNotifyResult result = response.getResult();
        // 2. 构建结果
        if (Objects.equals("SUCCESS", result.getRefundStatus())) {
            return PayRefundRespDTO.successOf(result.getRefundId(), parseDateV3(result.getSuccessTime()),
                    result.getOutRefundNo(), response);
        }
        return PayRefundRespDTO.failureOf(result.getOutRefundNo(), response);
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws WxPayException {
        try {
            switch (config.getApiVersion()) {
                case API_VERSION_V2:
                    return doGetRefundV2(outTradeNo, outRefundNo);
                case WxPayClientConfig.API_VERSION_V3:
                    return doGetRefundV3(outTradeNo, outRefundNo);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            if (ObjectUtils.equalsAny(e.getErrCode(), "REFUNDNOTEXIST", "RESOURCE_NOT_EXISTS")) {
                String errorCode = getErrorCode(e);
                String errorMessage = getErrorMessage(e);
                return PayRefundRespDTO.failureOf(errorCode, errorMessage,
                        outRefundNo, e.getXmlString());
            }
            throw e;
        }
    }

    private PayRefundRespDTO doGetRefundV2(String outTradeNo, String outRefundNo) throws WxPayException {
        // 1. 构建 WxPayRefundRequest 请求
        WxPayRefundQueryRequest request = WxPayRefundQueryRequest.newBuilder()
                .outTradeNo(outTradeNo)
                .outRefundNo(outRefundNo)
                .build();
        // 2.1 执行请求
        WxPayRefundQueryResult response = client.refundQuery(request);
        // 2.2 创建返回结果
        if (!Objects.equals("SUCCESS", response.getResultCode())) {
            return PayRefundRespDTO.waitingOf(null,
                    outRefundNo, response);
        }
        WxPayRefundQueryResult.RefundRecord refund = CollUtil.findOne(response.getRefundRecords(),
                record -> record.getOutRefundNo().equals(outRefundNo));
        if (refund == null) {
            return PayRefundRespDTO.failureOf(outRefundNo, response);
        }
        switch (refund.getRefundStatus()) {
            case "SUCCESS":
                return PayRefundRespDTO.successOf(refund.getRefundId(), parseDateV2B(refund.getRefundSuccessTime()),
                        outRefundNo, response);
            case "PROCESSING":
                return PayRefundRespDTO.waitingOf(refund.getRefundId(),
                        outRefundNo, response);
            case "CHANGE": // 退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款
            case "FAIL":
                return PayRefundRespDTO.failureOf(outRefundNo, response);
            default:
                throw new IllegalArgumentException(String.format("未知的退款状态(%s)", refund.getRefundStatus()));
        }
    }

    private PayRefundRespDTO doGetRefundV3(String outTradeNo, String outRefundNo) throws WxPayException {
        // 1. 构建 WxPayRefundRequest 请求
        WxPayRefundQueryV3Request request = new WxPayRefundQueryV3Request();
        request.setOutRefundNo(outRefundNo);
        // 2.1 执行请求
        WxPayRefundQueryV3Result response = client.refundQueryV3(request);
        // 2.2 创建返回结果
        switch (response.getStatus()) {
            case "SUCCESS":
                return PayRefundRespDTO.successOf(response.getRefundId(), parseDateV3(response.getSuccessTime()),
                        outRefundNo, response);
            case "PROCESSING":
                return PayRefundRespDTO.waitingOf(response.getRefundId(),
                        outRefundNo, response);
            case "ABNORMAL": // 退款异常
            case "CLOSED":
                return PayRefundRespDTO.failureOf(outRefundNo, response);
            default:
                throw new IllegalArgumentException(String.format("未知的退款状态(%s)", response.getStatus()));
        }
    }

    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) {
       throw new UnsupportedOperationException("待实现");
    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo, PayTransferTypeEnum type) {
        throw new UnsupportedOperationException("待实现");
    }

    // ========== 各种工具方法 ==========

    static String formatDateV2(LocalDateTime time) {
        return TemporalAccessorUtil.format(time.atZone(ZoneId.systemDefault()), PURE_DATETIME_PATTERN);
    }

    static LocalDateTime parseDateV2(String time) {
        return LocalDateTimeUtil.parse(time, PURE_DATETIME_PATTERN);
    }

    static LocalDateTime parseDateV2B(String time) {
        return LocalDateTimeUtil.parse(time, NORM_DATETIME_PATTERN);
    }

    static String formatDateV3(LocalDateTime time) {
        return TemporalAccessorUtil.format(time.atZone(ZoneId.systemDefault()), UTC_WITH_XXX_OFFSET_PATTERN);
    }

    static LocalDateTime parseDateV3(String time) {
        return LocalDateTimeUtil.parse(time, UTC_WITH_XXX_OFFSET_PATTERN);
    }

    static String getErrorCode(WxPayException e) {
        if (StrUtil.isNotEmpty(e.getErrCode())) {
            return e.getErrCode();
        }
        if (StrUtil.isNotEmpty(e.getCustomErrorMsg())) {
            return "CUSTOM_ERROR";
        }
        return e.getReturnCode();
    }

    static String getErrorMessage(WxPayException e) {
        if (StrUtil.isNotEmpty(e.getErrCode())) {
            return e.getErrCodeDes();
        }
        if (StrUtil.isNotEmpty(e.getCustomErrorMsg())) {
            return e.getCustomErrorMsg();
        }
        return e.getReturnMsg();
    }

}
