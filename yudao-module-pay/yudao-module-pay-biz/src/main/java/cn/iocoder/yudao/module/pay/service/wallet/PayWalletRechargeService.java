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
     * 创建钱包充值记录（发起充值）
     *
     * @param userId      用户 id
     * @param userType    用户类型
     * @param createReqVO 钱包充值请求 VO
     * @param userIp  用户Ip
     * @return 钱包充值记录
     */
    PayWalletRechargeDO createWalletRecharge(Long userId, Integer userType, String userIp,
                                             AppPayWalletRechargeCreateReqVO createReqVO);

    /**
     * 更新钱包充值成功
     *
     * @param id         钱包充值记录 id
     * @param payOrderId 支付订单 id
     */
    void updateWalletRechargerPaid(Long id, Long payOrderId);

    /**
     * 发起钱包充值退款
     *
     * @param id     钱包充值编号
     * @param userIp 用户 ip 地址
     */
    void refundWalletRecharge(Long id, String userIp);

    /**
     * 更新钱包充值记录为已退款
     *
     * @param id          钱包充值 id
     * @param payRefundId 退款单id
     */
    void updateWalletRechargeRefunded(Long id, Long payRefundId);

}
