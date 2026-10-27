package cn.iocoder.yudao.module.pay.service.demo;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.mockito.Mockito.*;

/**
 * {@link PayDemoOrderServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class PayDemoOrderServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PayDemoOrderServiceImpl payDemoOrderService;

    @Mock
    private PayOrderApi payOrderApi;
    @Mock
    private PayRefundApi payRefundApi;
    @Mock
    private PayDemoOrderMapper payDemoOrderMapper;

    @Test
    public void testUpdateDemoOrderPaid_success() {
        // mock 数据
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(false));
        when(payOrderApi.getOrder(eq(100L))).thenReturn(buildPayOrder(100L, PayOrderStatusEnum.SUCCESS.getStatus(), 10, "1"));
        when(payDemoOrderMapper.updateByIdAndPayed(eq(1L), eq(false), any())).thenReturn(1);

        // 调用
        payDemoOrderService.updateDemoOrderPaid(1L, 100L);
        // 断言
        verify(payDemoOrderMapper).updateByIdAndPayed(eq(1L), eq(false),
                argThat(updateObj -> updateObj.getPayStatus() && "mock".equals(updateObj.getPayChannelCode())));
    }

    @Test
    public void testUpdateDemoOrderPaid_repeatNotify() {
        // mock 数据：已支付，且支付单号相同
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(true));

        // 调用：重复通知，幂等返回
        payDemoOrderService.updateDemoOrderPaid(1L, 100L);
        // 断言
        verifyNoInteractions(payOrderApi);
        verify(payDemoOrderMapper, never()).updateByIdAndPayed(anyLong(), anyBoolean(), any());
    }

    @Test
    public void testUpdateDemoOrderPaid_paidPayOrderIdNotMatch() {
        // mock 数据：已支付，但支付单号不同
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(true));

        // 调用，并断言异常
        assertServiceException(() -> payDemoOrderService.updateDemoOrderPaid(1L, 200L),
                DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        verifyNoInteractions(payOrderApi);
        verify(payDemoOrderMapper, never()).updateByIdAndPayed(anyLong(), anyBoolean(), any());
    }

    @Test
    public void testUpdateDemoOrderPaid_unpaidPayOrderIdNotBound() {
        // mock 数据：未支付；其它支付应用中，存在相同商户订单编号、金额，且已支付的支付单
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(false));

        // 调用，并断言异常
        assertServiceException(() -> payDemoOrderService.updateDemoOrderPaid(1L, 200L),
                DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        // 断言：不查询支付单，也不更新订单
        verifyNoInteractions(payOrderApi);
        verify(payDemoOrderMapper, never()).updateByIdAndPayed(anyLong(), anyBoolean(), any());
    }

    @Test
    public void testUpdateDemoOrderPaid_payOrderNotSuccess() {
        // mock 数据：支付单号一致，但未支付
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(false));
        when(payOrderApi.getOrder(eq(100L))).thenReturn(buildPayOrder(100L, PayOrderStatusEnum.WAITING.getStatus(), 10, "1"));

        // 调用，并断言异常
        assertServiceException(() -> payDemoOrderService.updateDemoOrderPaid(1L, 100L),
                DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        verify(payDemoOrderMapper, never()).updateByIdAndPayed(anyLong(), anyBoolean(), any());
    }

    @Test
    public void testUpdateDemoOrderPaid_payPriceNotMatch() {
        // mock 数据：支付单号一致，但金额不同
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(false));
        when(payOrderApi.getOrder(eq(100L))).thenReturn(buildPayOrder(100L, PayOrderStatusEnum.SUCCESS.getStatus(), 1, "1"));

        // 调用，并断言异常
        assertServiceException(() -> payDemoOrderService.updateDemoOrderPaid(1L, 100L),
                DEMO_ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        verify(payDemoOrderMapper, never()).updateByIdAndPayed(anyLong(), anyBoolean(), any());
    }

    @Test
    public void testUpdateDemoOrderPaid_merchantOrderIdNotMatch() {
        // mock 数据：支付单号一致，但商户订单编号不同
        when(payDemoOrderMapper.selectById(eq(1L))).thenReturn(buildOrder(false));
        when(payOrderApi.getOrder(eq(100L))).thenReturn(buildPayOrder(100L, PayOrderStatusEnum.SUCCESS.getStatus(), 10, "2"));

        // 调用，并断言异常
        assertServiceException(() -> payDemoOrderService.updateDemoOrderPaid(1L, 100L),
                DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        verify(payDemoOrderMapper, never()).updateByIdAndPayed(anyLong(), anyBoolean(), any());
    }

    private static PayDemoOrderDO buildOrder(boolean payStatus) {
        return new PayDemoOrderDO().setId(1L).setUserId(1L).setSpuId(1L).setSpuName("华为手机")
                .setPrice(10).setPayStatus(payStatus).setPayOrderId(100L);
    }

    private static PayOrderRespDTO buildPayOrder(Long id, Integer status, Integer price, String merchantOrderId) {
        return new PayOrderRespDTO().setId(id).setStatus(status).setPrice(price)
                .setMerchantOrderId(merchantOrderId).setChannelCode("mock");
    }

}
