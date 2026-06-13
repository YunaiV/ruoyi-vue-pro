package cn.iocoder.yudao.module.wms.service.order.shipment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.detail.WmsShipmentOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.shipment.WmsShipmentOrderDetailMapper;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.SHIPMENT_ORDER_DETAIL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Import(WmsShipmentOrderDetailServiceImpl.class)
public class WmsShipmentOrderDetailServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsShipmentOrderDetailServiceImpl shipmentOrderDetailService;

    @Resource
    private WmsShipmentOrderDetailMapper shipmentOrderDetailMapper;

    @MockitoBean
    private WmsItemSkuService itemSkuService;

    @Test
    public void testCreateShipmentOrderDetailList_success() {
        // mock 数据
        Long orderId = 10L;
        WmsShipmentOrderSaveReqVO reqVO = createShipmentOrderReqVO(
                createShipmentOrderDetailReqVO(null, 1001L, "1.00"),
                createShipmentOrderDetailReqVO(null, 1002L, "2.00"));

        // 调用
        shipmentOrderDetailService.createShipmentOrderDetailList(orderId, reqVO);

        // 断言
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(orderId, details.get(0).getOrderId());
        assertEquals(orderId, details.get(1).getOrderId());
        assertEquals(100L, details.get(0).getWarehouseId());
        assertEquals(0, new BigDecimal("100.00").compareTo(details.get(0).getTotalPrice()));
        assertEquals(0, new BigDecimal("200.00").compareTo(details.get(1).getTotalPrice()));
    }

    @Test
    public void testCreateShipmentOrderDetailList_ignoreId() {
        // mock 数据
        WmsShipmentOrderSaveReqVO reqVO = createShipmentOrderReqVO(
                createShipmentOrderDetailReqVO(999L, 1001L, "1.00"));

        // 调用
        shipmentOrderDetailService.createShipmentOrderDetailList(10L, reqVO);

        // 断言
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailMapper.selectListByOrderId(10L);
        assertEquals(1, details.size());
        assertNotNull(details.get(0).getId());
        assertEquals(1001L, details.get(0).getSkuId());
    }

    @Test
    public void testUpdateShipmentOrderDetailList_diff() {
        // mock 数据
        Long orderId = 10L;
        WmsShipmentOrderDetailDO detail01 = createShipmentOrderDetail(orderId, 1001L, "1.00");
        WmsShipmentOrderDetailDO detail02 = createShipmentOrderDetail(orderId, 1002L, "2.00");
        shipmentOrderDetailMapper.insert(detail01);
        shipmentOrderDetailMapper.insert(detail02);
        WmsShipmentOrderSaveReqVO reqVO = createShipmentOrderReqVO(
                createShipmentOrderDetailReqVO(detail01.getId(), 2001L, "11.00"),
                createShipmentOrderDetailReqVO(null, 2002L, "22.00"));

        // 调用
        shipmentOrderDetailService.updateShipmentOrderDetailList(orderId, reqVO);

        // 断言：修改
        WmsShipmentOrderDetailDO dbUpdateDetail = shipmentOrderDetailMapper.selectById(detail01.getId());
        assertNotNull(dbUpdateDetail);
        assertEquals(orderId, dbUpdateDetail.getOrderId());
        assertEquals(2001L, dbUpdateDetail.getSkuId());
        assertEquals(0, new BigDecimal("11.00").compareTo(dbUpdateDetail.getQuantity()));
        assertEquals(0, new BigDecimal("1100.00").compareTo(dbUpdateDetail.getTotalPrice()));
        // 断言：新增
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        WmsShipmentOrderDetailDO dbCreateDetail = details.stream()
                .filter(detail -> Long.valueOf(2002L).equals(detail.getSkuId()))
                .findFirst().orElse(null);
        assertNotNull(dbCreateDetail);
        assertEquals(orderId, dbCreateDetail.getOrderId());
        assertEquals(0, new BigDecimal("22.00").compareTo(dbCreateDetail.getQuantity()));
        assertEquals(0, new BigDecimal("2200.00").compareTo(dbCreateDetail.getTotalPrice()));
        // 断言：删除
        assertNull(shipmentOrderDetailMapper.selectById(detail02.getId()));
    }

    @Test
    public void testUpdateShipmentOrderDetailList_detailNotExists() {
        // mock 数据
        WmsShipmentOrderSaveReqVO reqVO = createShipmentOrderReqVO(
                createShipmentOrderDetailReqVO(999L, 1001L, "1.00"));

        // 调用，并断言
        assertServiceException(() -> shipmentOrderDetailService.updateShipmentOrderDetailList(10L, reqVO),
                SHIPMENT_ORDER_DETAIL_NOT_EXISTS);
    }

    private static WmsShipmentOrderSaveReqVO createShipmentOrderReqVO(WmsShipmentOrderDetailSaveReqVO... details) {
        WmsShipmentOrderSaveReqVO reqVO = new WmsShipmentOrderSaveReqVO();
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setWarehouseId(100L);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsShipmentOrderDetailSaveReqVO createShipmentOrderDetailReqVO(Long id, Long skuId, String quantity) {
        WmsShipmentOrderDetailSaveReqVO reqVO = new WmsShipmentOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setPrice(new BigDecimal("100.00"));
        reqVO.setTotalPrice(new BigDecimal(quantity).multiply(new BigDecimal("100.00")));
        return reqVO;
    }

    private static WmsShipmentOrderDetailDO createShipmentOrderDetail(Long orderId, Long skuId, String quantity) {
        return WmsShipmentOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .warehouseId(100L)
                .quantity(new BigDecimal(quantity))
                .price(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal(quantity).multiply(new BigDecimal("100.00")))
                .build();
    }

}
