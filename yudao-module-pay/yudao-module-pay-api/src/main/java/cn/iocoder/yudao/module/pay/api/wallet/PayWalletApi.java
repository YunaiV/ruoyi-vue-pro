package cn.iocoder.yudao.module.pay.api.wallet;

import cn.iocoder.yudao.module.pay.api.wallet.dto.WalletSummaryRespDTO;

import java.time.LocalDateTime;

/**
 * 钱包 API 接口
 *
 * @author owen
 */
public interface PayWalletApi {

    /**
     * 获取钱包统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 钱包统计
     */
    WalletSummaryRespDTO getWalletSummary(LocalDateTime beginTime, LocalDateTime endTime);

}
