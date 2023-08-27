package cn.iocoder.yudao.framework.pay.core.client.impl.delegate;

import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;

import java.util.Map;

/**
 * @author jason
 */
public abstract  class DelegatePayClient<Config extends PayClientConfig> extends AbstractPayClient<PayClientConfig> {

    private  final DelegatePayClient<Config> delegate;

    public DelegatePayClient(Long channelId, String channelCode, PayClientConfig config) {
        super(channelId, channelCode, config);
        delegate = this;
    }

    @Override
    protected void doInit() {
        delegate.doInit();
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)  {
        return delegate.doUnifiedOrder(reqDTO);
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo)  {
        return delegate.doGetOrder(outTradeNo);
    }

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO)  {
        return delegate.doUnifiedRefund(reqDTO);
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        return delegate.doGetRefund(outTradeNo, outRefundNo);
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String,String> params, String body)  {
        return delegate.doParseRefundNotify(params, body);
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String,String> params, String body)  {
        return delegate.doParseOrderNotify(params, body);
    }
}
