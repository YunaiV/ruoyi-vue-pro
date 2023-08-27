package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;

/**
 * 钱包 Service 接口
 *
 * @author jason
 */
public interface PayWalletService {

    /**
     * 获取钱包信息
     * @param userId 用户 id
     * @param userType 用户类型
     */
    PayWalletDO getPayWallet(Long userId, Integer userType);
}
