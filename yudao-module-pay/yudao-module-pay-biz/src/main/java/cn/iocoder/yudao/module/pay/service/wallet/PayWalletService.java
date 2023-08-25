package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;

/**
 * 支付钱包 Service 接口
 *
 * @author jason
 */
public interface PayWalletService {

    /**
     * 得到用户的支付钱包
     * @param userId 用户 id
     * @param userType 用户类型
     */
    PayWalletDO getPayWallet(Long userId, Integer userType);
}
