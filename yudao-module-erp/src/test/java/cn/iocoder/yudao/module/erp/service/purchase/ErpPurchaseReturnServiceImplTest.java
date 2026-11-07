package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ErpPurchaseReturnServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class ErpPurchaseReturnServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ErpPurchaseReturnServiceImpl purchaseReturnService;

    @Mock
    private ErpPurchaseReturnMapper purchaseReturnMapper;

    @Test
    public void testUpdatePurchaseReturnRefundPrice_sameValueDifferentScale() {
        // mock 数据
        when(purchaseReturnMapper.selectById(1L)).thenReturn(new ErpPurchaseReturnDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setRefundPrice(new BigDecimal("1.0")));

        // 调用
        purchaseReturnService.updatePurchaseReturnRefundPrice(1L, new BigDecimal("1.00"));
        // 断言：金额未变化，不更新
        verify(purchaseReturnMapper, never()).updateById(any(ErpPurchaseReturnDO.class));
    }

    @Test
    public void testUpdatePurchaseReturnRefundPrice_changed() {
        // mock 数据
        when(purchaseReturnMapper.selectById(1L)).thenReturn(new ErpPurchaseReturnDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setRefundPrice(new BigDecimal("1.0")));

        // 调用
        purchaseReturnService.updatePurchaseReturnRefundPrice(1L, new BigDecimal("10.00"));
        // 断言
        ArgumentCaptor<ErpPurchaseReturnDO> captor = ArgumentCaptor.forClass(ErpPurchaseReturnDO.class);
        verify(purchaseReturnMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(new BigDecimal("10.00"), captor.getValue().getRefundPrice());
    }

    @Test
    public void testUpdatePurchaseReturnRefundPrice_exceed() {
        // mock 数据
        when(purchaseReturnMapper.selectById(1L)).thenReturn(new ErpPurchaseReturnDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setRefundPrice(new BigDecimal("1.0")));

        // 调用，并断言
        assertServiceException(() -> purchaseReturnService.updatePurchaseReturnRefundPrice(1L, new BigDecimal("10.01")),
                PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED, new BigDecimal("10.01"), new BigDecimal("10.00"));
        verify(purchaseReturnMapper, never()).updateById(any(ErpPurchaseReturnDO.class));
    }

}
