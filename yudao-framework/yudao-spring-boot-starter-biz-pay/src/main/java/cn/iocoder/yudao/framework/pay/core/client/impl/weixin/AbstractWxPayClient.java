package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.TemporalAccessorUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayNotifyReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyV3Result;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

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
        if (StrUtil.isNotEmpty(config.getPrivateKeyContent())) {
            // weixin-pay-java 存在 BUG，无法直接设置内容，所以创建临时文件来解决
            payConfig.setPrivateKeyPath(FileUtils.createTempFile(config.getPrivateKeyContent()).getPath());
        }
        if (StrUtil.isNotEmpty(config.getPrivateCertContent())) {
            // weixin-pay-java 存在 BUG，无法直接设置内容，所以创建临时文件来解决
            payConfig.setPrivateCertPath(FileUtils.createTempFile(config.getPrivateCertContent()).getPath());
        }

        // 创建 client 客户端
        client = new WxPayServiceImpl();
        client.setConfig(payConfig);
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)
            throws Throwable {
        try {
            switch (config.getApiVersion()) {
                case WxPayClientConfig.API_VERSION_V2:
                    return doUnifiedOrderV2(reqDTO);
                case WxPayClientConfig.API_VERSION_V3:
                    return doUnifiedOrderV3(reqDTO);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            log.error("[doUnifiedOrder][request({}) 发起支付失败]", toJsonString(reqDTO), e);
            throw buildPayException(e);
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
    public Object parseNotify(PayNotifyReqDTO rawNotify) {
        log.info("[parseNotify][微信支付回调 data 数据: {}]", rawNotify.getBody());
        try {
            // 微信支付 v2 回调结果处理
            switch (config.getApiVersion()) {
                case WxPayClientConfig.API_VERSION_V2:
                    return parseOrderNotifyV2(rawNotify);
                case WxPayClientConfig.API_VERSION_V3:
                    return parseOrderNotifyV3(rawNotify);
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            log.error("[parseNotify][rawNotify({}) 解析失败]", toJsonString(rawNotify), e);
            throw buildPayException(e);
        }
    }

    private PayOrderNotifyRespDTO parseOrderNotifyV2(PayNotifyReqDTO data) throws WxPayException {
        WxPayOrderNotifyResult notifyResult = client.parseOrderNotifyResult(data.getBody());
        Assert.isTrue(Objects.equals(notifyResult.getResultCode(), "SUCCESS"), "支付结果非 SUCCESS");
        // 转换结果
        return PayOrderNotifyRespDTO
                .builder()
                .orderExtensionNo(notifyResult.getOutTradeNo())
                .channelOrderNo(notifyResult.getTransactionId())
                .channelUserId(notifyResult.getOpenid())
                .successTime(parseDateV2(notifyResult.getTimeEnd()))
                .build();
    }

    private PayOrderNotifyRespDTO parseOrderNotifyV3(PayNotifyReqDTO data) throws WxPayException {
        WxPayOrderNotifyV3Result notifyResult = client.parseOrderNotifyV3Result(data.getBody(), null);
        WxPayOrderNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();
        // 转换结果
        Assert.isTrue(Objects.equals(notifyResult.getResult().getTradeState(), "SUCCESS"),
                "支付结果非 SUCCESS");
        return PayOrderNotifyRespDTO.builder()
                .orderExtensionNo(result.getOutTradeNo())
                .channelOrderNo(result.getTradeState())
                .channelUserId(result.getPayer() != null ? result.getPayer().getOpenid() : null)
                .successTime(parseDateV3(result.getSuccessTime()))
                .build();
    }

    // ========== 各种工具方法 ==========

    static String getOpenid(PayOrderUnifiedReqDTO reqDTO) {
        String openid = MapUtil.getStr(reqDTO.getChannelExtras(), "openid");
        if (StrUtil.isEmpty(openid)) {
            throw new IllegalArgumentException("支付请求的 openid 不能为空！");
        }
        return openid;
    }

    static PayException buildPayException(WxPayException e) {
        return new PayException(ObjectUtils.defaultIfNull(e.getErrCode(), e.getReturnCode()),
                ObjectUtils.defaultIfNull(e.getErrCodeDes(), e.getCustomErrorMsg()));
    }

    static String formatDateV2(LocalDateTime time) {
        return TemporalAccessorUtil.format(time.atZone(ZoneId.systemDefault()), "yyyyMMddHHmmss");
    }

    static LocalDateTime parseDateV2(String time) {
        return LocalDateTimeUtil.parse(time, "yyyyMMddHHmmss");
    }

    static String formatDateV3(LocalDateTime time) {
        return TemporalAccessorUtil.format(time.atZone(ZoneId.systemDefault()), "yyyy-MM-dd'T'HH:mm:ssXXX");
    }

    static LocalDateTime parseDateV3(String time) {
        return LocalDateTimeUtil.parse(time, "yyyy-MM-dd'T'HH:mm:ssXXX");
    }

}
