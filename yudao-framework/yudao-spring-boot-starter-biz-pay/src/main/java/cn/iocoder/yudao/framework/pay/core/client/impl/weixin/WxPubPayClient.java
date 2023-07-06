package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayDisplayModeEnum;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信支付（公众号）的 PayClient 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class WxPubPayClient extends AbstractWxPayClient {

    public WxPubPayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_PUB.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.JSAPI);
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrderV2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = WxPayUnifiedOrderRequest.newBuilder()
                .outTradeNo(reqDTO.getMerchantOrderId())
                .body(reqDTO.getBody())
                .totalFee(reqDTO.getAmount()) // 单位分
                .timeExpire(formatDateV2(reqDTO.getExpireTime()))
                .spbillCreateIp(reqDTO.getUserIp())
                .openid(getOpenid(reqDTO))
                .notifyUrl(reqDTO.getNotifyUrl())
                .build();
        // 执行请求
        WxPayMpOrderResult response = client.createOrder(request);

        // 转换结果
        return new PayOrderUnifiedRespDTO().setDisplayMode(PayDisplayModeEnum.CUSTOM.getMode())
                .setDisplayContent(JsonUtils.toJsonString(response));
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrderV3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
        request.setOutTradeNo(reqDTO.getMerchantOrderId());
        request.setDescription(reqDTO.getBody());
        request.setAmount(new WxPayUnifiedOrderV3Request.Amount().setTotal(reqDTO.getAmount())); // 单位分
        request.setTimeExpire(formatDateV3(reqDTO.getExpireTime()));
        request.setPayer(new WxPayUnifiedOrderV3Request.Payer().setOpenid(getOpenid(reqDTO)));
        request.setSceneInfo(new WxPayUnifiedOrderV3Request.SceneInfo().setPayerClientIp(reqDTO.getUserIp()));
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        // 执行请求
        WxPayUnifiedOrderV3Result.JsapiResult response = client.createOrderV3(TradeTypeEnum.JSAPI, request);

        // 转换结果
        return new PayOrderUnifiedRespDTO().setDisplayMode(PayDisplayModeEnum.CUSTOM.getMode())
                .setDisplayContent(JsonUtils.toJsonString(response));
    }

    @Override
    protected PayRefundUnifiedRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        // TODO 需要实现
        throw new UnsupportedOperationException();
    }

}
