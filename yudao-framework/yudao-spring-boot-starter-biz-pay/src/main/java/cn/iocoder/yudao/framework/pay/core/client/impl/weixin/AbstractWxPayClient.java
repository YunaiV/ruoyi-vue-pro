package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.TemporalAccessorUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayFrameworkErrorCodeConstants;
import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

import static cn.hutool.core.date.DatePattern.PURE_DATETIME_PATTERN;
import static cn.hutool.core.date.DatePattern.UTC_WITH_XXX_OFFSET_PATTERN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
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
        BeanUtil.copyProperties(config, payConfig, "keyContent");
        payConfig.setTradeType(tradeType);
//        if (WxPayClientConfig.API_VERSION_V2.equals(config.getApiVersion())) {
//            payConfig.setSignType(WxPayConstants.SignType.MD5);
//        }
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

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Exception {
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
            // todo 芋艿：异常的处理；
            throw buildUnifiedOrderException(reqDTO, e);
        }
    }

    /**
     * 【V2】调用支付渠道，统一下单
     *
     * @param reqDTO 下单信息
     * @return 各支付渠道的返回结果
     */
    protected abstract PayOrderUnifiedRespDTO doUnifiedOrderV2(PayOrderUnifiedReqDTO reqDTO)
            throws WxPayException;

    /**
     * 【V3】调用支付渠道，统一下单
     *
     * @param reqDTO 下单信息
     * @return 各支付渠道的返回结果
     */
    protected abstract PayOrderUnifiedRespDTO doUnifiedOrderV3(PayOrderUnifiedReqDTO reqDTO)
            throws WxPayException;

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
            // todo 芋艿：异常的处理；
            throw buildUnifiedOrderException(null, e);
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
        WxPayRefundResult response = client.refundV2(request); // TODO 芋艿：可以分成 V2 和 V3 的退款接口
        // 2.2 创建返回结果
        PayRefundRespDTO refund = new PayRefundRespDTO()
                .setOutRefundNo(reqDTO.getOutRefundNo())
                .setRawData(response);
        if (Objects.equals("SUCCESS", response.getResultCode())) {
            refund.setStatus(PayRefundStatusRespEnum.WAITING.getStatus());
            refund.setChannelRefundNo(response.getRefundId());
        } else {
            refund.setStatus(PayRefundStatusRespEnum.FAILURE.getStatus());
            // TODO 芋艿；异常的处理；
        }
        return refund;
    }

    private PayRefundRespDTO doUnifiedRefundV3(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        return null;
    }

    @Override
    public Object parseNotify(Map<String, String> params, String body) {
        log.info("[parseNotify][微信支付回调 data 数据: {}]", body);
        try {
            // 微信支付 v2 回调结果处理
            switch (config.getApiVersion()) {
                case API_VERSION_V2:
                    return parseOrderNotifyV2(body);
                case WxPayClientConfig.API_VERSION_V3:
                    return parseOrderNotifyV3(body);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            log.error("[parseNotify][params({}) body({}) 解析失败]", params, body, e);
//            throw buildPayException(e);
            throw new RuntimeException(e);
            // TODO 芋艿：缺一个异常翻译
        }
    }

    private PayOrderRespDTO parseOrderNotifyV2(String body) throws WxPayException {
        WxPayOrderNotifyResult notifyResult = client.parseOrderNotifyResult(body);
        Assert.isTrue(Objects.equals(notifyResult.getResultCode(), "SUCCESS"), "支付结果非 SUCCESS");
        // 转换结果
        return PayOrderRespDTO
                .builder()
                .outTradeNo(notifyResult.getOutTradeNo())
                .channelOrderNo(notifyResult.getTransactionId())
                .channelUserId(notifyResult.getOpenid())
                .successTime(parseDateV2(notifyResult.getTimeEnd()))
                .build();
    }

    private PayOrderRespDTO parseOrderNotifyV3(String body) throws WxPayException {
        WxPayOrderNotifyV3Result notifyResult = client.parseOrderNotifyV3Result(body, null);
        WxPayOrderNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();
        // 转换结果
        Assert.isTrue(Objects.equals(notifyResult.getResult().getTradeState(), "SUCCESS"),
                "支付结果非 SUCCESS");
        return PayOrderRespDTO.builder()
                .outTradeNo(result.getOutTradeNo())
                .channelOrderNo(result.getTradeState())
                .channelUserId(result.getPayer() != null ? result.getPayer().getOpenid() : null)
                .successTime(parseDateV3(result.getSuccessTime()))
                .build();
    }

    // ========== 各种工具方法 ==========

    /**
     * 构建统一下单的异常
     *
     * 目的：将参数不正确等异常，转换成 {@link cn.iocoder.yudao.framework.common.exception.ServiceException} 业务异常
     *
     * @param reqDTO 请求
     * @param e 微信的支付异常
     * @return 转换后的异常
     *
     */
    static Exception buildUnifiedOrderException(PayOrderUnifiedReqDTO reqDTO, WxPayException e) {
        // 情况一：业务结果为 FAIL
        if (Objects.equals(e.getResultCode(), "FAIL")) {
            log.error("[buildUnifiedOrderException][request({}) 发起支付失败]", toJsonString(reqDTO), e);
            if (Objects.equals(e.getErrCode(), "PARAM_ERROR")) {
                throw invalidParamException(e.getErrCodeDes());
            }
            throw exception(PayFrameworkErrorCodeConstants.ORDER_UNIFIED_ERROR, e.getErrCodeDes());
        }
        // 情况二：状态码结果为 FAIL
        if (Objects.equals(e.getReturnCode(), "FAIL")) {
            throw exception(PayFrameworkErrorCodeConstants.ORDER_UNIFIED_ERROR, e.getReturnMsg());
        }
        // 情况三：系统异常，这里暂时不打，交给上层的 AbstractPayClient 统一打日志
        return e;
    }

    static String formatDateV2(LocalDateTime time) {
        return TemporalAccessorUtil.format(time.atZone(ZoneId.systemDefault()), PURE_DATETIME_PATTERN);
    }

    static LocalDateTime parseDateV2(String time) {
        return LocalDateTimeUtil.parse(time, PURE_DATETIME_PATTERN);
    }

    static String formatDateV3(LocalDateTime time) {
        return TemporalAccessorUtil.format(time.atZone(ZoneId.systemDefault()), UTC_WITH_XXX_OFFSET_PATTERN);
    }

    static LocalDateTime parseDateV3(String time) {
        return LocalDateTimeUtil.parse(time, UTC_WITH_XXX_OFFSET_PATTERN);
    }

}
