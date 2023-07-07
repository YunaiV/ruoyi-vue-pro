package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayDisplayModeEnum;
import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;

public class WxBarPayClient extends AbstractWxPayClient {

    public WxBarPayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_BAR.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.MICROPAY);
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrderV2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayMicropayRequest 对象
        WxPayMicropayRequest request = WxPayMicropayRequest.newBuilder()
                .outTradeNo(reqDTO.getMerchantOrderId())
                .body(reqDTO.getSubject())
                .detail(reqDTO.getBody())
                .totalFee(reqDTO.getAmount()) // 单位分
                .timeExpire(formatDateV2(reqDTO.getExpireTime()))
                .spbillCreateIp(reqDTO.getUserIp())
                .authCode(getAuthCode(reqDTO))
                .build();
        // 执行请求
        WxPayMicropayResult response = client.micropay(request);

        // 转换结果
        // TODO 芋艿：这里后面要看看
        return new PayOrderUnifiedRespDTO().setDisplayMode(PayDisplayModeEnum.CUSTOM.getMode())
                .setDisplayContent(JsonUtils.toJsonString(response));
    }

    @Override
    protected PayOrderUnifiedRespDTO doUnifiedOrderV3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        return doUnifiedOrderV2(reqDTO);
    }

    @Override
    protected PayRefundUnifiedRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        return null;
    }

    // ========== 各种工具方法 ==========
    static String getAuthCode(PayOrderUnifiedReqDTO reqDTO) {
        String authCode = MapUtil.getStr(reqDTO.getChannelExtras(), "authCode");
        if (StrUtil.isEmpty(authCode)) {
            throw new IllegalArgumentException("支付请求的 authCode 不能为空！");
        }
        return authCode;
    }

}
