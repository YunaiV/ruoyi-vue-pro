package cn.iocoder.yudao.module.wms.service.order.check;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.detail.WmsCheckOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order.WmsCheckOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.check.WmsCheckOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.check.WmsCheckOrderDetailMapper;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.CHECK_ORDER_DETAIL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Import(WmsCheckOrderDetailServiceImpl.class)
public class WmsCheckOrderDetailServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsCheckOrderDetailServiceImpl checkOrderDetailService;

    @Resource
    private WmsCheckOrderDetailMapper checkOrderDetailMapper;

    @MockitoBean
    private WmsItemSkuService itemSkuService;

    @Test
    public void testCreateCheckOrderDetailList_success() {
        // mock 数据
        Long orderId = 10L;
        WmsCheckOrderSaveReqVO reqVO = createCheckOrderReqVO(
                createCheckOrderDetailReqVO(null, 1001L, "10.00", "9.00"),
                createCheckOrderDetailReqVO(null, 1002L, "20.00", "21.00"));

        // 调用
        checkOrderDetailService.createCheckOrderDetailList(orderId, reqVO);

        // 断言
        List<WmsCheckOrderDetailDO> details = checkOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        assertEquals(orderId, details.get(0).getOrderId());
        assertEquals(orderId, details.get(1).getOrderId());
        assertEquals(100L, details.get(0).getWarehouseId());
    }

    @Test
    public void testCreateCheckOrderDetailList_ignoreId() {
        // mock 数据
        WmsCheckOrderSaveReqVO reqVO = createCheckOrderReqVO(
                createCheckOrderDetailReqVO(999L, 1001L, "10.00", "9.00"));

        // 调用
        checkOrderDetailService.createCheckOrderDetailList(10L, reqVO);

        // 断言
        List<WmsCheckOrderDetailDO> details = checkOrderDetailMapper.selectListByOrderId(10L);
        assertEquals(1, details.size());
        assertNotNull(details.get(0).getId());
        assertEquals(1001L, details.get(0).getSkuId());
    }

    @Test
    public void testUpdateCheckOrderDetailList_diff() {
        // mock 数据
        Long orderId = 10L;
        WmsCheckOrderDetailDO detail01 = createCheckOrderDetail(orderId, 1001L, "10.00", "9.00");
        WmsCheckOrderDetailDO detail02 = createCheckOrderDetail(orderId, 1002L, "20.00", "21.00");
        checkOrderDetailMapper.insert(detail01);
        checkOrderDetailMapper.insert(detail02);
        WmsCheckOrderSaveReqVO reqVO = createCheckOrderReqVO(
                createCheckOrderDetailReqVO(detail01.getId(), 2001L, "11.00", "8.00"),
                createCheckOrderDetailReqVO(null, 2002L, "22.00", "25.00"));

        // 调用
        checkOrderDetailService.updateCheckOrderDetailList(orderId, reqVO);

        // 断言：修改
        WmsCheckOrderDetailDO dbUpdateDetail = checkOrderDetailMapper.selectById(detail01.getId());
        assertNotNull(dbUpdateDetail);
        assertEquals(orderId, dbUpdateDetail.getOrderId());
        assertEquals(2001L, dbUpdateDetail.getSkuId());
        assertEquals(0, new BigDecimal("11.00").compareTo(dbUpdateDetail.getQuantity()));
        assertEquals(0, new BigDecimal("8.00").compareTo(dbUpdateDetail.getCheckQuantity()));
        assertEquals(0, new BigDecimal("100.00").compareTo(dbUpdateDetail.getPrice()));
        assertEquals(100L, dbUpdateDetail.getWarehouseId());
        // 断言：新增
        List<WmsCheckOrderDetailDO> details = checkOrderDetailMapper.selectListByOrderId(orderId);
        assertEquals(2, details.size());
        WmsCheckOrderDetailDO dbCreateDetail = details.stream()
                .filter(detail -> Long.valueOf(2002L).equals(detail.getSkuId()))
                .findFirst().orElse(null);
        assertNotNull(dbCreateDetail);
        assertEquals(orderId, dbCreateDetail.getOrderId());
        assertEquals(0, new BigDecimal("22.00").compareTo(dbCreateDetail.getQuantity()));
        assertEquals(0, new BigDecimal("25.00").compareTo(dbCreateDetail.getCheckQuantity()));
        // 断言：删除
        assertNull(checkOrderDetailMapper.selectById(detail02.getId()));
    }

    @Test
    public void testUpdateCheckOrderDetailList_detailNotExists() {
        // mock 数据
        WmsCheckOrderSaveReqVO reqVO = createCheckOrderReqVO(
                createCheckOrderDetailReqVO(999L, 1001L, "10.00", "9.00"));

        // 调用，并断言
        assertServiceException(() -> checkOrderDetailService.updateCheckOrderDetailList(10L, reqVO),
                CHECK_ORDER_DETAIL_NOT_EXISTS);
    }

    private static WmsCheckOrderSaveReqVO createCheckOrderReqVO(WmsCheckOrderDetailSaveReqVO... details) {
        WmsCheckOrderSaveReqVO reqVO = new WmsCheckOrderSaveReqVO();
        reqVO.setOrderTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        reqVO.setWarehouseId(100L);
        reqVO.setDetails(Arrays.asList(details));
        return reqVO;
    }

    private static WmsCheckOrderDetailSaveReqVO createCheckOrderDetailReqVO(Long id, Long skuId,
                                                                           String quantity, String checkQuantity) {
        WmsCheckOrderDetailSaveReqVO reqVO = new WmsCheckOrderDetailSaveReqVO();
        reqVO.setId(id);
        reqVO.setSkuId(skuId);
        reqVO.setInventoryId(300L);
        reqVO.setQuantity(new BigDecimal(quantity));
        reqVO.setCheckQuantity(new BigDecimal(checkQuantity));
        reqVO.setPrice(new BigDecimal("100.00"));
        return reqVO;
    }

    private static WmsCheckOrderDetailDO createCheckOrderDetail(Long orderId, Long skuId,
                                                               String quantity, String checkQuantity) {
        return WmsCheckOrderDetailDO.builder()
                .orderId(orderId)
                .skuId(skuId)
                .warehouseId(100L)
                .inventoryId(300L)
                .quantity(new BigDecimal(quantity))
                .checkQuantity(new BigDecimal(checkQuantity))
                .price(new BigDecimal("100.00"))
                .build();
    }

}
