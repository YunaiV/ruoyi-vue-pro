package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.wallet;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.exception.PayClientException;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.REFUND_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link WalletPayClient} 的单元测试类
 *
 * @author 芋道源码
 */
public class WalletPayClientTest extends BaseMockitoUnitTest {

    private WalletPayClient client;

    @Mock
    private PayWalletService walletService;
    @Mock
    private PayWalletTransactionService walletTransactionService;
    @Mock
    private PayRefundService refundService;

    @BeforeEach
    public void setUp() {
        client = new WalletPayClient(1L, new NonePayClientConfig());
        ReflectionTestUtils.setField(client, "wallService", walletService);
        ReflectionTestUtils.setField(client, "walletTransactionService", walletTransactionService);
        ReflectionTestUtils.setField(client, "refundService", refundService);
    }

    @Test // 已退款到钱包，但退款单仍是 WAITING（例如说：退款入账后，更新退款单失败）：以钱包退款流水为准，返回退款成功
    public void testGetRefund_waitingWithTransaction() {
        // mock 方法
        PayRefundDO refund = mockRefund(PayRefundStatusEnum.WAITING.getStatus());
        PayWalletTransactionDO transaction = mockRefundTransaction(refund);

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), resp.getStatus());
        assertEquals(transaction.getNo(), resp.getChannelRefundNo());
        assertEquals("R001", resp.getOutRefundNo());
    }

    @Test // 已退款到钱包，但退款单是退款失败：以钱包退款流水为准，返回退款成功
    public void testGetRefund_failureWithTransaction() {
        // mock 方法
        PayRefundDO refund = mockRefund(PayRefundStatusEnum.FAILURE.getStatus());
        mockRefundTransaction(refund);

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), resp.getStatus());
    }

    @Test // 退款单是 WAITING，钱包退款流水不存在：可能未退款、或者退款事务尚未提交，返回结果未知，不作为退款失败
    public void testGetRefund_waitingWithoutTransaction() {
        // mock 方法
        mockRefund(PayRefundStatusEnum.WAITING.getStatus());

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), resp.getStatus());
        assertNull(resp.getChannelRefundNo());
        assertEquals("R001", resp.getOutRefundNo());
    }

    @Test // 退款单是退款失败，钱包退款流水不存在：返回退款失败
    public void testGetRefund_failureWithoutTransaction() {
        // mock 方法
        PayRefundDO refund = mockRefund(PayRefundStatusEnum.FAILURE.getStatus());

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), resp.getStatus());
        assertEquals(refund.getChannelErrorCode(), resp.getChannelErrorCode());
        assertEquals(refund.getChannelErrorMsg(), resp.getChannelErrorMsg());
    }

    @Test // 退款单是退款成功，但钱包退款流水不存在：无效状态，抛出异常
    public void testGetRefund_successWithoutTransaction() {
        // mock 方法
        mockRefund(PayRefundStatusEnum.SUCCESS.getStatus());

        // 调用，并断言异常
        assertThrows(PayClientException.class, () -> client.getRefund("P001", "R001"));
    }

    @Test // 退款单不存在：返回退款失败，错误码为数字编码
    public void testGetRefund_refundNotFound() {
        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), resp.getStatus());
        assertEquals(String.valueOf(REFUND_NOT_FOUND.getCode()), resp.getChannelErrorCode());
        assertEquals(REFUND_NOT_FOUND.getMsg(), resp.getChannelErrorMsg());
    }

    private PayRefundDO mockRefund(Integer status) {
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setId(100L).setNo("R001").setStatus(status));
        when(refundService.getRefundByNo(eq("R001"))).thenReturn(refund);
        return refund;
    }

    private PayWalletTransactionDO mockRefundTransaction(PayRefundDO refund) {
        PayWalletTransactionDO transaction = randomPojo(PayWalletTransactionDO.class);
        when(walletTransactionService.getWalletTransaction(eq(String.valueOf(refund.getId())),
                eq(PayWalletBizTypeEnum.PAYMENT_REFUND))).thenReturn(transaction);
        return transaction;
    }

}
