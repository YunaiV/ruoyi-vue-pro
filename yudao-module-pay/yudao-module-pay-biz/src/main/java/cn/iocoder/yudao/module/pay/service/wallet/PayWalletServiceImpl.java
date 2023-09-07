package cn.iocoder.yudao.module.pay.service.wallet;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.member.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.TOO_MANY_REQUESTS;
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
    public PayWalletDO getOrCreatePayWallet(Long userId, Integer userType) {
        PayWalletDO payWalletDO = payWalletMapper.selectByUserIdAndType(userId, userType);
        if (payWalletDO == null) {
            payWalletDO = new PayWalletDO();
            payWalletDO.setUserId(userId);
            payWalletDO.setUserType(userType);
            payWalletDO.setBalance(0);
            payWalletDO.setTotalExpense(0L);
            payWalletDO.setTotalRecharge(0L);
            payWalletDO.setCreateTime(LocalDateTime.now());
            payWalletMapper.insert(payWalletDO);
        }
        return payWalletDO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO orderPay(Long userId, Integer userType, String outTradeNo, Integer price) {
        // 判断支付交易拓展单是否存
        PayOrderExtensionDO orderExtension = payOrderService.getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            throw exception(ORDER_EXTENSION_NOT_FOUND);
        }
        return reduceWalletBalance(userId, userType, orderExtension.getOrderId(), PAYMENT, price);
    }

    @Override
    public PayWalletTransactionDO reduceWalletBalance(Long userId, Integer userType,
                                                      Long bizId, PayWalletBizTypeEnum bizType, Integer price) {
        // 1.1 获取钱包
        PayWalletDO payWallet = getOrCreatePayWallet(userId, userType);
        // 1.2 判断余额是否足够
        int afterBalance = payWallet.getBalance() - price;
        if (afterBalance < 0) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }

        // 2.1 扣除余额
        int number = payWalletMapper.updateWhenDecBalance(bizType,payWallet.getBalance(), payWallet.getTotalRecharge(),
                payWallet.getTotalExpense(), price, payWallet.getId());
        if (number == 0) {
            throw exception(TOO_MANY_REQUESTS);
        }
        // 2.2 生成钱包流水
        String walletNo = generateWalletNo(bizType);
        PayWalletTransactionDO walletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setPrice(-price).setBalance(afterBalance)
                .setBizId(String.valueOf(bizId)).setBizType(bizType.getType()).setTitle(bizType.getDescription());
        payWalletTransactionService.createWalletTransaction(walletTransaction);
        return walletTransaction;
    }

    @Override
    public PayWalletTransactionDO addWalletBalance(Long userId, Integer userType, Long bizId,
                                                   PayWalletBizTypeEnum bizType, Integer price) {
        // 1.1 获取钱包
        PayWalletDO payWallet = getOrCreatePayWallet(userId, userType);

        // 2.1 增加余额
        int number = payWalletMapper.updateWhenIncBalance(bizType, payWallet.getBalance(), payWallet.getTotalRecharge(),
                payWallet.getTotalExpense(), price, payWallet.getId());
        if (number == 0) {
            throw exception(TOO_MANY_REQUESTS);
        }

        // 2.2 生成钱包流水
        String walletNo = generateWalletNo(bizType);
        PayWalletTransactionDO newWalletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setPrice(price).setBalance(payWallet.getBalance()+price)
                .setBizId(String.valueOf(bizId)).setBizType(bizType.getType())
                .setTitle(bizType.getDescription());
        payWalletTransactionService.createWalletTransaction(newWalletTransaction);
        return newWalletTransaction;
    }

    private String generateWalletNo(PayWalletBizTypeEnum bizType) {
        String no = "";
        switch(bizType){
            case PAYMENT :
                no = noRedisDAO.generate(WALLET_PAY_NO_PREFIX);
                break;
            case PAYMENT_REFUND :
                no = noRedisDAO.generate(WALLET_REFUND_NO_PREFIX);
                break;
            default :
                // TODO 待增加
        }
        return no;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO orderRefund(String outRefundNo, Integer refundPrice, String reason) {
        // 1.1 判断退款单是否存在
        PayRefundDO payRefund = payRefundService.getRefundByNo(outRefundNo);
        if (payRefund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        // 1.2 校验是否可以退款
        Long walletId =  validateWalletCanRefund(payRefund.getId(), payRefund.getChannelOrderNo(),  refundPrice);

        PayWalletDO payWallet = payWalletMapper.selectById(walletId);
        Assert.notNull(payWallet, "钱包 {} 不存在", walletId);
        return addWalletBalance(payWallet.getUserId(), payWallet.getUserType(),payRefund.getId(), PAYMENT_REFUND, refundPrice);
    }

    /**
     * 校验是否能退款
     *
     * @param refundId 支付退款单 id
     * @param walletPayNo 钱包支付 no
     */
    private Long validateWalletCanRefund(Long refundId, String walletPayNo, Integer refundPrice) {
        // 查询钱包支付交易
        PayWalletTransactionDO payWalletTransaction = payWalletTransactionService.getWalletTransactionByNo(walletPayNo);
        if (payWalletTransaction == null) {
            throw exception(WALLET_TRANSACTION_NOT_FOUND);
        }
        // 原来的支付金额
        int amount = - payWalletTransaction.getPrice();
        if (refundPrice != amount) {
            throw exception(WALLET_REFUND_AMOUNT_ERROR);
        }
        PayWalletTransactionDO refundTransaction = payWalletTransactionService.getWalletTransaction(
                String.valueOf(refundId), PAYMENT_REFUND);
        if (refundTransaction != null) {
            throw exception(WALLET_REFUND_EXIST);
        }
        return payWalletTransaction.getWalletId();
    }
}
