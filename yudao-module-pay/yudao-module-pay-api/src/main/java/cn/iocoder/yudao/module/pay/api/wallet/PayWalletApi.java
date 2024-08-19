package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletUpdateBalanceReqDTO;

// TODO @puhui999：不在 MemberUserController 提供接口，而是 PayWalletController 增加。不然 member 耦合 pay 拉。
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
