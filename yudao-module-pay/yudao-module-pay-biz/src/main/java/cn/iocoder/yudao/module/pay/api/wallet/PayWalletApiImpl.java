package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.service.wallet.PayWalletRechargeService;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 钱包 API 接口实现类
 *
 * @author owen
 */
@Service
@Validated
public class PayWalletApiImpl implements PayWalletApi {

    @Resource
    private PayWalletRechargeService payWalletRechargeService;
    @Resource
    private PayWalletTransactionService payWalletTransactionService;

}
