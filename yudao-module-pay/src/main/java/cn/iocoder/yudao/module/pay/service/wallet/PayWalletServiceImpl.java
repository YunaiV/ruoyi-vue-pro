package cn.iocoder.yudao.module.pay.service.wallet;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.wallet.PayWalletPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.dal.redis.wallet.PayWalletLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.pay.service.wallet.bo.WalletTransactionCreateReqBO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum.PAYMENT;
import static cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum.PAYMENT_REFUND;

/**
 * 钱包 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayWalletServiceImpl implements PayWalletService {

    /**
     * 通知超时时间，单位：毫秒
     */
    public static final long UPDATE_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

    @Resource
    private PayWalletMapper walletMapper;
    @Resource
    private PayWalletLockRedisDAO lockRedisDAO;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PayWalletTransactionService walletTransactionService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PayOrderService orderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PayRefundService refundService;

    @Override
    @SneakyThrows
    public PayWalletDO getOrCreateWallet(Long userId, Integer userType) {
        PayWalletDO wallet = walletMapper.selectByUserIdAndType(userId, userType);
        if (wallet == null) {
            // 使用双重检查锁，保证钱包创建并发问题
            // https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1475/files
            wallet = lockRedisDAO.lock(userId, UPDATE_TIMEOUT_MILLIS,
                    () -> getSelf().createWalletIfAbsent(userId, userType));
        }
        return wallet;
    }

    /**
     * 创建钱包，如果不存在的话
     *
     * 注意：使用新事务，在释放锁之前提交，不加入调用方的事务。原因是：
     * 1. 调用方的事务未提交时，其它请求查询不到钱包，会重复创建钱包
     * 2. 后续不加入调用方事务的操作，也需要查询到钱包。例如说，佣金提现到钱包时，转账 createTransfer 不加入调用方的事务
     * 另外，钱包是空钱包，即使调用方的事务回滚，保留也没有影响
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @return 钱包
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public PayWalletDO createWalletIfAbsent(Long userId, Integer userType) {
        PayWalletDO wallet = walletMapper.selectByUserIdAndType(userId, userType);
        if (wallet != null) {
            return wallet;
        }
        wallet = new PayWalletDO().setUserId(userId).setUserType(userType)
                .setBalance(0).setTotalExpense(0).setTotalRecharge(0);
        wallet.setCreateTime(LocalDateTime.now());
        walletMapper.insert(wallet);
        return wallet;
    }

    @Override
    public PayWalletDO getWallet(Long walletId) {
        return walletMapper.selectById(walletId);
    }

    @Override
    public PageResult<PayWalletDO> getWalletPage(PayWalletPageReqVO pageReqVO) {
        return walletMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransactionDO orderPay(Long walletId, String outTradeNo, Integer price) {
        // 1. 判断支付交易拓展单是否存
        PayOrderExtensionDO orderExtension = orderService.getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            throw exception(PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        PayWalletDO wallet = walletMapper.selectById(walletId);
        // 2. 扣减余额
        return reduceWalletBalance(wallet.getId(), orderExtension.getOrderId(), PAYMENT, price);
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
        Long walletId = validateWalletCanRefund(payRefund.getId(), payRefund.getChannelOrderNo());
        PayWalletDO wallet = walletMapper.selectById(walletId);
        Assert.notNull(wallet, "钱包 {} 不存在", walletId);

        // 2. 增加余额
        return addWalletBalance(walletId, String.valueOf(payRefund.getId()), PAYMENT_REFUND, refundPrice);
    }

    /**
     * 校验是否能退款
     *
     * @param refundId 支付退款单 id
     * @param walletPayNo 钱包支付 no
     */
    private Long validateWalletCanRefund(Long refundId, String walletPayNo) {
        // 1. 校验钱包支付交易存在
        PayWalletTransactionDO walletTransaction = walletTransactionService.getWalletTransactionByNo(walletPayNo);
        if (walletTransaction == null) {
            throw exception(WALLET_TRANSACTION_NOT_FOUND);
        }
        // 2. 校验退款是否存在
        PayWalletTransactionDO refundTransaction = walletTransactionService.getWalletTransaction(
                String.valueOf(refundId), PAYMENT_REFUND);
        if (refundTransaction != null) {
            throw exception(WALLET_REFUND_EXIST);
        }
        return walletTransaction.getWalletId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public PayWalletTransactionDO reduceWalletBalance(Long walletId, Long bizId,
                                                      PayWalletBizTypeEnum bizType, Integer price) {
        // 1. 获取钱包
        PayWalletDO payWallet = getWallet(walletId);
        if (payWallet == null) {
            log.error("[reduceWalletBalance][用户钱包({})不存在]", walletId);
            throw exception(WALLET_NOT_FOUND);
        }

        // 2. 加锁，更新钱包余额（目的：避免钱包流水的并发更新时，余额变化不连贯）
        return lockRedisDAO.lock(walletId, UPDATE_TIMEOUT_MILLIS, () -> {
            // 2.1 扣除余额
            int updateCounts;
            switch (bizType) {
                case PAYMENT: {
                    updateCounts = walletMapper.updateWhenConsumption(payWallet.getId(), price);
                    break;
                }
                case RECHARGE_REFUND: {
                    updateCounts = walletMapper.updateWhenRechargeRefund(payWallet.getId(), price);
                    break;
                }
                default: {
                    // TODO 其它类型待实现
                    throw new UnsupportedOperationException("待实现");
                }
            }
            if (updateCounts == 0) {
                throw exception(WALLET_BALANCE_NOT_ENOUGH);
            }

            // 2.2 更新后重新查询钱包，避免使用锁外读取的旧余额生成流水
            PayWalletDO currentWallet = getWallet(walletId);

            // 3. 生成钱包流水
            // 充值退款时，余额已在冻结时扣除，本次只扣冻结金额。https://t.zsxq.com/OJk9m
            WalletTransactionCreateReqBO bo = new WalletTransactionCreateReqBO().setWalletId(payWallet.getId())
                    .setPrice(-price).setBalance(currentWallet.getBalance()).setBizId(String.valueOf(bizId))
                    .setBizType(bizType.getType()).setTitle(bizType.getDescription());
            return walletTransactionService.createWalletTransaction(bo);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public PayWalletTransactionDO addWalletBalance(Long walletId, String bizId,
                                                   PayWalletBizTypeEnum bizType, Integer price) {
        // 1. 获取钱包
        PayWalletDO payWallet = getWallet(walletId);
        if (payWallet == null) {
            log.error("[addWalletBalance][用户钱包({})不存在]", walletId);
            throw exception(WALLET_NOT_FOUND);
        }

        // 2. 加锁，更新钱包余额（目的：避免钱包流水的并发更新时，余额变化不连贯）
        return lockRedisDAO.lock(walletId, UPDATE_TIMEOUT_MILLIS, () -> {
            // 2.1 更新钱包金额
            switch (bizType) {
                case PAYMENT_REFUND: { // 退款更新
                    walletMapper.updateWhenConsumptionRefund(payWallet.getId(), price);
                    break;
                }
                case RECHARGE: { // 充值更新
                    walletMapper.updateWhenRecharge(payWallet.getId(), price);
                    break;
                }
                case UPDATE_BALANCE: // 更新余额
                case TRANSFER: // 分佣提现
                    walletMapper.updateWhenAdd(payWallet.getId(), price);
                    break;
                default: {
                    throw new UnsupportedOperationException("待实现：" + bizType);
                }
            }

            // 2.2 更新后重新查询钱包，避免使用锁外读取的旧余额生成流水
            PayWalletDO currentWallet = getWallet(walletId);

            // 3. 生成钱包流水
            WalletTransactionCreateReqBO transactionCreateReqBO = new WalletTransactionCreateReqBO()
                    .setWalletId(payWallet.getId()).setPrice(price).setBalance(currentWallet.getBalance())
                    .setBizId(bizId).setBizType(bizType.getType()).setTitle(bizType.getDescription());
            return walletTransactionService.createWalletTransaction(transactionCreateReqBO);
        });
    }

    @Override
    public void freezePrice(Long id, Integer price) {
        int updateCounts = walletMapper.freezePrice(id, price);
        if (updateCounts == 0) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }
    }

    @Override
    public void unfreezePrice(Long id, Integer price) {
        int updateCounts = walletMapper.unFreezePrice(id, price);
        if (updateCounts == 0) {
            throw exception(WALLET_FREEZE_PRICE_NOT_ENOUGH);
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayWalletServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
