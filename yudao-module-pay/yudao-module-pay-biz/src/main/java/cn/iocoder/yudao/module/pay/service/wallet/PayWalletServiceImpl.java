package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
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

/**
 * 钱包 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletServiceImpl implements  PayWalletService {
    private static final String WALLET_CHANNEL_NO_PREFIX = "W";
    @Resource
    @Lazy
    private PayOrderService payOrderService;
    @Resource
    private PayWalletMapper payWalletMapper;
    @Resource
    private PayWalletTransactionService payWalletTransactionService;
    @Resource
    private PayNoRedisDAO noRedisDAO;

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
        Long userId = getLoginUserId();
        Integer userType = getLoginUserType();
        PayWalletDO payWallet = getPayWallet(userId, userType);
        if (payWallet == null) {
            log.error("[pay] 用户 {} 钱包不存在", userId);
            throw exception(WALLET_NOT_FOUND);
        }
        // 判断余额是否足够
        int afterBalance = payWallet.getBalance() - price;
        if(afterBalance < 0){
            throw exception(WALLET_NOT_ENOUGH);
        }
        payWallet.setBalance(afterBalance);
        payWallet.setTotalExpense(payWallet.getTotalExpense() + price);
        payWalletMapper.updateById(payWallet);

        // 生成钱包渠道流水号
        String walletNo = noRedisDAO.generate(WALLET_CHANNEL_NO_PREFIX);
        PayWalletTransactionDO payWalletTransaction = new PayWalletTransactionDO().setWalletId(payWallet.getId())
                .setNo(walletNo).setAmount(price).setBalance(afterBalance).setTransactionTime(LocalDateTime.now())
                .setBizId(orderExtension.getOrderId()).setBizType(PAYMENT.getBizType());
        payWalletTransactionService.addPayWalletTransaction(payWalletTransaction);

        return payWalletTransaction;
    }
}
