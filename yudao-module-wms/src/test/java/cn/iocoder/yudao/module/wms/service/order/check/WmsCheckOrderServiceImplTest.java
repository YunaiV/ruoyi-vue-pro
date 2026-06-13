package cn.iocoder.yudao.module.wms.service.order.check;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.detail.WmsCheckOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.check.WmsCheckOrderDetailMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.order.check.WmsCheckOrderMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryCheckReqDTO;
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
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.CHECK_ORDER_DETAIL_REQUIRED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.CHECK_ORDER_INVENTORY_CHANGED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.CHECK_ORDER_STATUS_NOT_DELETABLE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.CHECK_ORDER_STATUS_NOT_PREPARE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Import({WmsCheckOrderServiceImpl.class, WmsCheckOrderDetailServiceImpl.class})
public class WmsCheckOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsCheckOrderServiceImpl checkOrderService;

    @Resource
    private WmsCheckOrderMapper checkOrderMapper;
    @Resource
    private WmsCheckOrderDetailMapper checkOrderDetailMapper;

    @MockitoBean
    private WmsWarehouseService warehouseService;
    @MockitoBean
    private WmsItemSkuService itemSkuService;
    @MockitoBean
    private WmsInventoryService inventoryService;

    @Test
    public void testCreateCheckOrder_calculateTotals() {
        // mock 数据
        WmsCheckOrderSaveReqVO reqVO = createCheckOrderReqVO(100L,
                createCheckOrderDetailReqVO(null, 200L, "10.00", "7.00", "5.00"),
                createCheckOrderDetailReqVO(null, 201L, "20.00", "25.00", "2.00"));

        // 调用
        Long orderId = checkOrderService.createCheckOrder(reqVO);

        // 断言：盘库单汇总由后端计算
        WmsCheckOrderDO order = checkOrderMapper.selectById(orderId);
        assertNotNull(order);
        assertEquals(0, new BigDecimal("2.00").compareTo(order.getTotalQuantity()));
        assertEquals(0, new BigDecimal("90.00").compareTo(order.getTotalPrice()));
        assertEquals(0, new BigDecimal("85.00").compareTo(order.getActualPrice()));
        // 断言：盘库明细保存单价
        List<WmsCheckOrderDetailDO> details = checkOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(0, new BigDecimal("5.00").compareTo(details.get(0).getPrice()));
    }

    @Test
    public void testUpdateCheckOrder_calculateTotals() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L);
        checkOrderMapper.insert(order);
        WmsCheckOrderDetailDO oldDetail = createCheckOrderDetail(order.getId(), 200L, 100L, "10.00", "7.00");
        checkOrderDetailMapper.insert(oldDetail);
        WmsCheckOrderSaveReqVO reqVO = createCheckOrderReqVO(100L,
                createCheckOrderDetailReqVO(oldDetail.getId(), 200L, "8.00", "10.00", "6.00"),
                createCheckOrderDetailReqVO(null, 201L, "2.00", "1.00", "4.00"));
        reqVO.setId(order.getId());
        reqVO.setNo(order.getNo());

        // 调用
        checkOrderService.updateCheckOrder(reqVO);

        // 断言：盘库单汇总由后端重新计算
        WmsCheckOrderDO dbOrder = checkOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(0, new BigDecimal("1.00").compareTo(dbOrder.getTotalQuantity()));
        assertEquals(0, new BigDecimal("56.00").compareTo(dbOrder.getTotalPrice()));
        assertEquals(0, new BigDecimal("64.00").compareTo(dbOrder.getActualPrice()));
        List<WmsCheckOrderDetailDO> details = checkOrderDetailMapper.selectListByOrderId(order.getId());
        assertEquals(2, details.size());
    }

    @Test
    public void testCompleteCheckOrder_success() {
        // mock 数据
        Long warehouseId = 100L;
        Long skuId = 200L;
        WmsCheckOrderDO order = createCheckOrder(warehouseId);
        checkOrderMapper.insert(order);
        checkOrderDetailMapper.insert(createCheckOrderDetail(order.getId(), skuId, warehouseId,
                "10.00", "7.00"));

        // 调用
        checkOrderService.completeCheckOrder(order.getId());

        // 断言：盘库单
        WmsCheckOrderDO dbOrder = checkOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.FINISHED.getStatus(), dbOrder.getStatus());
        // 断言：库存盘点
        ArgumentCaptor<WmsInventoryCheckReqDTO> captor = ArgumentCaptor.forClass(WmsInventoryCheckReqDTO.class);
        verify(inventoryService).checkInventory(captor.capture());
        WmsInventoryCheckReqDTO inventoryReqDTO = captor.getValue();
        assertEquals(order.getId(), inventoryReqDTO.getOrderId());
        assertEquals(order.getNo(), inventoryReqDTO.getOrderNo());
        assertEquals(WmsOrderTypeEnum.CHECK.getType(), inventoryReqDTO.getOrderType());
        assertEquals(1, inventoryReqDTO.getItems().size());
        assertEquals(skuId, inventoryReqDTO.getItems().get(0).getSkuId());
        assertEquals(warehouseId, inventoryReqDTO.getItems().get(0).getWarehouseId());
        assertEquals(300L, inventoryReqDTO.getItems().get(0).getInventoryId());
        assertEquals(0, new BigDecimal("10.00").compareTo(inventoryReqDTO.getItems().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("7.00").compareTo(inventoryReqDTO.getItems().get(0).getCheckQuantity()));
        assertEquals(0, new BigDecimal("30.00").compareTo(inventoryReqDTO.getItems().get(0).getPrice()));
    }

    @Test
    public void testCompleteCheckOrder_zeroDifference() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L);
        checkOrderMapper.insert(order);
        checkOrderDetailMapper.insert(createCheckOrderDetail(order.getId(), 200L, 100L,
                "10.00", "10.00"));

        // 调用
        checkOrderService.completeCheckOrder(order.getId());

        // 断言
        WmsCheckOrderDO dbOrder = checkOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.FINISHED.getStatus(), dbOrder.getStatus());
        verify(inventoryService).checkInventory(any());
    }

    @Test
    public void testCompleteCheckOrder_newInventoryCurrentMissing() {
        // mock 数据
        Long warehouseId = 100L;
        Long skuId = 200L;
        WmsCheckOrderDO order = createCheckOrder(warehouseId);
        checkOrderMapper.insert(order);
        checkOrderDetailMapper.insert(createNewInventoryCheckOrderDetail(order.getId(), skuId, warehouseId,
                "0.00", "5.00"));
        // 调用
        checkOrderService.completeCheckOrder(order.getId());

        // 断言
        ArgumentCaptor<WmsInventoryCheckReqDTO> captor = ArgumentCaptor.forClass(WmsInventoryCheckReqDTO.class);
        verify(inventoryService).checkInventory(captor.capture());
        WmsInventoryCheckReqDTO inventoryReqDTO = captor.getValue();
        assertEquals(1, inventoryReqDTO.getItems().size());
        assertEquals(skuId, inventoryReqDTO.getItems().get(0).getSkuId());
        assertEquals(warehouseId, inventoryReqDTO.getItems().get(0).getWarehouseId());
        assertNull(inventoryReqDTO.getItems().get(0).getInventoryId());
        assertEquals(0, new BigDecimal("0.00").compareTo(inventoryReqDTO.getItems().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(inventoryReqDTO.getItems().get(0).getCheckQuantity()));
    }

    @Test
    public void testCompleteCheckOrder_checkInventoryChanged() {
        // mock 数据
        Long warehouseId = 100L;
        Long skuId = 200L;
        WmsCheckOrderDO order = createCheckOrder(warehouseId);
        checkOrderMapper.insert(order);
        checkOrderDetailMapper.insert(createNewInventoryCheckOrderDetail(order.getId(), skuId, warehouseId,
                "0.00", "5.00"));
        doThrow(exception(CHECK_ORDER_INVENTORY_CHANGED)).when(inventoryService).checkInventory(any());

        // 调用，并断言
        assertServiceException(() -> checkOrderService.completeCheckOrder(order.getId()),
                CHECK_ORDER_INVENTORY_CHANGED);
        assertEquals(WmsOrderStatusEnum.PREPARE.getStatus(),
                checkOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    public void testCompleteCheckOrder_detailRequired() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L);
        checkOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> checkOrderService.completeCheckOrder(order.getId()),
                CHECK_ORDER_DETAIL_REQUIRED);
        verify(inventoryService, never()).checkInventory(any());
    }

    @Test
    public void testCompleteCheckOrder_duplicateComplete() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L);
        checkOrderMapper.insert(order);
        checkOrderDetailMapper.insert(createCheckOrderDetail(order.getId(), 200L, 100L,
                "10.00", "7.00"));

        // 调用
        checkOrderService.completeCheckOrder(order.getId());

        // 调用，并断言：二次完成不能再次写库存
        assertServiceException(() -> checkOrderService.completeCheckOrder(order.getId()),
                CHECK_ORDER_STATUS_NOT_PREPARE);
        verify(inventoryService).checkInventory(any());
    }

    @Test
    public void testCancelCheckOrder_success() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L);
        checkOrderMapper.insert(order);

        // 调用
        checkOrderService.cancelCheckOrder(order.getId());

        // 断言
        WmsCheckOrderDO dbOrder = checkOrderMapper.selectById(order.getId());
        assertNotNull(dbOrder);
        assertEquals(WmsOrderStatusEnum.CANCELED.getStatus(), dbOrder.getStatus());
        verify(inventoryService, never()).checkInventory(any());
    }

    @Test
    public void testUpdateByIdAndStatus_statusNotMatch() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L);
        checkOrderMapper.insert(order);

        // 调用
        int updateCount = checkOrderMapper.updateByIdAndStatus(order.getId(),
                WmsOrderStatusEnum.FINISHED.getStatus(),
                new WmsCheckOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus()));

        // 断言
        assertEquals(0, updateCount);
        assertEquals(WmsOrderStatusEnum.PREPARE.getStatus(),
                checkOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    public void testDeleteCheckOrder_canceled() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L).setStatus(WmsOrderStatusEnum.CANCELED.getStatus());
        checkOrderMapper.insert(order);
        checkOrderDetailMapper.insert(createCheckOrderDetail(order.getId(), 200L, 100L,
                "10.00", "8.00"));

        // 调用
        checkOrderService.deleteCheckOrder(order.getId());

        // 断言
        assertNull(checkOrderMapper.selectById(order.getId()));
        assertEquals(0, checkOrderDetailMapper.selectListByOrderId(order.getId()).size());
    }

    @Test
    public void testDeleteCheckOrder_finished() {
        // mock 数据
        WmsCheckOrderDO order = createCheckOrder(100L).setStatus(WmsOrderStatusEnum.FINISHED.getStatus());
        checkOrderMapper.insert(order);

        // 调用，并断言
        assertServiceException(() -> checkOrderService.deleteCheckOrder(order.getId()),
                CHECK_ORDER_STATUS_NOT_DELETABLE);
    }

    private static WmsCheckOrderDO createCheckOrder(Long warehouseId) {
        return new WmsCheckOrderDO()
                .setNo("PK202605120001")
                .setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0))
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus())
                .setWarehouseId(warehouseId)
                .setTotalQuantity(new BigDecimal("-3.00"))
                .setTotalPrice(new BigDecimal("300.00"))
                .setActualPrice(new BigDecimal("210.00"));
    }

    private static WmsCheckOrderDetailDO createCheckOrderDetail(Long orderId, Long skuId, Long warehouseId,
                                                               String quantity, String checkQuantity) {
        return WmsCheckOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .warehouseId(warehouseId)
                .inventoryId(300L)
                .quantity(new BigDecimal(quantity))
                .checkQuantity(new BigDecimal(checkQuantity))
                .price(new BigDecimal("30.00"))
                .build();
    }

    private static WmsCheckOrderDetailDO createNewInventoryCheckOrderDetail(Long orderId, Long skuId, Long warehouseId,
                                                                           String quantity, String checkQuantity) {
        return WmsCheckOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .warehouseId(warehouseId)
                .quantity(new BigDecimal(quantity))
                .checkQuantity(new BigDecimal(checkQuantity))
                .price(new BigDecimal("30.00"))
                .build();
    }

    private static WmsCheckOrderSaveReqVO createCheckOrderReqVO(Long warehouseId, WmsCheckOrderDetailSaveReqVO... details) {
        WmsCheckOrderSaveReqVO reqVO = new WmsCheckOrderSaveReqVO();
        reqVO.setNo("PK202605120002");
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setWarehouseId(warehouseId);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsCheckOrderDetailSaveReqVO createCheckOrderDetailReqVO(Long id, Long skuId,
                                                                           String quantity, String checkQuantity,
                                                                           String price) {
        WmsCheckOrderDetailSaveReqVO reqVO = new WmsCheckOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setInventoryId(300L);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setCheckQuantity(new BigDecimal(checkQuantity));
        reqVO.setPrice(new BigDecimal(price));
        return reqVO;
    }

}
