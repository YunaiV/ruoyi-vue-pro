package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.WalletSummaryRespDTO;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletRechargeService;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    @Override
    public WalletSummaryRespDTO getWalletSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        WalletSummaryRespDTO walletSummary = payWalletRechargeService.getWalletSummary(beginTime, endTime);
        walletSummary.setOrderWalletPayPrice(payWalletTransactionService.getPriceSummary(PayWalletBizTypeEnum.PAYMENT, beginTime, endTime));
        return walletSummary;
    }

}
