package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信支付【Native 二维码】的 PayClient 实现类
 *
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml">Native 下单</a>
 *
 * @author zwy
 */
@Slf4j
public class WxNativePayClient extends AbstractWxPayClient {

    public WxNativePayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_NATIVE.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.NATIVE);
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrderV2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = WxPayUnifiedOrderRequest.newBuilder()
                .outTradeNo(reqDTO.getOutTradeNo())
                .body(reqDTO.getSubject())
                .detail(reqDTO.getBody())
                .totalFee(reqDTO.getPrice()) // 单位分
                .productId(reqDTO.getOutTradeNo())
                .timeExpire(formatDateV2(reqDTO.getExpireTime()))
                .spbillCreateIp(reqDTO.getUserIp())
                .notifyUrl(reqDTO.getNotifyUrl())
                .build();
        // 执行请求
        WxPayNativeOrderResult response = client.createOrder(request);

        // 转换结果
        return new PayOrderUnifiedRespDTO(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                response.getCodeUrl());
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrderV3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request()
                .setOutTradeNo(reqDTO.getOutTradeNo())
                .setDescription(reqDTO.getSubject())
                .setAmount(new WxPayUnifiedOrderV3Request.Amount().setTotal(reqDTO.getPrice())) // 单位分
                .setTimeExpire(formatDateV3(reqDTO.getExpireTime()))
                .setSceneInfo(new WxPayUnifiedOrderV3Request.SceneInfo().setPayerClientIp(reqDTO.getUserIp()))
                .setNotifyUrl(reqDTO.getNotifyUrl());
        // 执行请求
        String response = client.createOrderV3(TradeTypeEnum.NATIVE, request);

        // 转换结果
        return new PayOrderUnifiedRespDTO(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                response);
    }

}
