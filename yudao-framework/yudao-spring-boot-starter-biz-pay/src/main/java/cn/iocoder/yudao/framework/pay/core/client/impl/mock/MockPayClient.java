package cn.iocoder.yudao.framework.pay.core.client.impl.mock;

import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 模拟支付的 PayClient 实现类, 模拟支付返回结果都是成功
 *
 * @author jason
 */
public class MockPayClient extends AbstractPayClient<MockPayClientConfig> {

    private static final String MOCK_RESP_SUCCESS_DATA = "MOCK_SUCCESS";

    public MockPayClient(Long channelId, MockPayClientConfig config) {
        super(channelId, PayChannelEnum.MOCK.getCode(), config);
    }

    @Override
    protected void doInit() {

    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 模拟支付渠道订单号为空
        return PayOrderRespDTO.successOf("", "", LocalDateTime.now(), reqDTO.getOutTradeNo(), MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) {
        // 模拟支付渠道订单号为空
        return PayOrderRespDTO.successOf("", "", LocalDateTime.now(), outTradeNo, MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        // 模拟支付渠道退款单号为空
        return PayRefundRespDTO.successOf("", LocalDateTime.now(), reqDTO.getOutRefundNo(), MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        // 模拟支付渠道退款单号为空
        return PayRefundRespDTO.successOf("", LocalDateTime.now(), outRefundNo, MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body) {
        throw new UnsupportedOperationException("模拟支付无退款回调");
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body) {
        throw new UnsupportedOperationException("模拟支付无支付回调");
    }
}