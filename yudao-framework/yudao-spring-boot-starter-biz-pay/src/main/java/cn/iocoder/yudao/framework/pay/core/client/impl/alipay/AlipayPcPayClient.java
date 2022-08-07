package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * 支付宝【PC网站支付】的 PayClient 实现类
 * 文档：https://opendocs.alipay.com/open/270/105898
 *
 * @author XGD
 */
@Slf4j
public class AlipayPcPayClient extends AbstractAlipayClient {

    public AlipayPcPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_PC.getCode(), config, new AlipayPayCodeMapping());
    }

    @Override
    public PayCommonResult<AlipayTradePagePayResponse> doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 构建 AlipayTradePagePayModel 请求
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        // 构建 AlipayTradePagePayRequest
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        JSONObject bizContent = new JSONObject();
        // 参数说明可查看: https://opendocs.alipay.com/open/028r8t?scene=22
        bizContent.put("out_trade_no", reqDTO.getMerchantOrderId());
        bizContent.put("total_amount", calculateAmount(reqDTO.getAmount()));
        bizContent.put("subject", reqDTO.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        // PC扫码支付的方式：支持前置模式和跳转模式。4: 订单码-可定义宽度的嵌入式二维码
        bizContent.put("qr_pay_mode", "4");
        // 自定义二维码宽度
        bizContent.put("qrcode_width", "150");
        request.setBizContent(bizContent.toJSONString());
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl("");
        // 执行请求
        AlipayTradePagePayResponse response;
        try {
            response = client.pageExecute(request);
        } catch (AlipayApiException e) {
            log.error("[unifiedOrder][request({}) 发起支付失败]", JsonUtils.toJsonString(reqDTO), e);
            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping);
        }
        // 响应为表单格式，前端可嵌入响应的页面或关闭当前支付窗口
        return PayCommonResult.build(StrUtil.blankToDefault(response.getCode(),"10000") ,response.getMsg(), response, codeMapping);
    }
}
