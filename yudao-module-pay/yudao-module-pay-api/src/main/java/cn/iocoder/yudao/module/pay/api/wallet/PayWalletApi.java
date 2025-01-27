package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;

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

}
