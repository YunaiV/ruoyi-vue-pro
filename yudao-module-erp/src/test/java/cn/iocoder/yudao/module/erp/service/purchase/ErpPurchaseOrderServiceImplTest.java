package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ErpPurchaseOrderServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class ErpPurchaseOrderServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ErpPurchaseOrderServiceImpl purchaseOrderService;

    @Mock
    private ErpPurchaseOrderMapper purchaseOrderMapper;
    @Mock
    private ErpPurchaseOrderItemMapper purchaseOrderItemMapper;
    @Mock
    private ErpProductService productService;

    @Test
    public void testUpdatePurchaseOrderInCount_sameValueDifferentScale() {
        // mock 数据
        ErpPurchaseOrderItemDO item = new ErpPurchaseOrderItemDO().setId(10L).setProductId(100L)
                .setCount(new BigDecimal("5")).setInCount(new BigDecimal("1.0"));
        when(purchaseOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));

        // 调用
        purchaseOrderService.updatePurchaseOrderInCount(1L, Map.of(10L, new BigDecimal("1.00")));
        // 断言：订单项数值未变化，不更新
        verify(purchaseOrderItemMapper, never()).updateById(any(ErpPurchaseOrderItemDO.class));
        verify(purchaseOrderMapper).updateById(any(ErpPurchaseOrderDO.class));
    }

    @Test
    public void testUpdatePurchaseOrderInCount_changed() {
        // mock 数据
        ErpPurchaseOrderItemDO item = new ErpPurchaseOrderItemDO().setId(10L).setProductId(100L)
                .setCount(new BigDecimal("5")).setInCount(new BigDecimal("1.0"));
        when(purchaseOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));

        // 调用
        purchaseOrderService.updatePurchaseOrderInCount(1L, Map.of(10L, new BigDecimal("2")));
        // 断言
        ArgumentCaptor<ErpPurchaseOrderItemDO> captor = ArgumentCaptor.forClass(ErpPurchaseOrderItemDO.class);
        verify(purchaseOrderItemMapper).updateById(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals(new BigDecimal("2"), captor.getValue().getInCount());
    }

    @Test
    public void testUpdatePurchaseOrderInCount_exceed() {
        // mock 数据
        ErpPurchaseOrderItemDO item = new ErpPurchaseOrderItemDO().setId(10L).setProductId(100L)
                .setCount(new BigDecimal("5")).setInCount(new BigDecimal("1.0"));
        when(purchaseOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));
        when(productService.getProduct(100L)).thenReturn(new ErpProductDO().setId(100L).setName("商品"));

        // 调用，并断言
        assertServiceException(() -> purchaseOrderService.updatePurchaseOrderInCount(1L,
                Map.of(10L, new BigDecimal("5.01"))), PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED, "商品", item.getCount());
        verify(purchaseOrderItemMapper, never()).updateById(any(ErpPurchaseOrderItemDO.class));
    }

    @Test
    public void testUpdatePurchaseOrderReturnCount_sameValueDifferentScale() {
        // mock 数据
        ErpPurchaseOrderItemDO item = new ErpPurchaseOrderItemDO().setId(10L).setProductId(100L)
                .setInCount(new BigDecimal("3")).setReturnCount(new BigDecimal("1.0"));
        when(purchaseOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));

        // 调用
        purchaseOrderService.updatePurchaseOrderReturnCount(1L, Map.of(10L, new BigDecimal("1.00")));
        // 断言：订单项数值未变化，不更新
        verify(purchaseOrderItemMapper, never()).updateById(any(ErpPurchaseOrderItemDO.class));
        verify(purchaseOrderMapper).updateById(any(ErpPurchaseOrderDO.class));
    }

    @Test
    public void testUpdatePurchaseOrderReturnCount_exceed() {
        // mock 数据
        ErpPurchaseOrderItemDO item = new ErpPurchaseOrderItemDO().setId(10L).setProductId(100L)
                .setInCount(new BigDecimal("3")).setReturnCount(new BigDecimal("1.0"));
        when(purchaseOrderItemMapper.selectListByOrderId(1L)).thenReturn(Collections.singletonList(item));
        when(productService.getProduct(100L)).thenReturn(new ErpProductDO().setId(100L).setName("商品"));

        // 调用，并断言
        assertServiceException(() -> purchaseOrderService.updatePurchaseOrderReturnCount(1L,
                Map.of(10L, new BigDecimal("3.5"))), PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED, "商品", item.getInCount());
        verify(purchaseOrderItemMapper, never()).updateById(any(ErpPurchaseOrderItemDO.class));
    }

}
