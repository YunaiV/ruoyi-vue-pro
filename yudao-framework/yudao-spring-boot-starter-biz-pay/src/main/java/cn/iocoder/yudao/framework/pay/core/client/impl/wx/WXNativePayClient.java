package cn.iocoder.yudao.framework.pay.core.client.impl.wx;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.*;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXCodeMapping.CODE_SUCCESS;
import static cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXCodeMapping.MESSAGE_SUCCESS;

/**
 * @Description:  Native支付
 * @author: zwy
 * @date: 2022年04月18日 17:00
 */
@Slf4j
public class WXNativePayClient extends AbstractPayClient<WXPayClientConfig> {
    private WxPayService client;

    public WXNativePayClient(Long channelId, WXPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_NATIVE.getCode(), config, new WXCodeMapping());
    }

    @Override
    protected void doInit() {
        WxPayConfig payConfig = new WxPayConfig();
        BeanUtil.copyProperties(config, payConfig, "keyContent");
        payConfig.setTradeType(WxPayConstants.TradeType.NATIVE); // 设置使用 native 支付方式
//        if (StrUtil.isNotEmpty(config.getKeyContent())) {
//            payConfig.setKeyContent(config.getKeyContent().getBytes(StandardCharsets.UTF_8));
//        }
        if (StrUtil.isNotEmpty(config.getPrivateKeyContent())) {
            // weixin-pay-java 存在 BUG，无法直接设置内容，所以创建临时文件来解决
            payConfig.setPrivateKeyPath(FileUtils.createTempFile(config.getPrivateKeyContent()).getPath());
        }
        if (StrUtil.isNotEmpty(config.getPrivateCertContent())) {
            // weixin-pay-java 存在 BUG，无法直接设置内容，所以创建临时文件来解决
            payConfig.setPrivateCertPath(FileUtils.createTempFile(config.getPrivateCertContent()).getPath());
        }
        // 真实客户端
        this.client = new WxPayServiceImpl();
        client.setConfig(payConfig);
    }

    @Override
    public PayCommonResult<String> doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 这里原生的返回的是支付的 url 所以直接使用string接收
        //"invokeResponse": "weixin://wxpay/bizpayurl?pr=EGYAem7zz"
        String responseV3;
        try {
            switch (config.getApiVersion()) {
                case WXPayClientConfig.API_VERSION_V2:
                    responseV3 = unifiedOrderV2(reqDTO).getCodeUrl();
                    break;
                case WXPayClientConfig.API_VERSION_V3:
                  responseV3 = this.unifiedOrderV3(reqDTO);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            log.error("[unifiedOrder][request({}) 发起支付失败，原因({})]", toJsonString(reqDTO), e);
            return PayCommonResult.build(ObjectUtils.defaultIfNull(e.getErrCode(), e.getReturnCode(), "CustomErrorCode"),
                    ObjectUtils.defaultIfNull(e.getErrCodeDes(), e.getCustomErrorMsg()), null, codeMapping);
        }
        return PayCommonResult.build(CODE_SUCCESS, MESSAGE_SUCCESS, responseV3, codeMapping);
    }

    private WxPayNativeOrderResult unifiedOrderV2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        //前端
        String trade_type = reqDTO.getChannelExtras().get("trade_type");
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = WxPayUnifiedOrderRequest
                .newBuilder()
                .outTradeNo(reqDTO.getMerchantOrderId())
                .body(reqDTO.getBody())
                .totalFee(reqDTO.getAmount().intValue()) // 单位分
//                .timeExpire(DateUtil.format(reqDTO.getExpireTime(), "yyyyMMddHHmmss"))
                .spbillCreateIp(reqDTO.getUserIp())
                .notifyUrl(reqDTO.getNotifyUrl())
                .productId(trade_type)
                .build();
        // 执行请求
        return client.createOrder(request);
    }

    private String unifiedOrderV3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
        request.setOutTradeNo(reqDTO.getMerchantOrderId());
        request.setDescription(reqDTO.getBody());
        request.setAmount(new WxPayUnifiedOrderV3Request.Amount().setTotal(reqDTO.getAmount().intValue())); // 单位分
        request.setSceneInfo(new WxPayUnifiedOrderV3Request.SceneInfo().setPayerClientIp(reqDTO.getUserIp()));
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        // 执行请求
//        log.info("支付字段request:{}",request.getTimeExpire());

        return client.createOrderV3(TradeTypeEnum.NATIVE, request);
    }


    @Override
    public PayOrderNotifyRespDTO parseOrderNotify(PayNotifyDataDTO data) throws WxPayException {
        WxPayOrderNotifyResult notifyResult = client.parseOrderNotifyResult(data.getBody());
        Assert.isTrue(Objects.equals(notifyResult.getResultCode(), "SUCCESS"), "支付结果非 SUCCESS");
        // 转换结果
        return PayOrderNotifyRespDTO.builder().orderExtensionNo(notifyResult.getOutTradeNo())
                .channelOrderNo(notifyResult.getTransactionId()).channelUserId(notifyResult.getOpenid())
                .successTime(DateUtil.parse(notifyResult.getTimeEnd(), "yyyyMMddHHmmss"))
                .data(data.getBody()).build();
    }

    @Override
    public PayRefundNotifyDTO parseRefundNotify(PayNotifyDataDTO notifyData) {
        //TODO 需要实现
        throw new UnsupportedOperationException("需要实现");
    }


    @Override
    protected PayCommonResult<PayRefundUnifiedRespDTO> doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        //TODO 需要实现
        throw new UnsupportedOperationException();
    }
}
