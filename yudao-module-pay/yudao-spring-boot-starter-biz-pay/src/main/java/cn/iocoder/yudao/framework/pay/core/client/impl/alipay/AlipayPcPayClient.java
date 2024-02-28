package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.Method;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 支付宝【PC 网站】的 PayClient 实现类
 *
 * 文档：<a href="https://opendocs.alipay.com/open/270/105898">电脑网站支付</a>
 *
 * @author XGD
 */
@Slf4j
public class AlipayPcPayClient extends AbstractAlipayPayClient {

    public AlipayPcPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_PC.getCode(), config);
    }

    @Override
    public PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 构建 AlipayTradePagePayModel 请求
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        // ① 通用的参数
        model.setOutTradeNo(reqDTO.getOutTradeNo());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(formatAmount(reqDTO.getPrice()));
        model.setTimeExpire(formatTime(reqDTO.getExpireTime()));
        model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 销售产品码. 目前 PC 支付场景下仅支持 FAST_INSTANT_TRADE_PAY
        // ② 个性化的参数
        // 如果想弄更多个性化的参数，可参考 https://www.pingxx.com/api/支付渠道 extra 参数说明.html 的 alipay_pc_direct 部分进行拓展
        model.setQrPayMode("2"); // 跳转模式 - 订单码，效果参见：https://help.pingxx.com/article/1137360/
        // ③ 支付宝 PC 支付有两种展示模式：FORM、URL
        String displayMode = ObjectUtil.defaultIfNull(reqDTO.getDisplayMode(),
                PayOrderDisplayModeEnum.URL.getMode());

        // 1.2 构建 AlipayTradePagePayRequest 请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());

        // 2.1 执行请求
        AlipayTradePagePayResponse response;
        if (Objects.equals(displayMode, PayOrderDisplayModeEnum.FORM.getMode())) {
            response = client.pageExecute(request, Method.POST.name()); // 需要特殊使用 POST 请求
        } else {
            response = client.pageExecute(request, Method.GET.name());
        }
        // 2.2 处理结果
        if (!response.isSuccess()) {
            return buildClosedPayOrderRespDTO(reqDTO, response);
        }
        return PayOrderRespDTO.waitingOf(displayMode, response.getBody(),
                reqDTO.getOutTradeNo(), response);
    }
}
