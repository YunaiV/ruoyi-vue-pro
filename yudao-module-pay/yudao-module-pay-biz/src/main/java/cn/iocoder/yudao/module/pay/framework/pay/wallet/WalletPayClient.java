package cn.iocoder.yudao.module.pay.framework.pay.wallet;

import cn.iocoder.yudao.framework.pay.core.client.impl.NonePayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.delegate.DelegatePayClient;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 钱包支付的 PayClient 实现类
 *
 * @author jason
 */
@Slf4j
public class WalletPayClient extends DelegatePayClient<NonePayClientConfig> {

    private PayWalletService payWalletService;

    public WalletPayClient(Long channelId, String channelCode, NonePayClientConfig config) {
        super(channelId, channelCode, config);
    }

    public WalletPayClient(Long channelId, String channelCode, NonePayClientConfig config, PayWalletService payWalletService) {
        this(channelId, channelCode, config);
        this.payWalletService = payWalletService;
    }

    @Override
    protected void doInit() {
        // 钱包支付 无需初始化
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        PayWalletTransactionDO payWalletTransaction = payWalletService.pay(reqDTO.getOutTradeNo(), reqDTO.getPrice());
        return PayOrderRespDTO.successOf(payWalletTransaction.getNo(), payWalletTransaction.getCreator(),
                payWalletTransaction.getTransactionTime(),
                reqDTO.getOutTradeNo(), "");
    }


    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body) {
        throw new UnsupportedOperationException("钱包支付无支付回调");
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) {
        return null;
    }

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        return null;
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body) {
        throw new UnsupportedOperationException("钱包支付无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        return null;
    }
}
