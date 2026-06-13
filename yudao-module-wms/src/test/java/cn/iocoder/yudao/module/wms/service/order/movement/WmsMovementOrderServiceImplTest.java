package cn.iocoder.yudao.module.wms.service.order.movement;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.detail.WmsMovementOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.movement.WmsMovementOrderDetailMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.order.movement.WmsMovementOrderMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.MOVEMENT_ORDER_DETAIL_REQUIRED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.MOVEMENT_ORDER_STATUS_NOT_DELETABLE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.MOVEMENT_ORDER_STATUS_NOT_PREPARE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.MOVEMENT_ORDER_WAREHOUSE_SAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Import({WmsMovementOrderServiceImpl.class, WmsMovementOrderDetailServiceImpl.class})
public class WmsMovementOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsMovementOrderServiceImpl movementOrderService;

    @Resource
    private WmsMovementOrderMapper movementOrderMapper;
    @Resource
    private WmsMovementOrderDetailMapper movementOrderDetailMapper;

    @MockitoBean
    private WmsWarehouseService warehouseService;
    @MockitoBean
    private WmsItemSkuService itemSkuService;
    @MockitoBean
    private WmsInventoryService inventoryService;

    @Test
    public void testCreateMovementOrder_calculateTotal() {
        // mock 数据
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(null, 100L, 200L,
                createMovementOrderDetailReqVO(null, 3001L, "1.50", "10.00", "16.00"),
                createMovementOrderDetailReqVO(null, 3002L, "2.50", "20.00", "51.00"));

        // 调用
        Long orderId = movementOrderService.createMovementOrder(reqVO);

        // 断言
        WmsMovementOrderDO dbOrder = movementOrderMapper.selectById(orderId);
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("4.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("67.00").compareTo(dbOrder.getTotalPrice()));
        List<WmsMovementOrderDetailDO> details = movementOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(0, new BigDecimal("16.00").compareTo(details.get(0).getTotalPrice()));
    }

    @Test
    public void testUpdateMovementOrder_calculateTotal() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L);
        movementOrderMapper.insert(order);
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(order.getId(), 100L, 200L,
                createMovementOrderDetailReqVO(null, 3001L, "3.00", "30.00", "88.00"));

        // 调用
        movementOrderService.updateMovementOrder(reqVO);

        // 断言
        WmsMovementOrderDO dbOrder = movementOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("3.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("88.00").compareTo(dbOrder.getTotalPrice()));
        List<WmsMovementOrderDetailDO> details = movementOrderDetailMapper.selectListByOrderId(order.getId());
        assertEquals(1, details.size());
        assertEquals(0, new BigDecimal("88.00").compareTo(details.get(0).getTotalPrice()));
    }

    @Test
    public void testCompleteMovementOrder_success() {
        // mock 数据
        Long sourceWarehouseId = 100L;
        Long targetWarehouseId = 200L;
        Long skuId = 300L;
        WmsMovementOrderDO order = createMovementOrder(sourceWarehouseId, targetWarehouseId);
        movementOrderMapper.insert(order);
        movementOrderDetailMapper.insert(createMovementOrderDetail(order.getId(), skuId,
                sourceWarehouseId, targetWarehouseId));

        // 调用
        movementOrderService.completeMovementOrder(order.getId());

        // 断言：移库单
        WmsMovementOrderDO dbOrder = movementOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.FINISHED.getStatus(), dbOrder.getStatus());
        // 断言：库存变更
        ArgumentCaptor<WmsInventoryChangeReqDTO> captor = ArgumentCaptor.forClass(WmsInventoryChangeReqDTO.class);
        verify(inventoryService).changeInventory(captor.capture());
        WmsInventoryChangeReqDTO inventoryReqDTO = captor.getValue();
        assertEquals(order.getId(), inventoryReqDTO.getOrderId());
        assertEquals(order.getNo(), inventoryReqDTO.getOrderNo());
        assertEquals(WmsOrderTypeEnum.MOVEMENT.getType(), inventoryReqDTO.getOrderType());
        assertEquals(2, inventoryReqDTO.getItems().size());
        assertEquals(skuId, inventoryReqDTO.getItems().get(0).getSkuId());
        assertEquals(sourceWarehouseId, inventoryReqDTO.getItems().get(0).getWarehouseId());
        assertEquals(0, new BigDecimal("-2.00").compareTo(inventoryReqDTO.getItems().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("-40.00").compareTo(inventoryReqDTO.getItems().get(0).getTotalPrice()));
        assertEquals(skuId, inventoryReqDTO.getItems().get(1).getSkuId());
        assertEquals(targetWarehouseId, inventoryReqDTO.getItems().get(1).getWarehouseId());
        assertEquals(0, new BigDecimal("2.00").compareTo(inventoryReqDTO.getItems().get(1).getQuantity()));
        assertEquals(0, new BigDecimal("40.00").compareTo(inventoryReqDTO.getItems().get(1).getTotalPrice()));
    }

    @Test
    public void testCompleteMovementOrder_detailRequired() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L);
        movementOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> movementOrderService.completeMovementOrder(order.getId()),
                MOVEMENT_ORDER_DETAIL_REQUIRED);
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testCompleteMovementOrder_duplicateComplete() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L);
        movementOrderMapper.insert(order);
        movementOrderDetailMapper.insert(createMovementOrderDetail(order.getId(), 300L, 100L, 200L));

        // 调用
        movementOrderService.completeMovementOrder(order.getId());

        // 调用，并断言：二次完成不能再次写库存
        assertServiceException(() -> movementOrderService.completeMovementOrder(order.getId()),
                MOVEMENT_ORDER_STATUS_NOT_PREPARE);
        verify(inventoryService).changeInventory(any());
    }

    @Test
    public void testCreateMovementOrder_sameWarehouse() {
        // 准备参数
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(100L, 100L);

        // 调用，并断言
        assertServiceException(() -> movementOrderService.createMovementOrder(reqVO), MOVEMENT_ORDER_WAREHOUSE_SAME);
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testCancelMovementOrder_success() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L);
        movementOrderMapper.insert(order);

        // 调用
        movementOrderService.cancelMovementOrder(order.getId());

        // 断言
        WmsMovementOrderDO dbOrder = movementOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.CANCELED.getStatus(), dbOrder.getStatus());
        verify(inventoryService, never()).changeInventory(any());
    }

    @Test
    public void testUpdateByIdAndStatus_statusNotMatch() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L);
        movementOrderMapper.insert(order);

        // 调用
        int updateCount = movementOrderMapper.updateByIdAndStatus(order.getId(),
                WmsOrderStatusEnum.FINISHED.getStatus(),
                new WmsMovementOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus()));

        // 断言
        assertEquals(0, updateCount);
        assertEquals(WmsOrderStatusEnum.PREPARE.getStatus(),
                movementOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    public void testDeleteMovementOrder_canceled() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L).setStatus(WmsOrderStatusEnum.CANCELED.getStatus());
        movementOrderMapper.insert(order);
        movementOrderDetailMapper.insert(createMovementOrderDetail(order.getId(), 300L, 100L, 200L));

        // 调用
        movementOrderService.deleteMovementOrder(order.getId());

        // 断言
        assertNull(movementOrderMapper.selectById(order.getId()));
        assertEquals(0, movementOrderDetailMapper.selectListByOrderId(order.getId()).size());
    }

    @Test
    public void testDeleteMovementOrder_finished() {
        // mock 数据
        WmsMovementOrderDO order = createMovementOrder(100L, 200L).setStatus(WmsOrderStatusEnum.FINISHED.getStatus());
        movementOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> movementOrderService.deleteMovementOrder(order.getId()),
                MOVEMENT_ORDER_STATUS_NOT_DELETABLE);
    }

    private static WmsMovementOrderDO createMovementOrder(Long sourceWarehouseId, Long targetWarehouseId) {
        return new WmsMovementOrderDO()
                .setNo("YK202605120001")
                .setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0))
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus())
                .setSourceWarehouseId(sourceWarehouseId)
                .setTargetWarehouseId(targetWarehouseId)
                .setTotalQuantity(new BigDecimal("2.00"))
                .setTotalPrice(new BigDecimal("20.00"));
    }

    private static WmsMovementOrderDetailDO createMovementOrderDetail(Long orderId, Long skuId,
                                                                      Long sourceWarehouseId, Long targetWarehouseId) {
        return WmsMovementOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .sourceWarehouseId(sourceWarehouseId)
                .targetWarehouseId(targetWarehouseId)
                .quantity(new BigDecimal("2.00"))
                .price(new BigDecimal("20.00"))
                .totalPrice(new BigDecimal("40.00"))
                .build();
    }

    private static WmsMovementOrderSaveReqVO createMovementOrderReqVO(Long sourceWarehouseId, Long targetWarehouseId) {
        return createMovementOrderReqVO(null, sourceWarehouseId, targetWarehouseId);
    }

    private static WmsMovementOrderSaveReqVO createMovementOrderReqVO(Long id, Long sourceWarehouseId,
                                                                      Long targetWarehouseId,
                                                                      WmsMovementOrderDetailSaveReqVO... details) {
        WmsMovementOrderSaveReqVO reqVO = new WmsMovementOrderSaveReqVO();
        reqVO.setId(id);
        reqVO.setNo("YK202605120001");
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setSourceWarehouseId(sourceWarehouseId);
        reqVO.setTargetWarehouseId(targetWarehouseId);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsMovementOrderDetailSaveReqVO createMovementOrderDetailReqVO(Long id, Long skuId,
                                                                                  String quantity, String price) {
        return createMovementOrderDetailReqVO(id, skuId, quantity, price, null);
    }

    private static WmsMovementOrderDetailSaveReqVO createMovementOrderDetailReqVO(Long id, Long skuId,
                                                                                  String quantity, String price,
                                                                                  String totalPrice) {
        WmsMovementOrderDetailSaveReqVO reqVO = new WmsMovementOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setPrice(new BigDecimal(price));
        if (totalPrice != null) {
            reqVO.setTotalPrice(new BigDecimal(totalPrice));
        }
        return reqVO;
    }

}
