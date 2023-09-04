package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;

/**
 * 钱包 Service 接口
 *
 * @author jason
 */
public interface PayWalletService {

    // TODO @jason：改成 getOrCreateWallet；因为目前解耦，用户注册时，不会创建钱包；需要这里兜底处理；
    /**
     * 获取钱包信息
     *
     * @param userId 用户编号
     * @param userType 用户类型
     */
    PayWalletDO getPayWallet(Long userId, Integer userType);

    /**
     * 钱包订单支付
     *
     * @param outTradeNo 外部订单号
     * @param price 金额
     */
    PayWalletTransactionDO pay(Long userId, Integer userType, String outTradeNo, Integer price);


    /**
     * 扣减钱包余额
     *
     * @param userId  用户 id
     * @param userType 用户类型
     * @param bizId 业务关联 id
     * @param bizType 业务关联分类
     * @param price 扣减金额
     * @return 钱包流水
     */
    PayWalletTransactionDO reduceWalletBalance(Long userId, Integer userType,
                                               Long bizId, PayWalletBizTypeEnum bizType, Integer price);


    /**
     * 增加钱包余额
     *
     * @param userId 用户 id
     * @param userType 用户类型
     * @param bizId 业务关联 id
     * @param bizType 业务关联分类
     * @param price 增加金额
     * @return 钱包流水
     */
    PayWalletTransactionDO addWalletBalance(Long userId, Integer userType,
                                            Long bizId, PayWalletBizTypeEnum bizType, Integer price);

    /**
     * 钱包订单支付退款
     *
     * @param outRefundNo 外部退款号
     * @param refundPrice 退款金额
     * @param reason  退款原因
     */
    PayWalletTransactionDO refund(String outRefundNo, Integer refundPrice, String reason);

}
