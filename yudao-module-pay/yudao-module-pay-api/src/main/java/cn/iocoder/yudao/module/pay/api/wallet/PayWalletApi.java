package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletCreateReqDto;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;

/**
 * 钱包 API 接口
 *
 * @author liurulin
 */
public interface PayWalletApi {

    /**
     * 添加钱包
     * @param reqDTO 创建请求
     */
    void addWallet(PayWalletCreateReqDto reqDTO);

    /**
     * 根据用户id获取钱包信息
     * @param userId 用户id
     * @return 钱包信息
     */
    PayWalletRespDTO getWalletByUserId(Long userId);
}
