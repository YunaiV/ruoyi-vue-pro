package cn.iocoder.yudao.module.wms.service.order.shipment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.detail.WmsShipmentOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.shipment.WmsShipmentOrderDetailMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.order.shipment.WmsShipmentOrderMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsShipmentOrderTypeEnum;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.SHIPMENT_ORDER_DETAIL_REQUIRED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.SHIPMENT_ORDER_STATUS_NOT_DELETABLE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.SHIPMENT_ORDER_STATUS_NOT_PREPARE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Import({WmsShipmentOrderServiceImpl.class, WmsShipmentOrderDetailServiceImpl.class})
public class WmsShipmentOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsShipmentOrderServiceImpl shipmentOrderService;

    @Resource
    private WmsShipmentOrderMapper shipmentOrderMapper;
    @Resource
    private WmsShipmentOrderDetailMapper shipmentOrderDetailMapper;

    @MockitoBean
    private WmsWarehouseService warehouseService;
    @MockitoBean
    private WmsMerchantService merchantService;
    @MockitoBean
    private WmsItemSkuService itemSkuService;
    @MockitoBean
    private WmsInventoryService inventoryService;

    @Test
    public void testCreateShipmentOrder_calculateTotal() {
        // mock 数据
        WmsShipmentOrderSaveReqVO reqVO = createShipmentOrderSaveReqVO(null,
                createShipmentOrderDetailReqVO(null, 2001L, "1.50", "10.00", "16.00"),
                createShipmentOrderDetailReqVO(null, 2002L, "2.50", "20.00", "51.00"));

        // 调用
        Long orderId = shipmentOrderService.createShipmentOrder(reqVO);

        // 断言
        WmsShipmentOrderDO dbOrder = shipmentOrderMapper.selectById(orderId);
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("4.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("67.00").compareTo(dbOrder.getTotalPrice()));
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(0, new BigDecimal("16.00").compareTo(details.get(0).getTotalPrice()));
    }

    @Test
    public void testUpdateShipmentOrder_calculateTotal() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L);
        shipmentOrderMapper.insert(order);
        WmsShipmentOrderSaveReqVO reqVO = createShipmentOrderSaveReqVO(order.getId(),
                createShipmentOrderDetailReqVO(null, 2001L, "3.00", "30.00", "88.00"));

        // 调用
        shipmentOrderService.updateShipmentOrder(reqVO);

        // 断言
        WmsShipmentOrderDO dbOrder = shipmentOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("3.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("88.00").compareTo(dbOrder.getTotalPrice()));
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailMapper.selectListByOrderId(order.getId());
        assertEquals(1, details.size());
        assertEquals(0, new BigDecimal("88.00").compareTo(details.get(0).getTotalPrice()));
    }

    @Test
    public void testCompleteShipmentOrder_success() {
        // mock 数据
        Long warehouseId = 100L;
        Long skuId = 200L;
        WmsShipmentOrderDO order = createShipmentOrder(warehouseId);
        shipmentOrderMapper.insert(order);
        shipmentOrderDetailMapper.insert(createShipmentOrderDetail(order.getId(), skuId, warehouseId));

        // 调用
        shipmentOrderService.completeShipmentOrder(order.getId());

        // 断言：出库单
        WmsShipmentOrderDO dbOrder = shipmentOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.FINISHED.getStatus(), dbOrder.getStatus());
        // 断言：库存变更
        ArgumentCaptor<WmsInventoryChangeReqDTO> captor = ArgumentCaptor.forClass(WmsInventoryChangeReqDTO.class);
        verify(inventoryService).changeInventory(captor.capture());
        WmsInventoryChangeReqDTO inventoryReqDTO = captor.getValue();
        assertEquals(order.getId(), inventoryReqDTO.getOrderId());
        assertEquals(order.getNo(), inventoryReqDTO.getOrderNo());
        assertEquals(WmsOrderTypeEnum.SHIPMENT.getType(), inventoryReqDTO.getOrderType());
        assertEquals(1, inventoryReqDTO.getItems().size());
        assertEquals(skuId, inventoryReqDTO.getItems().get(0).getSkuId());
        assertEquals(warehouseId, inventoryReqDTO.getItems().get(0).getWarehouseId());
        assertEquals(0, new BigDecimal("-2.00").compareTo(inventoryReqDTO.getItems().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("-40.00").compareTo(inventoryReqDTO.getItems().get(0).getTotalPrice()));
    }

    @Test
    public void testCompleteShipmentOrder_detailRequired() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L);
        shipmentOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> shipmentOrderService.completeShipmentOrder(order.getId()),
                SHIPMENT_ORDER_DETAIL_REQUIRED);
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testCompleteShipmentOrder_duplicateComplete() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L);
        shipmentOrderMapper.insert(order);
        shipmentOrderDetailMapper.insert(createShipmentOrderDetail(order.getId(), 200L, 100L));

        // 调用
        shipmentOrderService.completeShipmentOrder(order.getId());

        // 调用，并断言：二次完成不能再次写库存
        assertServiceException(() -> shipmentOrderService.completeShipmentOrder(order.getId()),
                SHIPMENT_ORDER_STATUS_NOT_PREPARE);
        verify(inventoryService).changeInventory(any());
    }

    @Test
    public void testCancelShipmentOrder_success() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L);
        shipmentOrderMapper.insert(order);

        // 调用
        shipmentOrderService.cancelShipmentOrder(order.getId());

        // 断言
        WmsShipmentOrderDO dbOrder = shipmentOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.CANCELED.getStatus(), dbOrder.getStatus());
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testUpdateByIdAndStatus_statusNotMatch() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L);
        shipmentOrderMapper.insert(order);

        // 调用
        int updateCount = shipmentOrderMapper.updateByIdAndStatus(order.getId(),
                WmsOrderStatusEnum.FINISHED.getStatus(),
                new WmsShipmentOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus()));

        // 断言
        assertEquals(0, updateCount);
        assertEquals(WmsOrderStatusEnum.PREPARE.getStatus(),
                shipmentOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    public void testDeleteShipmentOrder_canceled() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L).setStatus(WmsOrderStatusEnum.CANCELED.getStatus());
        shipmentOrderMapper.insert(order);
        shipmentOrderDetailMapper.insert(createShipmentOrderDetail(order.getId(), 200L, 100L));

        // 调用
        shipmentOrderService.deleteShipmentOrder(order.getId());

        // 断言
        assertNull(shipmentOrderMapper.selectById(order.getId()));
        assertEquals(0, shipmentOrderDetailMapper.selectListByOrderId(order.getId()).size());
    }

    @Test
    public void testDeleteShipmentOrder_finished() {
        // mock 数据
        WmsShipmentOrderDO order = createShipmentOrder(100L).setStatus(WmsOrderStatusEnum.FINISHED.getStatus());
        shipmentOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> shipmentOrderService.deleteShipmentOrder(order.getId()),
                SHIPMENT_ORDER_STATUS_NOT_DELETABLE);
    }

    private static WmsShipmentOrderDO createShipmentOrder(Long warehouseId) {
        return new WmsShipmentOrderDO()
                .setNo("CK202605120001")
                .setType(WmsShipmentOrderTypeEnum.SALE.getType())
                .setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0))
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus())
                .setWarehouseId(warehouseId)
                .setTotalQuantity(new BigDecimal("2.00"))
                .setTotalPrice(new BigDecimal("20.00"));
    }

    private static WmsShipmentOrderSaveReqVO createShipmentOrderSaveReqVO(Long id,
                                                                          WmsShipmentOrderDetailSaveReqVO... details) {
        WmsShipmentOrderSaveReqVO reqVO = new WmsShipmentOrderSaveReqVO();
        reqVO.setId(id);
        reqVO.setNo("CK202605120001");
        reqVO.setType(WmsShipmentOrderTypeEnum.SALE.getType());
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setWarehouseId(100L);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsShipmentOrderDetailSaveReqVO createShipmentOrderDetailReqVO(Long id, Long skuId,
                                                                                  String quantity, String price) {
        return createShipmentOrderDetailReqVO(id, skuId, quantity, price, null);
    }

    private static WmsShipmentOrderDetailSaveReqVO createShipmentOrderDetailReqVO(Long id, Long skuId,
                                                                                  String quantity, String price,
                                                                                  String totalPrice) {
        WmsShipmentOrderDetailSaveReqVO reqVO = new WmsShipmentOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setPrice(new BigDecimal(price));
        if (totalPrice != null) {
            reqVO.setTotalPrice(new BigDecimal(totalPrice));
        }
        return reqVO;
    }

    private static WmsShipmentOrderDetailDO createShipmentOrderDetail(Long orderId, Long skuId, Long warehouseId) {
        return WmsShipmentOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .warehouseId(warehouseId)
                .quantity(new BigDecimal("2.00"))
                .price(new BigDecimal("20.00"))
                .totalPrice(new BigDecimal("40.00"))
                .build();
    }

}
