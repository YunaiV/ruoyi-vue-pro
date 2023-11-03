package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.wallet.PayWalletPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;

/**
 * 钱包 Service 接口
 *
 * @author jason
 */
public interface PayWalletService {

    /**
     * 获取钱包信息
     * <p>
     * 如果不存在，则创建钱包。由于用户注册时候不会创建钱包
     *
     * @param userId   用户编号
     * @param userType 用户类型
     */
    PayWalletDO getOrCreateWallet(Long userId, Integer userType);

    /**
     * 获取钱包信息
     *
     * @param walletId 钱包 id
     */
    PayWalletDO getWallet(Long walletId);


    /**
     * 获得会员钱包分页
     *
     * @param pageReqVO 分页查询
     * @return 会员钱包分页
     */
    PageResult<PayWalletDO> getWalletPage(Integer userType, PayWalletPageReqVO pageReqVO);

    /**
     * 钱包订单支付
     *
     * @param userId     用户 id
     * @param userType   用户类型
     * @param outTradeNo 外部订单号
     * @param price      金额
     */
    PayWalletTransactionDO orderPay(Long userId, Integer userType, String outTradeNo, Integer price);

    /**
     * 钱包订单支付退款
     *
     * @param outRefundNo 外部退款号
     * @param refundPrice 退款金额
     * @param reason      退款原因
     */
    PayWalletTransactionDO orderRefund(String outRefundNo, Integer refundPrice, String reason);

    /**
     * 扣减钱包余额
     *
     * @param walletId 钱包 id
     * @param bizId    业务关联 id
     * @param bizType  业务关联分类
     * @param price    扣减金额
     * @return 钱包流水
     */
    PayWalletTransactionDO reduceWalletBalance(Long walletId, Long bizId,
                                               PayWalletBizTypeEnum bizType, Integer price);

    /**
     * 增加钱包余额
     *
     * @param walletId 钱包 id
     * @param bizId    业务关联 id
     * @param bizType  业务关联分类
     * @param price    增加金额
     * @return 钱包流水
     */
    PayWalletTransactionDO addWalletBalance(Long walletId, String bizId,
                                            PayWalletBizTypeEnum bizType, Integer price);

    /**
     * 冻结钱包部分余额
     *
     * @param id    钱包编号
     * @param price 冻结金额
     */
    void freezePrice(Long id, Integer price);

    /**
     * 解冻钱包余额
     *
     * @param id    钱包编号
     * @param price 解冻金额
     */
    void unfreezePrice(Long id, Integer price);

}
