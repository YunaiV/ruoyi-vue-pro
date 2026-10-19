package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig.MODE_CERTIFICATE;

/**
 * 支付宝【小程序】的 PayClient 实现类
 *
 * 文档：<a href="https://opendocs.alipay.com/mini/6039ed0c_alipay.trade.create">统一收单交易创建</a>
 *
 * @author graypxl
 */
public class AlipayLitePayClient extends AbstractAlipayPayClient {

    public static final String BUYER_ID_KEY = "buyer_id";
    public static final String BUYER_OPEN_ID_KEY = "buyer_open_id";

    public AlipayLitePayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_LITE.getCode(), config);
    }

    @Override
    public PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws AlipayApiException {
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(formatAmount(reqDTO.getPrice()));
        model.setTimeExpire(formatTime(reqDTO.getExpireTime()));
        model.setProductCode("JSAPI_PAY");
        model.setOpAppId(config.getAppId());
        applyBuyerIdentity(model, reqDTO);

        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());

        AlipayTradeCreateResponse response;
        if (Objects.equals(config.getMode(), MODE_CERTIFICATE)) {
            response = client.certificateExecute(request);
        } else {
            response = client.execute(request);
        }
        if (!response.isSuccess()) {
            return buildClosedPayOrderRespDTO(reqDTO, response);
        }
        return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), response.getTradeNo(),
                reqDTO.getOutTradeNo(), response);
    }

    static void applyBuyerIdentity(AlipayTradeCreateModel model, PayOrderUnifiedReqDTO reqDTO) {
        String buyerId = MapUtil.getStr(reqDTO.getChannelExtras(), BUYER_ID_KEY);
        String buyerOpenId = MapUtil.getStr(reqDTO.getChannelExtras(), BUYER_OPEN_ID_KEY);
        if (StrUtil.isBlank(buyerOpenId)) {
            buyerOpenId = MapUtil.getStr(reqDTO.getChannelExtras(), "openid");
        }
        if (isAlipayUserId(buyerId)) {
            model.setBuyerId(buyerId);
        } else if (StrUtil.isNotBlank(buyerOpenId)) {
            model.setBuyerOpenId(buyerOpenId);
        } else if (StrUtil.isNotBlank(buyerId)) {
            model.setBuyerOpenId(buyerId);
        } else {
            throw invalidParamException("支付请求的 buyer_open_id 不能为空！");
        }
    }

    static boolean isAlipayUserId(String value) {
        return StrUtil.isNotBlank(value) && value.startsWith("2088") && value.length() <= 28
                && StrUtil.isNumeric(value);
    }

}
