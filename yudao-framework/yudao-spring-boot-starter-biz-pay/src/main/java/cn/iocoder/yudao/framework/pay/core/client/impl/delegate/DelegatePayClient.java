package cn.iocoder.yudao.framework.pay.core.client.impl.delegate;

import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;

import java.util.Map;

// TODO @jason：其它模块，主要是无法 pay client 初始化存在问题，所以我感觉，是不是可以搞个 PayClientInitializer 接口。这样，PayClientFactory 去 get 这个支付模式对应的 PayClientInitializer，通过它来创建。具体注入的地方，可以在 PayChannel init 方法那；
/**
 * 代理支付 Client 的抽象类。
 *
 * 用于支付 Client 由其它模块实现，例如钱包支付
 *
 * @author jason
 */
public abstract class DelegatePayClient<Config extends PayClientConfig> extends AbstractPayClient<PayClientConfig> {

    private final DelegatePayClient<Config> delegate;

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
