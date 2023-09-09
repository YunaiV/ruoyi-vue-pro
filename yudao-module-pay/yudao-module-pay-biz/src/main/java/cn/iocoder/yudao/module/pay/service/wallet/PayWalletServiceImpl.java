package cn.iocoder.yudao.module.pay.service.wallet;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.pay.service.wallet.bo.CreateWalletTransactionBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum.PAYMENT;
import static cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum.PAYMENT_REFUND;

/**
 * 钱包 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletServiceImpl implements  PayWalletService {

    @Resource
    private PayWalletMapper walletMapper;
    @Resource
    private PayWalletTransactionService walletTransactionService;
    @Resource
    @Lazy
    private PayOrderService orderService;
    @Resource
    @Lazy
    private PayRefundService refundService;

    @Override
    public PayWalletDO getOrCreateWallet(Long userId, Integer userType) {
        PayWalletDO wallet = walletMapper.selectByUserIdAndType(userId, userType);
        if (wallet == null) {
            wallet = new PayWalletDO().setUserId(userId).setUserType(userType)
                    .setBalance(0).setTotalExpense(0).setTotalRecharge(0);
            wallet.setCreateTime(LocalDateTime.now());
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO orderPay(Long userId, Integer userType, String outTradeNo, Integer price) {
        // 1. 判断支付交易拓展单是否存
        PayOrderExtensionDO orderExtension = orderService.getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            throw exception(ORDER_EXTENSION_NOT_FOUND);
        }
        // 2. 扣减余额
        return reduceWalletBalance(userId, userType, orderExtension.getOrderId(), PAYMENT, price);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO orderRefund(String outRefundNo, Integer refundPrice, String reason) {
        // 1.1 判断退款单是否存在
        PayRefundDO payRefund = refundService.getRefundByNo(outRefundNo);
        if (payRefund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        // 1.2 校验是否可以退款
        Long walletId = validateWalletCanRefund(payRefund.getId(), payRefund.getChannelOrderNo(),  refundPrice);
        PayWalletDO wallet = walletMapper.selectById(walletId);
        Assert.notNull(wallet, "钱包 {} 不存在", walletId);
        // 2. 增加余额
        return addWalletBalance(wallet.getUserId(), wallet.getUserType(), payRefund.getId(), PAYMENT_REFUND, refundPrice);
    }

    /**
     * 校验是否能退款
     *
     * @param refundId 支付退款单 id
     * @param walletPayNo 钱包支付 no
     */
    private Long validateWalletCanRefund(Long refundId, String walletPayNo, Integer refundPrice) {
        // 1. 校验钱包支付交易存在
        PayWalletTransactionDO walletTransaction = walletTransactionService.getWalletTransactionByNo(walletPayNo);
        if (walletTransaction == null) {
            throw exception(WALLET_TRANSACTION_NOT_FOUND);
        }
        // 原来的支付金额
        // TODO @jason：应该允许多次退款哈；
        int amount = - walletTransaction.getPrice();
        if (refundPrice != amount) {
            throw exception(WALLET_REFUND_AMOUNT_ERROR);
        }
        PayWalletTransactionDO refundTransaction = walletTransactionService.getWalletTransaction(
                String.valueOf(refundId), PAYMENT_REFUND);
        if (refundTransaction != null) {
            throw exception(WALLET_REFUND_EXIST);
        }
        return walletTransaction.getWalletId();
    }

    @Override
    public PayWalletTransactionDO reduceWalletBalance(Long userId, Integer userType,
                                                      Long bizId, PayWalletBizTypeEnum bizType, Integer price) {
        // 1.1 获取钱包
        PayWalletDO payWallet = getOrCreateWallet(userId, userType);
        // 2.1 扣除余额
        int number = 0 ;
        switch (bizType) {
            case PAYMENT: {
                number = walletMapper.updateWhenConsumption(price, payWallet.getId());
                break;
            }
            case RECHARGE_REFUND: {
                // TODO
                break;
            }
        }
        if (number == 0) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }
        int afterBalance = payWallet.getBalance() - price;
        // 2.2 生成钱包流水
        CreateWalletTransactionBO bo = new CreateWalletTransactionBO().setWalletId(payWallet.getId())
                .setPrice(-price).setBalance(afterBalance).setBizId(String.valueOf(bizId))
                .setBizType(bizType.getType()).setTitle(bizType.getDescription());
        return walletTransactionService.createWalletTransaction(bo);
    }

    @Override
    public PayWalletTransactionDO addWalletBalance(Long userId, Integer userType,
                                                   Long bizId, PayWalletBizTypeEnum bizType, Integer price) {
        // 获取钱包
        PayWalletDO payWallet = getOrCreateWallet(userId, userType);
        switch (bizType) {
            case PAYMENT_REFUND: {
                // 更新退款
                walletMapper.updateWhenConsumptionRefund(price, payWallet.getId());
                break;
            }
            case RECHARGE: {
                //TODO
                break;
            }
        }
        // 2.2 生成钱包流水
        CreateWalletTransactionBO bo = new CreateWalletTransactionBO().setWalletId(payWallet.getId())
                .setPrice(price).setBalance(payWallet.getBalance()+price).setBizId(String.valueOf(bizId))
                .setBizType(bizType.getType()).setTitle(bizType.getDescription());
        return walletTransactionService.createWalletTransaction(bo);
    }

}
