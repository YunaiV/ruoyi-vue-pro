package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.dal.redis.wallet.PayWalletLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link PayWalletServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Import({PayWalletServiceImpl.class, PayWalletLockRedisDAO.class})
public class PayWalletServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayWalletServiceImpl walletService;

    @Resource
    private PayWalletMapper walletMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private PayWalletTransactionService walletTransactionService;
    @MockitoBean
    private PayOrderService orderService;
    @MockitoBean
    private PayRefundService refundService;

    @Test
    public void testGetOrCreateWallet_create() {
        // 调用：首次获取，创建钱包
        PayWalletDO wallet = walletService.getOrCreateWallet(1L, 2);
        // 断言
        assertNotNull(wallet.getId());
        PayWalletDO dbWallet = walletMapper.selectById(wallet.getId());
        assertEquals(1L, dbWallet.getUserId());
        assertEquals(2, dbWallet.getUserType());
        assertEquals(0, dbWallet.getBalance());

        // 调用：再次获取，不重复创建钱包
        assertEquals(wallet.getId(), walletService.getOrCreateWallet(1L, 2).getId());
        assertEquals(1, walletMapper.selectList().size());
    }

    @Test // 调用方存在事务时，首次创建的钱包需要独立提交（例如说：佣金提现审核时，创建钱包后再转账到钱包）
    public void testGetOrCreateWallet_outerTransaction() {
        // mock 方法（钱包流水）
        when(walletTransactionService.createWalletTransaction(any())).thenReturn(new PayWalletTransactionDO());

        // 调用：调用方事务中，首次创建钱包；随后不加入调用方事务（同 createTransfer），给钱包增加余额；最后调用方事务回滚
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        TransactionTemplate notSupportedTemplate = new TransactionTemplate(transactionManager);
        notSupportedTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        Long walletId = transactionTemplate.execute(status -> {
            PayWalletDO wallet = walletService.getOrCreateWallet(1L, 2);
            notSupportedTemplate.executeWithoutResult(notSupportedStatus -> walletService.addWalletBalance(
                    wallet.getId(), "T001", PayWalletBizTypeEnum.TRANSFER, 100));
            status.setRollbackOnly();
            return wallet.getId();
        });
        // 断言：钱包已独立提交，钱包余额已增加，不受调用方事务回滚影响
        PayWalletDO dbWallet = walletMapper.selectById(walletId);
        assertNotNull(dbWallet);
        assertEquals(100, dbWallet.getBalance());
    }

}
