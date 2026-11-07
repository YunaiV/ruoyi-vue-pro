package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ErpSaleOutServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class ErpSaleOutServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ErpSaleOutServiceImpl saleOutService;

    @Mock
    private ErpSaleOutMapper saleOutMapper;

    @Test
    public void testUpdateSaleInReceiptPrice_sameValueDifferentScale() {
        // mock 数据
        when(saleOutMapper.selectById(1L)).thenReturn(new ErpSaleOutDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setReceiptPrice(new BigDecimal("1.0")));

        // 调用
        saleOutService.updateSaleInReceiptPrice(1L, new BigDecimal("1.00"));
        // 断言：金额未变化，不更新
        verify(saleOutMapper, never()).updateById(any(ErpSaleOutDO.class));
    }

    @Test
    public void testUpdateSaleInReceiptPrice_changed() {
        // mock 数据
        when(saleOutMapper.selectById(1L)).thenReturn(new ErpSaleOutDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setReceiptPrice(new BigDecimal("1.0")));

        // 调用
        saleOutService.updateSaleInReceiptPrice(1L, new BigDecimal("2"));
        // 断言
        ArgumentCaptor<ErpSaleOutDO> captor = ArgumentCaptor.forClass(ErpSaleOutDO.class);
        verify(saleOutMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(new BigDecimal("2"), captor.getValue().getReceiptPrice());
    }

    @Test
    public void testUpdateSaleInReceiptPrice_exceed() {
        // mock 数据
        when(saleOutMapper.selectById(1L)).thenReturn(new ErpSaleOutDO().setId(1L)
                .setTotalPrice(new BigDecimal("10.00")).setReceiptPrice(new BigDecimal("1.0")));

        // 调用，并断言
        assertServiceException(() -> saleOutService.updateSaleInReceiptPrice(1L, new BigDecimal("10.01")),
                SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED, new BigDecimal("10.01"), new BigDecimal("10.00"));
        verify(saleOutMapper, never()).updateById(any(ErpSaleOutDO.class));
    }

}
