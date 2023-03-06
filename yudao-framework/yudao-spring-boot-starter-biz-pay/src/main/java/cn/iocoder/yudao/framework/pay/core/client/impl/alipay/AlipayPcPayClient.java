package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Method;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayDisplayModeEnum;
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
public class AlipayPcPayClient extends AbstractAlipayClient {

    public AlipayPcPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_PC.getCode(), config);
    }

    @Override
    public PayOrderUnifiedRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 构建 AlipayTradePagePayModel 请求
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        // ① 通用的参数
        model.setOutTradeNo(reqDTO.getMerchantOrderId());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(formatAmount(reqDTO.getAmount()));
        model.setTimeExpire(formatTime(reqDTO.getExpireTime()));
        model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 销售产品码. 目前 PC 支付场景下仅支持 FAST_INSTANT_TRADE_PAY
        // ② 个性化的参数
        // 参考 https://www.pingxx.com/api/支付渠道 extra 参数说明.html 的 alipay_pc_direct 部分
        model.setQrPayMode(MapUtil.getStr(reqDTO.getChannelExtras(), "qr_pay_mode"));
        model.setQrcodeWidth(MapUtil.getLong(reqDTO.getChannelExtras(), "qr_code_width"));
        // ③ 支付宝 PC 支付有多种展示模式，因此这里需要计算
        String displayMode = getDisplayMode(reqDTO.getDisplayMode(), model.getQrPayMode());

        // 1.2 构建 AlipayTradePagePayRequest 请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());

        // 2.1 执行请求
        AlipayTradePagePayResponse response;
        if (Objects.equals(displayMode, PayDisplayModeEnum.FORM.getMode())) {
            response = client.pageExecute(request, Method.POST.name()); // 需要特殊使用 POST 请求
        } else {
            response = client.pageExecute(request, Method.GET.name());
        }

        // 2.2 处理结果
        validateSuccess(response);
        return new PayOrderUnifiedRespDTO().setDisplayMode(displayMode)
                .setDisplayContent(response.getBody());
    }

    /**
     * 获得最终的支付 UI 展示模式
     *
     * @param displayMode 前端传递的 UI 展示模式
     * @param qrPayMode 前端传递的二维码模式
     * @return 最终的支付 UI 展示模式
     */
    private String getDisplayMode(String displayMode, String qrPayMode) {
        // 1.1 支付宝二维码的前置模式
        if (StrUtil.equalsAny(qrPayMode, "0", "1", "3", "4")) {
            return PayDisplayModeEnum.IFRAME.getMode();
        }
        // 1.2 支付宝二维码的跳转模式
        if (StrUtil.equals(qrPayMode, "2")) {
            return PayDisplayModeEnum.URL.getMode();
        }
        // 2. 前端传递了 UI 展示模式
        return displayMode != null ? displayMode :
                PayDisplayModeEnum.URL.getMode(); // 模式使用 URL 跳转
    }

}
