package cn.iocoder.yudao.module.wms.service.order.movement;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.detail.WmsMovementOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.movement.WmsMovementOrderDetailMapper;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.MOVEMENT_ORDER_DETAIL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Import(WmsMovementOrderDetailServiceImpl.class)
public class WmsMovementOrderDetailServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsMovementOrderDetailServiceImpl movementOrderDetailService;

    @Resource
    private WmsMovementOrderDetailMapper movementOrderDetailMapper;

    @MockitoBean
    private WmsItemSkuService itemSkuService;

    @Test
    public void testCreateMovementOrderDetailList_success() {
        // mock 数据
        Long orderId = 10L;
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(
                createMovementOrderDetailReqVO(null, 1001L, "1.00"),
                createMovementOrderDetailReqVO(null, 1002L, "2.00"));

        // 调用
        movementOrderDetailService.createMovementOrderDetailList(orderId, reqVO);

        // 断言
        List<WmsMovementOrderDetailDO> details = movementOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(orderId, details.get(0).getOrderId());
        assertEquals(orderId, details.get(1).getOrderId());
        assertEquals(100L, details.get(0).getSourceWarehouseId());
        assertEquals(200L, details.get(0).getTargetWarehouseId());
        assertEquals(0, new BigDecimal("100.00").compareTo(details.get(0).getTotalPrice()));
        assertEquals(0, new BigDecimal("200.00").compareTo(details.get(1).getTotalPrice()));
    }

    @Test
    public void testCreateMovementOrderDetailList_ignoreId() {
        // mock 数据
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(
                createMovementOrderDetailReqVO(999L, 1001L, "1.00"));

        // 调用
        movementOrderDetailService.createMovementOrderDetailList(10L, reqVO);

        // 断言
        List<WmsMovementOrderDetailDO> details = movementOrderDetailMapper.selectListByOrderId(10L);
        assertEquals(1, details.size());
        assertNotNull(details.get(0).getId());
        assertEquals(1001L, details.get(0).getSkuId());
    }

    @Test
    public void testUpdateMovementOrderDetailList_diff() {
        // mock 数据
        Long orderId = 10L;
        WmsMovementOrderDetailDO detail01 = createMovementOrderDetail(orderId, 1001L, "1.00");
        WmsMovementOrderDetailDO detail02 = createMovementOrderDetail(orderId, 1002L, "2.00");
        movementOrderDetailMapper.insert(detail01);
        movementOrderDetailMapper.insert(detail02);
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(
                createMovementOrderDetailReqVO(detail01.getId(), 2001L, "11.00"),
                createMovementOrderDetailReqVO(null, 2002L, "22.00"));

        // 调用
        movementOrderDetailService.updateMovementOrderDetailList(orderId, reqVO);

        // 断言：修改
        WmsMovementOrderDetailDO dbUpdateDetail = movementOrderDetailMapper.selectById(detail01.getId());
        assertNotNull(dbUpdateDetail);
        assertEquals(orderId, dbUpdateDetail.getOrderId());
        assertEquals(2001L, dbUpdateDetail.getSkuId());
        assertEquals(0, new BigDecimal("11.00").compareTo(dbUpdateDetail.getQuantity()));
        assertEquals(0, new BigDecimal("1100.00").compareTo(dbUpdateDetail.getTotalPrice()));
        assertEquals(100L, dbUpdateDetail.getSourceWarehouseId());
        assertEquals(200L, dbUpdateDetail.getTargetWarehouseId());
        // 断言：新增
        List<WmsMovementOrderDetailDO> details = movementOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        WmsMovementOrderDetailDO dbCreateDetail = details.stream()
                .filter(detail -> Long.valueOf(2002L).equals(detail.getSkuId()))
                .findFirst().orElse(null);
        assertNotNull(dbCreateDetail);
        assertEquals(orderId, dbCreateDetail.getOrderId());
        assertEquals(0, new BigDecimal("22.00").compareTo(dbCreateDetail.getQuantity()));
        assertEquals(0, new BigDecimal("2200.00").compareTo(dbCreateDetail.getTotalPrice()));
        // 断言：删除
        assertNull(movementOrderDetailMapper.selectById(detail02.getId()));
    }

    @Test
    public void testUpdateMovementOrderDetailList_detailNotExists() {
        // mock 数据
        WmsMovementOrderSaveReqVO reqVO = createMovementOrderReqVO(
                createMovementOrderDetailReqVO(999L, 1001L, "1.00"));

        // 调用，并断言
        assertServiceException(() -> movementOrderDetailService.updateMovementOrderDetailList(10L, reqVO),
                MOVEMENT_ORDER_DETAIL_NOT_EXISTS);
    }

    private static WmsMovementOrderSaveReqVO createMovementOrderReqVO(WmsMovementOrderDetailSaveReqVO... details) {
        WmsMovementOrderSaveReqVO reqVO = new WmsMovementOrderSaveReqVO();
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setSourceWarehouseId(100L);
        reqVO.setTargetWarehouseId(200L);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsMovementOrderDetailSaveReqVO createMovementOrderDetailReqVO(Long id, Long skuId, String quantity) {
        WmsMovementOrderDetailSaveReqVO reqVO = new WmsMovementOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setPrice(new BigDecimal("100.00"));
        reqVO.setTotalPrice(new BigDecimal(quantity).multiply(new BigDecimal("100.00")));
        return reqVO;
    }

    private static WmsMovementOrderDetailDO createMovementOrderDetail(Long orderId, Long skuId, String quantity) {
        return WmsMovementOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .sourceWarehouseId(100L)
                .targetWarehouseId(200L)
                .quantity(new BigDecimal(quantity))
                .price(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal(quantity).multiply(new BigDecimal("100.00")))
                .build();
    }

}
