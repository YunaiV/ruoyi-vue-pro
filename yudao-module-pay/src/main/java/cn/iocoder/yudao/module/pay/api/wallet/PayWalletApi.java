package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;

/**
 * 钱包 API 接口
 *
 * @author liurulin
 */
public interface PayWalletApi {

    /**
     * 添加钱包余额
     *
     * @param reqDTO 增加余额请求
     */
    void addWalletBalance(PayWalletAddBalanceReqDTO reqDTO);

    /**
     * 获取钱包信息
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @return 钱包信息
     */
    PayWalletRespDTO getOrCreateWallet(Long userId, Integer userType);

}
