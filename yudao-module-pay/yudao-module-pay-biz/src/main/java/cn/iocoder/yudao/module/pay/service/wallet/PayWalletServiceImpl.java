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

    /**
     * 余额支付的 no 前缀
     */
    private static final String WALLET_PAY_NO_PREFIX = "WP";
    /**
     * 余额退款的 no 前缀
     */
    private static final String WALLET_REFUND_NO_PREFIX = "WR";

    @Resource
    private PayWalletMapper payWalletMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Resource
    private PayWalletTransactionService payWalletTransactionService;
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

    // TODO @jason：可以做的更抽象一点；pay(bizType, bizId, price)；reduceWalletBalance；
    // TODO @jason：最好是，明确传入哪个 userId 或者 walletId；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO pay(String outTradeNo, Integer price) {
        // 1.1 判断支付交易拓展单是否存
        PayOrderExtensionDO orderExtension = payOrderService.getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            throw exception(ORDER_EXTENSION_NOT_FOUND);
        }
        // 1.2 判断余额是否足够
        PayWalletDO payWallet = validatePayWallet();
        int afterBalance = payWallet.getBalance() - price;
        if (afterBalance < 0) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }

        // 2.1 扣除余额
        // TODO @jason：不要直接整个更新；而是 new 一个出来更新；然后要考虑并发，要 where 余额 > price，以及 - price
        payWallet.setBalance(afterBalance);
        payWallet.setTotalExpense(payWallet.getTotalExpense() + price);
        payWalletMapper.updateById(payWallet);

        // 2.2 生成钱包流水
        String walletNo = noRedisDAO.generate(WALLET_PAY_NO_PREFIX);
        PayWalletTransactionDO walletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setAmount(price * -1).setBalance(afterBalance).setTransactionTime(LocalDateTime.now())
                .setBizId(orderExtension.getOrderId()).setBizType(PAYMENT.getType());
        payWalletTransactionService.createWalletTransaction(walletTransaction);
        return walletTransaction;
    }

    // TODO @jason：不要在 service 里去使用用户上下文，这样和 request 就耦合了。
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

    // TODO @jason：可以做的更抽象一点；pay(bizType, bizId, price)；addWalletBalance；这样，如果后续充值，应该也是能复用这个方法的；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO refund(String outRefundNo, Integer refundPrice, String reason) {
        // 1.1 判断退款单是否存在
        PayRefundDO payRefund = payRefundService.getRefundByNo(outRefundNo);
        if (payRefund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        // 1.2 校验是否可以退款
        PayWalletDO payWallet = validatePayWallet();
        validateWalletCanRefund(payRefund.getId(), payRefund.getChannelOrderNo(), payWallet.getId(), refundPrice);

        // TODO @jason：不要直接整个更新；而是 new 一个出来更新；然后要考虑并发，要 where 余额 + 金额
        Integer afterBalance = payWallet.getBalance() + refundPrice;
        payWallet.setBalance(afterBalance);
        payWallet.setTotalExpense(payWallet.getTotalExpense() + refundPrice * -1L);
        payWalletMapper.updateById(payWallet);

        // 2.2 生成钱包流水
        String walletNo = noRedisDAO.generate(WALLET_REFUND_NO_PREFIX);
        PayWalletTransactionDO newWalletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setAmount(refundPrice).setBalance(afterBalance).setTransactionTime(LocalDateTime.now())
                .setBizId(payRefund.getId()).setBizType(PAYMENT_REFUND.getType())
                .setDescription(reason);
        payWalletTransactionService.createWalletTransaction(newWalletTransaction);
        return newWalletTransaction;
    }

    /**
     * 校验是否能退款
     *
     * @param refundId 支付退款单 id
     * @param walletPayNo 钱包支付 no
     * @param walletId 钱包 id
     */
    // TODO @jason：不要使用基本类型；
    private void validateWalletCanRefund(long refundId, String walletPayNo, long walletId, int refundPrice) {
        // 查询钱包支付交易
        PayWalletTransactionDO payWalletTransaction = payWalletTransactionService.getWalletTransactionByNo(walletPayNo);
        if (payWalletTransaction == null) {
            throw exception(WALLET_TRANSACTION_NOT_FOUND);
        }
        // 原来的支付金额
        int amount = payWalletTransaction.getAmount() * -1; // TODO @jason：直接 - payWalletTransaction.getAmount() 即可；
        if (refundPrice != amount) {
            throw exception(WALLET_REFUND_AMOUNT_ERROR);
        }
        PayWalletTransactionDO refundTransaction = payWalletTransactionService.getWalletTransaction(walletId, refundId, PAYMENT_REFUND);
        if (refundTransaction != null) {
            throw exception(WALLET_REFUND_EXIST);
        }
    }

}
