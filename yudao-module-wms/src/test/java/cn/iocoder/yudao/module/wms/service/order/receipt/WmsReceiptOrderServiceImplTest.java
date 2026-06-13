package cn.iocoder.yudao.module.wms.service.order.receipt;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.detail.WmsReceiptOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.receipt.WmsReceiptOrderDetailMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.order.receipt.WmsReceiptOrderMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsReceiptOrderTypeEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.merchant.WmsMerchantService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.RECEIPT_ORDER_DETAIL_REQUIRED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.RECEIPT_ORDER_STATUS_NOT_PREPARE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.RECEIPT_ORDER_STATUS_NOT_DELETABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Import({WmsReceiptOrderServiceImpl.class, WmsReceiptOrderDetailServiceImpl.class})
public class WmsReceiptOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsReceiptOrderServiceImpl receiptOrderService;

    @Resource
    private WmsReceiptOrderMapper receiptOrderMapper;
    @Resource
    private WmsReceiptOrderDetailMapper receiptOrderDetailMapper;

    @MockitoBean
    private WmsWarehouseService warehouseService;
    @MockitoBean
    private WmsMerchantService merchantService;
    @MockitoBean
    private WmsItemSkuService itemSkuService;
    @MockitoBean
    private WmsInventoryService inventoryService;

    @Test
    public void testCreateReceiptOrder_calculateTotal() {
        // mock 数据
        WmsReceiptOrderSaveReqVO reqVO = createReceiptOrderSaveReqVO(null,
                createReceiptOrderDetailReqVO(null, 2001L, "1.50", "10.00", "16.00"),
                createReceiptOrderDetailReqVO(null, 2002L, "2.50", "20.00", "51.00"));

        // 调用
        Long orderId = receiptOrderService.createReceiptOrder(reqVO);

        // 断言
        WmsReceiptOrderDO dbOrder = receiptOrderMapper.selectById(orderId);
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("4.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("67.00").compareTo(dbOrder.getTotalPrice()));
        List<WmsReceiptOrderDetailDO> details = receiptOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(0, new BigDecimal("16.00").compareTo(details.get(0).getTotalPrice()));
    }

    @Test
    public void testUpdateReceiptOrder_calculateTotal() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L);
        receiptOrderMapper.insert(order);
        WmsReceiptOrderSaveReqVO reqVO = createReceiptOrderSaveReqVO(order.getId(),
                createReceiptOrderDetailReqVO(null, 2001L, "3.00", "30.00", "88.00"));

        // 调用
        receiptOrderService.updateReceiptOrder(reqVO);

        // 断言
        WmsReceiptOrderDO dbOrder = receiptOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("3.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("88.00").compareTo(dbOrder.getTotalPrice()));
        List<WmsReceiptOrderDetailDO> details = receiptOrderDetailMapper.selectListByOrderId(order.getId());
        assertEquals(1, details.size());
        assertEquals(0, new BigDecimal("88.00").compareTo(details.get(0).getTotalPrice()));
    }

    @Test
    public void testCompleteReceiptOrder_success() {
        // mock 数据
        Long warehouseId = 100L;
        Long skuId = 200L;
        WmsReceiptOrderDO order = createReceiptOrder(warehouseId);
        receiptOrderMapper.insert(order);
        receiptOrderDetailMapper.insert(createReceiptOrderDetail(order.getId(), skuId, warehouseId));

        // 调用
        receiptOrderService.completeReceiptOrder(order.getId());

        // 断言：入库单
        WmsReceiptOrderDO dbOrder = receiptOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.FINISHED.getStatus(), dbOrder.getStatus());
        // 断言：库存变更
        ArgumentCaptor<WmsInventoryChangeReqDTO> captor = ArgumentCaptor.forClass(WmsInventoryChangeReqDTO.class);
        verify(inventoryService).changeInventory(captor.capture());
        WmsInventoryChangeReqDTO inventoryReqDTO = captor.getValue();
        assertEquals(order.getId(), inventoryReqDTO.getOrderId());
        assertEquals(order.getNo(), inventoryReqDTO.getOrderNo());
        assertEquals(WmsOrderTypeEnum.RECEIPT.getType(), inventoryReqDTO.getOrderType());
        assertEquals(1, inventoryReqDTO.getItems().size());
        assertEquals(skuId, inventoryReqDTO.getItems().get(0).getSkuId());
        assertEquals(warehouseId, inventoryReqDTO.getItems().get(0).getWarehouseId());
        assertEquals(0, new BigDecimal("2.00").compareTo(inventoryReqDTO.getItems().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("40.00").compareTo(inventoryReqDTO.getItems().get(0).getTotalPrice()));
    }

    @Test
    public void testCompleteReceiptOrder_detailRequired() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L);
        receiptOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> receiptOrderService.completeReceiptOrder(order.getId()),
                RECEIPT_ORDER_DETAIL_REQUIRED);
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testCompleteReceiptOrder_duplicateComplete() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L);
        receiptOrderMapper.insert(order);
        receiptOrderDetailMapper.insert(createReceiptOrderDetail(order.getId(), 200L, 100L));

        // 调用
        receiptOrderService.completeReceiptOrder(order.getId());

        // 调用，并断言：二次完成不能再次写库存
        assertServiceException(() -> receiptOrderService.completeReceiptOrder(order.getId()),
                RECEIPT_ORDER_STATUS_NOT_PREPARE);
        verify(inventoryService).changeInventory(any());
    }

    @Test
    public void testCancelReceiptOrder_success() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L);
        receiptOrderMapper.insert(order);

        // 调用
        receiptOrderService.cancelReceiptOrder(order.getId());

        // 断言
        WmsReceiptOrderDO dbOrder = receiptOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.CANCELED.getStatus(), dbOrder.getStatus());
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testUpdateByIdAndStatus_statusNotMatch() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L);
        receiptOrderMapper.insert(order);

        // 调用
        int updateCount = receiptOrderMapper.updateByIdAndStatus(order.getId(),
                WmsOrderStatusEnum.FINISHED.getStatus(),
                new WmsReceiptOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus()));

        // 断言
        assertEquals(0, updateCount);
        assertEquals(WmsOrderStatusEnum.PREPARE.getStatus(),
                receiptOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    public void testDeleteReceiptOrder_canceled() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L).setStatus(WmsOrderStatusEnum.CANCELED.getStatus());
        receiptOrderMapper.insert(order);
        receiptOrderDetailMapper.insert(createReceiptOrderDetail(order.getId(), 200L, 100L));

        // 调用
        receiptOrderService.deleteReceiptOrder(order.getId());

        // 断言
        assertNull(receiptOrderMapper.selectById(order.getId()));
        assertEquals(0, receiptOrderDetailMapper.selectListByOrderId(order.getId()).size());
    }

    @Test
    public void testDeleteReceiptOrder_finished() {
        // mock 数据
        WmsReceiptOrderDO order = createReceiptOrder(100L).setStatus(WmsOrderStatusEnum.FINISHED.getStatus());
        receiptOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> receiptOrderService.deleteReceiptOrder(order.getId()),
                RECEIPT_ORDER_STATUS_NOT_DELETABLE);
    }

    private static WmsReceiptOrderDO createReceiptOrder(Long warehouseId) {
        return new WmsReceiptOrderDO()
                .setNo("RK202605120001")
                .setType(WmsReceiptOrderTypeEnum.PURCHASE.getType())
                .setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0))
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus())
                .setWarehouseId(warehouseId)
                .setTotalQuantity(new BigDecimal("2.00"))
                .setTotalPrice(new BigDecimal("20.00"));
    }

    private static WmsReceiptOrderSaveReqVO createReceiptOrderSaveReqVO(Long id,
                                                                        WmsReceiptOrderDetailSaveReqVO... details) {
        WmsReceiptOrderSaveReqVO reqVO = new WmsReceiptOrderSaveReqVO();
        reqVO.setId(id);
        reqVO.setNo("RK202605120001");
        reqVO.setType(WmsReceiptOrderTypeEnum.PURCHASE.getType());
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setWarehouseId(100L);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsReceiptOrderDetailSaveReqVO createReceiptOrderDetailReqVO(Long id, Long skuId,
                                                                                String quantity, String price) {
        return createReceiptOrderDetailReqVO(id, skuId, quantity, price, null);
    }

    private static WmsReceiptOrderDetailSaveReqVO createReceiptOrderDetailReqVO(Long id, Long skuId,
                                                                                String quantity, String price,
                                                                                String totalPrice) {
        WmsReceiptOrderDetailSaveReqVO reqVO = new WmsReceiptOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setPrice(new BigDecimal(price));
        if (totalPrice != null) {
            reqVO.setTotalPrice(new BigDecimal(totalPrice));
        }
        return reqVO;
    }

    private static WmsReceiptOrderDetailDO createReceiptOrderDetail(Long orderId, Long skuId, Long warehouseId) {
        return WmsReceiptOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .warehouseId(warehouseId)
                .quantity(new BigDecimal("2.00"))
                .price(new BigDecimal("20.00"))
                .totalPrice(new BigDecimal("40.00"))
                .build();
    }

}
