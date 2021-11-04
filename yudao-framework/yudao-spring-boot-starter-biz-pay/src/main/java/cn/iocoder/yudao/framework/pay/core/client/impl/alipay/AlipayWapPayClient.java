package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayNotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.Objects;

/**
 * 支付宝【手机网站】的 PayClient 实现类
 * 文档：https://opendocs.alipay.com/apis/api_1/alipay.trade.wap.pay
 *
 * @author 芋道源码
 */
public class AlipayWapPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    private DefaultAlipayClient client;

    public AlipayWapPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_WAP.getCode(), config, new AlipayPayCodeMapping());
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    @Override
    public PayCommonResult<AlipayTradeWapPayResponse> doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 构建 AlipayTradeWapPayModel 请求
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(reqDTO.getMerchantOrderId());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(calculateAmount(reqDTO.getAmount()).toString());
        model.setProductCode("QUICK_WAP_PAY"); // TODO 芋艿：这里咋整
        //TODO 芋艿：这里咋整  jason @芋艿 可以去掉吧,
        // TODO @jason: 这个支付方式，需要有 sellerId 么？
        //model.setSellerId("2088102147948060");
        model.setTimeExpire(DateUtil.format(reqDTO.getExpireTime(),"yyyy-MM-dd HH:mm:ss"));
        // TODO 芋艿：userIp
        // 构建 AlipayTradeWapPayRequest
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());
        // 执行请求
        AlipayTradeWapPayResponse response;
        try {
            response = client.pageExecute(request);
        } catch (AlipayApiException e) {
            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping);
        }

        // TODO 芋艿：sub Code
        if(response.isSuccess() && Objects.isNull(response.getCode()) && Objects.nonNull(response.getBody())){
            //成功alipay wap 成功 code 为 null , body 为form 表单
            return PayCommonResult.build("-9999", "Success", response, codeMapping);
        }else {
            return PayCommonResult.build(response.getCode(), response.getMsg(), response, codeMapping);
        }
    }

    // TODO @jason: 注释记得补下哈
    /**
     *  //https://opendocs.alipay.com/open/203/105286
     * @param data 通知结果
     * @return
     * @throws Exception
     */
    @Override
    public PayOrderNotifyRespDTO parseOrderNotify(PayNotifyDataDTO data) throws Exception {
        Map<String, String> params = data.getParams();
        return  PayOrderNotifyRespDTO.builder().orderExtensionNo(params.get("out_trade_no"))
                .channelOrderNo(params.get("trade_no")).channelUserId(params.get("seller_id"))
                .tradeStatus(params.get("trade_status"))
                .successTime(DateUtil.parse(params.get("notify_time"), "yyyy-MM-dd HH:mm:ss"))
                .data(data.getBody()).build();
    }

}
