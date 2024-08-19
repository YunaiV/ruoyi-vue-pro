package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletUpdateBalanceReqDTO;

/**
 * 会员钱包 API 接口
 *
 * @author HUIHUI
 */
public interface PayWalletApi {

    /**
     * 更新钱包余额
     *
     * @param reqDTO 请求
     */
    void updateBalance(PayWalletUpdateBalanceReqDTO reqDTO);

}
