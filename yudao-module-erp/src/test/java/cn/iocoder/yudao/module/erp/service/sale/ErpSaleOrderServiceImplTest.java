package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ErpSaleOrderServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class ErpSaleOrderServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ErpSaleOrderServiceImpl saleOrderService;

    @Mock
    private ErpSaleOrderMapper saleOrderMapper;
    @Mock
    private ErpSaleOrderItemMapper saleOrderItemMapper;
    @Mock
    private ErpProductService productService;

    @Test
    public void testUpdateSaleOrderOutCount_sameValueDifferentScale() {
        // mock 数据
        ErpSaleOrderItemDO item = new ErpSaleOrderItemDO().setId(10L).setProductId(100L)
                .setCount(new BigDecimal("5")).setOutCount(new BigDecimal("1.0"));
        when(saleOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));

        // 调用
        saleOrderService.updateSaleOrderOutCount(1L, Map.of(10L, new BigDecimal("1.00")));
        // 断言：订单项数值未变化，不更新
        verify(saleOrderItemMapper, never()).updateById(any(ErpSaleOrderItemDO.class));
        verify(saleOrderMapper).updateById(any(ErpSaleOrderDO.class));
    }

    @Test
    public void testUpdateSaleOrderOutCount_exceed() {
        // mock 数据
        ErpSaleOrderItemDO item = new ErpSaleOrderItemDO().setId(10L).setProductId(100L)
                .setCount(new BigDecimal("5")).setOutCount(new BigDecimal("1.0"));
        when(saleOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));
        when(productService.getProduct(100L)).thenReturn(new ErpProductDO().setId(100L).setName("商品"));

        // 调用，并断言
        assertServiceException(() -> saleOrderService.updateSaleOrderOutCount(1L,
                Map.of(10L, new BigDecimal("5.01"))), SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED, "商品", item.getCount());
        verify(saleOrderItemMapper, never()).updateById(any(ErpSaleOrderItemDO.class));
    }

    @Test
    public void testUpdateSaleOrderReturnCount_sameValueDifferentScale() {
        // mock 数据
        ErpSaleOrderItemDO item = new ErpSaleOrderItemDO().setId(10L).setProductId(100L)
                .setOutCount(new BigDecimal("3")).setReturnCount(new BigDecimal("1.0"));
        when(saleOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));

        // 调用
        saleOrderService.updateSaleOrderReturnCount(1L, Map.of(10L, new BigDecimal("1.00")));
        // 断言：订单项数值未变化，不更新
        verify(saleOrderItemMapper, never()).updateById(any(ErpSaleOrderItemDO.class));
        verify(saleOrderMapper).updateById(any(ErpSaleOrderDO.class));
    }

    @Test
    public void testUpdateSaleOrderReturnCount_changed() {
        // mock 数据
        ErpSaleOrderItemDO item = new ErpSaleOrderItemDO().setId(10L).setProductId(100L)
                .setOutCount(new BigDecimal("3")).setReturnCount(new BigDecimal("1.0"));
        when(saleOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));

        // 调用
        saleOrderService.updateSaleOrderReturnCount(1L, Map.of(10L, new BigDecimal("3")));
        // 断言
        ArgumentCaptor<ErpSaleOrderItemDO> captor = ArgumentCaptor.forClass(ErpSaleOrderItemDO.class);
        verify(saleOrderItemMapper).updateById(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals(new BigDecimal("3"), captor.getValue().getReturnCount());
    }

    @Test
    public void testUpdateSaleOrderReturnCount_exceed() {
        // mock 数据
        ErpSaleOrderItemDO item = new ErpSaleOrderItemDO().setId(10L).setProductId(100L)
                .setOutCount(new BigDecimal("3")).setReturnCount(new BigDecimal("1.0"));
        when(saleOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));
        when(productService.getProduct(100L)).thenReturn(new ErpProductDO().setId(100L).setName("商品"));

        // 调用，并断言
        assertServiceException(() -> saleOrderService.updateSaleOrderReturnCount(1L,
                Map.of(10L, new BigDecimal("3.5"))), SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED, "商品", item.getOutCount());
        verify(saleOrderItemMapper, never()).updateById(any(ErpSaleOrderItemDO.class));
    }

}
