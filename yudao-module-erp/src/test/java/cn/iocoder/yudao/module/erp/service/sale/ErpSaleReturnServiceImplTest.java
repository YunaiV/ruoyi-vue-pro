package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleReturnMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_RETURN_FAIL_REFUND_PRICE_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ErpSaleReturnServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class ErpSaleReturnServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ErpSaleReturnServiceImpl saleReturnService;

    @Mock
    private ErpSaleReturnMapper saleReturnMapper;

    @Test
    public void testUpdateSaleReturnRefundPrice_sameValueDifferentScale() {
        // mock 数据
        when(saleReturnMapper.selectById(1L)).thenReturn(new ErpSaleReturnDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setRefundPrice(new BigDecimal("1.0")));

        // 调用
        saleReturnService.updateSaleReturnRefundPrice(1L, new BigDecimal("1.00"));
        // 断言：金额未变化，不更新
        verify(saleReturnMapper, never()).updateById(any(ErpSaleReturnDO.class));
    }

    @Test
    public void testUpdateSaleReturnRefundPrice_changed() {
        // mock 数据
        when(saleReturnMapper.selectById(1L)).thenReturn(new ErpSaleReturnDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setRefundPrice(new BigDecimal("1.0")));

        // 调用
        saleReturnService.updateSaleReturnRefundPrice(1L, new BigDecimal("2"));
        // 断言
        ArgumentCaptor<ErpSaleReturnDO> captor = ArgumentCaptor.forClass(ErpSaleReturnDO.class);
        verify(saleReturnMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(new BigDecimal("2"), captor.getValue().getRefundPrice());
    }

    @Test
    public void testUpdateSaleReturnRefundPrice_exceed() {
        // mock 数据
        when(saleReturnMapper.selectById(1L)).thenReturn(new ErpSaleReturnDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setRefundPrice(new BigDecimal("1.0")));

        // 调用，并断言
        assertServiceException(() -> saleReturnService.updateSaleReturnRefundPrice(1L, new BigDecimal("10.01")),
                SALE_RETURN_FAIL_REFUND_PRICE_EXCEED, new BigDecimal("10.01"), new BigDecimal("10.00"));
        verify(saleReturnMapper, never()).updateById(any(ErpSaleReturnDO.class));
    }

}
