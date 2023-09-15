package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;

/**
 * 钱包充值 Service 接口
 *
 * @author jason
 */
public interface PayWalletRechargeService {

    /**
     * 创建钱包充值记录
     * @param userId 用户 id
     * @param userType 用户类型
     * @param vo 钱包充值请求 vo
     * @return 钱包充值记录
     */
    PayWalletRechargeDO createWalletRecharge(Long userId, Integer userType, AppPayWalletRechargeCreateReqVO vo);


    /**
     * 更新钱包充值成功
     * @param walletRechargeId 钱包充值 id
     * @param payOrderId 支付订单 id
     */
    void updateWalletRechargerPaid(Long walletRechargeId, Long payOrderId);
}
