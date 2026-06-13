package cn.iocoder.yudao.module.wms.service.md.warehouse;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo.WmsWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.warehouse.WmsWarehouseMapper;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.order.check.WmsCheckOrderService;
import cn.iocoder.yudao.module.wms.service.order.movement.WmsMovementOrderService;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderService;
import cn.iocoder.yudao.module.wms.service.order.shipment.WmsShipmentOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_HAS_INVENTORY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@Import(WmsWarehouseServiceImpl.class)
public class WmsWarehouseServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsWarehouseServiceImpl warehouseService;

    @Resource
    private WmsWarehouseMapper warehouseMapper;

    @MockitoBean
    private WmsInventoryService inventoryService;
    @MockitoBean
    private WmsReceiptOrderService receiptOrderService;
    @MockitoBean
    private WmsShipmentOrderService shipmentOrderService;
    @MockitoBean
    private WmsMovementOrderService movementOrderService;
    @MockitoBean
    private WmsCheckOrderService checkOrderService;

    @Test
    public void testCreateWarehouse_codeDuplicate() {
        // mock 数据
        WmsWarehouseDO warehouse = createWarehouse("WH001", "成品仓");
        warehouseMapper.insert(warehouse);

        // 调用，并断言
        WmsWarehouseSaveReqVO reqVO = createWarehouseSaveReqVO();
        assertServiceException(() -> warehouseService.createWarehouse(reqVO), WAREHOUSE_CODE_DUPLICATE);
    }

    @Test
    public void testUpdateWarehouse_codeDuplicate() {
        // mock 数据
        WmsWarehouseDO warehouse = createWarehouse("WH001", "成品仓");
        warehouseMapper.insert(warehouse);
        WmsWarehouseDO updateWarehouse = createWarehouse("WH002", "原料仓");
        warehouseMapper.insert(updateWarehouse);

        // 调用，并断言
        WmsWarehouseSaveReqVO reqVO = createWarehouseSaveReqVO();
        reqVO.setId(updateWarehouse.getId());
        assertServiceException(() -> warehouseService.updateWarehouse(reqVO), WAREHOUSE_CODE_DUPLICATE);
    }

    @Test
    public void testDeleteWarehouse_hasInventory() {
        // mock 数据
        WmsWarehouseDO warehouse = createWarehouse();
        warehouseMapper.insert(warehouse);
        when(inventoryService.getInventoryCountByWarehouseId(warehouse.getId())).thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> warehouseService.deleteWarehouse(warehouse.getId()), WAREHOUSE_HAS_INVENTORY);
        assertNotNull(warehouseMapper.selectById(warehouse.getId()));
    }

    private static WmsWarehouseDO createWarehouse() {
        return createWarehouse("WH001", "成品仓");
    }

    private static WmsWarehouseDO createWarehouse(String code, String name) {
        return WmsWarehouseDO.builder()
                .code(code)
                .name(name)
                .sort(1)
                .build();
    }

    private static WmsWarehouseSaveReqVO createWarehouseSaveReqVO() {
        WmsWarehouseSaveReqVO reqVO = new WmsWarehouseSaveReqVO();
        reqVO.setCode("WH001");
        reqVO.setName("半成品仓");
        reqVO.setSort(1);
        return reqVO;
    }

}
