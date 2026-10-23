package cn.iocoder.yudao.module.pay.service.wallet;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletTransactionMapper;
import cn.iocoder.yudao.module.pay.dal.redis.wallet.PayWalletLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.pay.service.wallet.bo.WalletTransactionCreateReqBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.REFUND_NOT_FOUND;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.WALLET_REFUND_EXIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private PayWalletTransactionMapper walletTransactionMapper;
    @Resource
    private PayRefundMapper refundMapper;

    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private DataSource dataSource;

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

    @Test
    public void testOrderRefund_success() {
        // mock 数据
        PayRefundDO refund = insertWalletRefund();
        // mock 方法
        mockRefundAndWalletTransaction();

        // 调用
        PayWalletTransactionDO transaction = walletService.orderRefund(refund.getNo(), 30, "测试退款");
        // 断言
        assertEquals(30, transaction.getPrice());
        assertEquals(String.valueOf(refund.getId()), transaction.getBizId());
        assertEquals(PayWalletBizTypeEnum.PAYMENT_REFUND.getType(), transaction.getBizType());
        assertEquals(130, walletMapper.selectById(transaction.getWalletId()).getBalance());
    }

    @Test // 已退款到钱包（例如说：退款入账后，更新退款单失败，退款单仍是 WAITING），使用原退款单号重新发起：返回原退款流水，不重复增加余额
    public void testOrderRefund_refundExists() {
        // mock 数据
        PayRefundDO refund = insertWalletRefund();
        PayWalletTransactionDO refundTransaction = insertRefundTransaction(refund, 30);
        // mock 方法
        mockRefundAndWalletTransaction();

        // 调用
        PayWalletTransactionDO transaction = walletService.orderRefund(refund.getNo(), 30, "测试退款");
        // 断言
        assertEquals(refundTransaction.getId(), transaction.getId());
        assertEquals(100, walletMapper.selectById(refundTransaction.getWalletId()).getBalance());
        verify(walletTransactionService, never()).createWalletTransaction(any());
    }

    @Test // 已退款到钱包，但退款金额不一致
    public void testOrderRefund_refundExistsAndPriceNotMatch() {
        // mock 数据
        PayRefundDO refund = insertWalletRefund();
        PayWalletTransactionDO refundTransaction = insertRefundTransaction(refund, 30);
        // mock 方法
        mockRefundAndWalletTransaction();

        // 调用，并断言异常
        assertServiceException(() -> walletService.orderRefund(refund.getNo(), 20, "测试退款"),
                WALLET_REFUND_EXIST);
        // 断言
        assertEquals(100, walletMapper.selectById(refundTransaction.getWalletId()).getBalance());
        verify(walletTransactionService, never()).createWalletTransaction(any());
    }

    @Test
    public void testOrderRefund_refundNotFound() {
        // mock 方法
        mockRefundAndWalletTransaction();

        // 调用，并断言异常
        assertServiceException(() -> walletService.orderRefund("R404", 30, "测试退款"),
                REFUND_NOT_FOUND);
    }

    @Test // 调用方存在事务，退款到钱包后调用方事务回滚：退款流水、余额一起回滚，再次发起时只增加一次余额
    public void testOrderRefund_outerTransactionRollback() {
        // mock 数据
        PayRefundDO refund = insertWalletRefund();
        Long walletId = walletTransactionMapper.selectByNo("W001").getWalletId();
        // mock 方法
        mockRefundAndWalletTransaction();

        // 调用：调用方事务中退款到钱包，随后调用方事务回滚
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            walletService.orderRefund(refund.getNo(), 30, "测试退款");
            status.setRollbackOnly();
        });
        // 断言：退款流水、余额一起回滚
        assertNull(walletTransactionMapper.selectByBiz(String.valueOf(refund.getId()),
                PayWalletBizTypeEnum.PAYMENT_REFUND.getType()));
        assertEquals(100, walletMapper.selectById(walletId).getBalance());

        // 调用：再次退款到钱包
        PayWalletTransactionDO transaction = walletService.orderRefund(refund.getNo(), 30, "测试退款");
        // 断言：只记录一条退款流水，只增加一次余额
        assertEquals(transaction.getId(), walletTransactionMapper.selectByBiz(String.valueOf(refund.getId()),
                PayWalletBizTypeEnum.PAYMENT_REFUND.getType()).getId());
        assertEquals(130, walletMapper.selectById(walletId).getBalance());
    }

    @Test // 并发使用同一退款单退款到钱包（例如说：结果未知的退款单重新发起，与原退款请求重叠），并且调用方存在事务：
          // 后者在退款单的行锁上等待，前者提交后，当前读到已提交的退款流水，返回原流水，只增加一次余额
    public void testOrderRefund_concurrent() throws Exception {
        // mock 数据
        PayRefundDO refund = insertWalletRefund();
        // mock 方法
        mockRefundAndWalletTransaction();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        CountDownLatch firstWritten = new CountDownLatch(1);
        CountDownLatch firstCommit = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // 调用：第一个请求在调用方事务中退款到钱包，写入退款流水后，暂不提交
            Future<PayWalletTransactionDO> future01 = executor.submit(() -> transactionTemplate.execute(status -> {
                PayWalletTransactionDO transaction = walletService.orderRefund(refund.getNo(), 30, "测试退款");
                firstWritten.countDown();
                awaitLatch(firstCommit);
                return transaction;
            }));
            assertTrue(firstWritten.await(10, TimeUnit.SECONDS));
            // 调用：第二个请求在调用方事务中，先查询退款流水（不存在），再退款到钱包，在退款单的行锁上等待
            Future<PayWalletTransactionDO> future02 = executor.submit(() -> transactionTemplate.execute(status -> {
                assertNull(walletTransactionMapper.selectByBiz(String.valueOf(refund.getId()),
                        PayWalletBizTypeEnum.PAYMENT_REFUND.getType()));
                return walletService.orderRefund(refund.getNo(), 30, "测试退款");
            }));
            // 断言：第二个请求确实在等待锁，然后提交第一个请求
            assertTrue(waitLockWaiting());
            firstCommit.countDown();
            PayWalletTransactionDO transaction01 = future01.get(10, TimeUnit.SECONDS);
            PayWalletTransactionDO transaction02 = future02.get(10, TimeUnit.SECONDS);

            // 断言：返回同一条退款流水，只记录一条退款流水，只增加一次余额
            assertEquals(transaction01.getId(), transaction02.getId());
            verify(walletTransactionService, times(1)).createWalletTransaction(any());
            assertEquals(1L, walletTransactionMapper.selectCount(PayWalletTransactionDO::getBizType,
                    PayWalletBizTypeEnum.PAYMENT_REFUND.getType()));
            assertEquals(130, walletMapper.selectById(transaction01.getWalletId()).getBalance());
        } finally {
            firstCommit.countDown();
            executor.shutdownNow();
        }
    }

    @Test // 新建事务时，使用读已提交，避免当前读不存在的退款流水时加间隙锁；加入调用方的已有事务时，沿用调用方的隔离级别
          // 注意：H2 无法模拟 MySQL 可重复读下的间隙锁，这里只校验事务的隔离级别，间隙锁死锁需要 MySQL 实库验证
    public void testOrderRefund_isolation() {
        // mock 数据
        PayRefundDO refund = insertWalletRefund();
        // mock 方法：锁定退款单时，记录当前事务声明、实际连接的隔离级别
        mockRefundAndWalletTransaction();
        List<Integer> declaredIsolationLevels = new ArrayList<>();
        List<Integer> connectionIsolationLevels = new ArrayList<>();
        when(refundService.getRefundForUpdate(any())).thenAnswer(invocation -> {
            declaredIsolationLevels.add(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
            connectionIsolationLevels.add(DataSourceUtils.getConnection(dataSource).getTransactionIsolation());
            return refundMapper.selectOneForUpdate(PayRefundDO::getId, invocation.getArgument(0));
        });

        // 调用：不存在调用方事务，新建事务退款到钱包
        PayWalletTransactionDO transaction = walletService.orderRefund(refund.getNo(), 30, "测试退款");
        // 调用：调用方事务（可重复读）中，再次退款到钱包
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        PayWalletTransactionDO transaction02 = transactionTemplate.execute(status ->
                walletService.orderRefund(refund.getNo(), 30, "测试退款"));

        // 断言：新建事务使用读已提交；加入调用方事务时，不覆盖调用方的可重复读
        assertEquals(Arrays.asList(Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ),
                declaredIsolationLevels);
        assertEquals(Arrays.asList(Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ),
                connectionIsolationLevels);
        // 断言：返回同一条退款流水，只增加一次余额
        assertNotNull(transaction02);
        assertEquals(transaction.getId(), transaction02.getId());
        verify(walletTransactionService, times(1)).createWalletTransaction(any());
        assertEquals(130, walletMapper.selectById(transaction.getWalletId()).getBalance());
    }

    /**
     * 插入钱包、钱包支付流水、退款单：钱包余额为 100
     */
    private PayRefundDO insertWalletRefund() {
        PayWalletDO wallet = new PayWalletDO().setUserId(1L).setUserType(2)
                .setBalance(100).setFreezePrice(0).setTotalExpense(30).setTotalRecharge(130);
        walletMapper.insert(wallet);
        PayWalletTransactionDO payTransaction = randomPojo(PayWalletTransactionDO.class, o -> {
            o.setNo("W001");
            o.setWalletId(wallet.getId());
            o.setBizType(PayWalletBizTypeEnum.PAYMENT.getType());
            o.setPrice(-30);
        });
        walletTransactionMapper.insert(payTransaction);
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setNo("R001").setChannelOrderNo("W001")
                .setRefundPrice(30).setStatus(PayRefundStatusEnum.WAITING.getStatus()));
        refundMapper.insert(refund);
        return refund;
    }

    /**
     * 插入已退款到钱包的退款流水（未增加余额，只用于校验不会重复退款）
     */
    private PayWalletTransactionDO insertRefundTransaction(PayRefundDO refund, Integer price) {
        PayWalletTransactionDO payTransaction = walletTransactionMapper.selectByNo("W001");
        PayWalletTransactionDO refundTransaction = randomPojo(PayWalletTransactionDO.class, o -> {
            o.setWalletId(payTransaction.getWalletId());
            o.setBizType(PayWalletBizTypeEnum.PAYMENT_REFUND.getType());
            o.setBizId(String.valueOf(refund.getId()));
            o.setPrice(price);
        });
        walletTransactionMapper.insert(refundTransaction);
        return refundTransaction;
    }

    /**
     * mock 退款单、钱包流水的方法：使用数据库的真实查询，校验锁定退款单、查询退款流水的顺序
     */
    private void mockRefundAndWalletTransaction() {
        when(refundService.getRefundByNo(any())).thenAnswer(invocation ->
                refundMapper.selectByNo(invocation.getArgument(0)));
        when(refundService.getRefundForUpdate(any())).thenAnswer(invocation ->
                refundMapper.selectOneForUpdate(PayRefundDO::getId, invocation.getArgument(0)));
        when(walletTransactionService.getWalletTransactionByNo(any())).thenAnswer(invocation ->
                walletTransactionMapper.selectByNo(invocation.getArgument(0)));
        when(walletTransactionService.getWalletTransactionForUpdate(any(), any())).thenAnswer(invocation ->
                walletTransactionMapper.selectByBizForUpdate(invocation.getArgument(0),
                        ((PayWalletBizTypeEnum) invocation.getArgument(1)).getType()));
        when(walletTransactionService.createWalletTransaction(any())).thenAnswer(invocation ->
                createWalletTransaction(invocation.getArgument(0)));
    }

    /**
     * 等待存在锁等待的数据库会话，基于 H2 的 INFORMATION_SCHEMA.SESSIONS 表的 BLOCKER_ID 字段
     *
     * @return 是否存在
     */
    private boolean waitLockWaiting() throws InterruptedException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        for (int i = 0; i < 500; i++) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS WHERE BLOCKER_ID IS NOT NULL", Integer.class);
            if (count != null && count > 0) {
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private PayWalletTransactionDO createWalletTransaction(WalletTransactionCreateReqBO bo) {
        PayWalletTransactionDO transaction = new PayWalletTransactionDO();
        transaction.setNo(randomString());
        transaction.setWalletId(bo.getWalletId());
        transaction.setBizType(bo.getBizType());
        transaction.setBizId(bo.getBizId());
        transaction.setTitle(bo.getTitle());
        transaction.setPrice(bo.getPrice());
        transaction.setBalance(bo.getBalance());
        walletTransactionMapper.insert(transaction);
        return transaction;
    }

}
