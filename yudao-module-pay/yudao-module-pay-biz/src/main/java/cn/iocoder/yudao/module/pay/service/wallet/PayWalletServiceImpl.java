package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserType;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.pay.enums.member.WalletBizTypeEnum.PAYMENT;
import static cn.iocoder.yudao.module.pay.enums.member.WalletBizTypeEnum.PAYMENT_REFUND;

/**
 * 钱包 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletServiceImpl implements  PayWalletService {
    private static final String WALLET_PAY_NO_PREFIX = "WP";
    private static final String WALLET_REFUND_NO_PREFIX = "WR";
    @Resource
    private PayWalletMapper payWalletMapper;
    @Resource
    private PayWalletTransactionService payWalletTransactionService;
    @Resource
    private PayNoRedisDAO noRedisDAO;
    @Resource
    @Lazy
    private PayOrderService payOrderService;
    @Resource
    @Lazy
    private PayRefundService payRefundService;

    @Override
    public PayWalletDO getPayWallet(Long userId, Integer userType) {
        return payWalletMapper.selectByUserIdAndType(userId, userType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO pay(String outTradeNo, Integer price) {
        // 判断支付交易拓展单是否存在
        PayOrderExtensionDO orderExtension = payOrderService.getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            throw exception(ORDER_EXTENSION_NOT_FOUND);
        }
        PayWalletDO payWallet = validatePayWallet();
        // 判断余额是否足够
        int afterBalance = payWallet.getBalance() - price;
        if(afterBalance < 0){
            throw exception(WALLET_NOT_ENOUGH);
        }
        payWallet.setBalance(afterBalance);
        payWallet.setTotalExpense(payWallet.getTotalExpense() + price);
        payWalletMapper.updateById(payWallet);

        // 生成钱包渠道流水号
        String walletNo = noRedisDAO.generate(WALLET_PAY_NO_PREFIX);
        PayWalletTransactionDO payWalletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setAmount(price * -1).setBalance(afterBalance).setTransactionTime(LocalDateTime.now())
                .setBizId(orderExtension.getOrderId()).setBizType(PAYMENT.getBizType());
        payWalletTransactionService.addPayWalletTransaction(payWalletTransaction);

        return payWalletTransaction;
    }

    private PayWalletDO validatePayWallet() {
        Long userId = getLoginUserId();
        Integer userType = getLoginUserType();
        PayWalletDO payWallet = getPayWallet(userId, userType);
        if (payWallet == null) {
            log.error("[validatePayWallet] 用户 {} 钱包不存在", userId);
            throw exception(WALLET_NOT_FOUND);
        }
        return payWallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO refund(String outRefundNo, Integer refundPrice, String reason) {
        // 判断退款单是否存在
        PayRefundDO payRefund = payRefundService.getRefundByNo(outRefundNo);
        if (payRefund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        PayWalletDO payWallet = validatePayWallet();
        // 校验是否可以退款
        validateWalletCanRefund(payRefund.getId(), payRefund.getChannelOrderNo(), payWallet.getId(), refundPrice);

        Integer afterBalance = payWallet.getBalance() + refundPrice;
        payWallet.setBalance(afterBalance);
        payWallet.setTotalExpense(payWallet.getTotalExpense() + refundPrice * -1L);
        payWalletMapper.updateById(payWallet);

        String walletNo = noRedisDAO.generate(WALLET_REFUND_NO_PREFIX);
        PayWalletTransactionDO newWalletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setAmount(refundPrice).setBalance(afterBalance).setTransactionTime(LocalDateTime.now())
                .setBizId(payRefund.getId()).setBizType(PAYMENT_REFUND.getBizType())
                .setDescription(reason);
        payWalletTransactionService.addPayWalletTransaction(newWalletTransaction);

        return newWalletTransaction;
    }

    /**
     * 校验是否能退款
     * @param refundId 支付退款单 id
     * @param walletPayNo 钱包支付 no
     * @param walletId 钱包 id
     */
    private void validateWalletCanRefund(long refundId, String walletPayNo, long walletId, int refundPrice) {
        // 查询钱包支付交易
        PayWalletTransactionDO payWalletTransaction = payWalletTransactionService.getPayWalletTransactionByNo(walletPayNo);
        if (payWalletTransaction == null) {
            throw exception(WALLET_TRANSACTION_NOT_FOUND);
        }
        // 原来的支付金额
        int amount = payWalletTransaction.getAmount() * -1;
        if (refundPrice != amount) {
            throw exception(WALLET_REFUND_AMOUNT_ERROR);
        }
        PayWalletTransactionDO refundTransaction = payWalletTransactionService.getPayWalletTransaction(walletId, refundId, PAYMENT_REFUND);
        if (refundTransaction != null) {
            throw exception(WALLET_REFUND_EXIST);
        }
    }
}
