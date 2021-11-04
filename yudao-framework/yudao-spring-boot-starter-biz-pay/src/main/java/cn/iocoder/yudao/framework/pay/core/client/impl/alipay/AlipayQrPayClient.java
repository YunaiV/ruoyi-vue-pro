package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.NotifyDataDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 支付宝【扫码支付】的 PayClient 实现类
 * 文档：https://opendocs.alipay.com/apis/02890k
 *
 * @author 芋道源码
 */
@Slf4j
public class AlipayQrPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    private DefaultAlipayClient client;

    public AlipayQrPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_QR.getCode(), config, new AlipayPayCodeMapping());
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        // 真实客户端
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    @Override
    public PayCommonResult<AlipayTradePrecreateResponse> doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 构建 AlipayTradePrecreateModel 请求
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(reqDTO.getMerchantOrderId());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(calculateAmount(reqDTO.getAmount()).toString()); // 单位：元
        // TODO 芋艿：userIp + expireTime
        // 构建 AlipayTradePrecreateRequest
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());

        // 执行请求
        AlipayTradePrecreateResponse response;
        try {
            response = client.execute(request);
        } catch (AlipayApiException e) {
            log.error("[unifiedOrder][request({}) 发起支付失败]", toJsonString(reqDTO), e);
            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping);
        }
        // TODO 芋艿：sub Code 需要测试下各种失败的情况
        return PayCommonResult.build(response.getCode(), response.getMsg(), response, codeMapping);
    }

    @Override
    public PayOrderNotifyRespDTO parseOrderNotify(NotifyDataDTO data) throws Exception {
        // TODO 芋艿：待完成
        return null;
    }
}
