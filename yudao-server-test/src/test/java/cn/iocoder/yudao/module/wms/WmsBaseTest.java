package cn.iocoder.yudao.module.wms;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSimpleRespVO;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:47
 * @description:
 */
public class WmsBaseTest extends BaseRestIntegrationTest {



    public CommonResult<PageResult<WmsStockWarehouseRespVO>> getStockWarehousePage(Long warehouseId) {
        WmsStockWarehousePageReqVO pageReqVO=new WmsStockWarehousePageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        pageReqVO.setWarehouseId(warehouseId);
        return getPage("/admin-api/wms/stock-warehouse/page",pageReqVO, WmsStockWarehouseRespVO.class);
    }

    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage(Long warehouseId) {
        WmsStockBinPageReqVO pageReqVO=new WmsStockBinPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        pageReqVO.setWarehouseId(warehouseId);
        return getPage("/admin-api/wms/stock-bin/page",pageReqVO, WmsStockBinRespVO.class);
    }

    public CommonResult<PageResult<WmsStockOwnershipRespVO>> getStockOwnershipPage(Long warehouseId) {
        WmsStockOwnershipPageReqVO pageReqVO=new WmsStockOwnershipPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        pageReqVO.setWarehouseId(warehouseId);
        return getPage("/admin-api/wms/stock-ownership/page",pageReqVO, WmsStockOwnershipRespVO.class);
    }

    public CommonResult<List<WmsStockBinRespVO>> getStockBin(Long warehouseId, Long binId, Long productId) {
        WmsStockBinPageReqVO pageReqVO=new WmsStockBinPageReqVO();
        pageReqVO.setWarehouseId(warehouseId).setBinId(binId).setProductId(productId);
        return getSimpleList("/admin-api/wms/stock-bin/stocks",pageReqVO, WmsStockBinRespVO.class);
    }

    public CommonResult<List<WmsStockOwnershipRespVO>> getStockOwnership(Long warehouseId, Long companyId , Long deptId, Long productId) {
        WmsStockOwnershipPageReqVO pageReqVO=new WmsStockOwnershipPageReqVO();
        pageReqVO.setWarehouseId(warehouseId).setCompanyId(companyId).setDeptId(deptId).setProductId(productId);
        return getSimpleList("/admin-api/wms/stock-ownership/stocks",pageReqVO, WmsStockOwnershipRespVO.class);
    }


    public CommonResult<List<WmsWarehouseBinSimpleRespVO>> getBinSimpleList(Long warehouseId) {
        WmsWarehouseBinPageReqVO pageReqVO=new WmsWarehouseBinPageReqVO();
        pageReqVO.setWarehouseId(warehouseId);
        return getSimpleList("/wms/warehouse-bin//simple-list",pageReqVO, WmsWarehouseBinSimpleRespVO.class);
    }

    public CommonResult<Long> createInventory(WmsInventorySaveReqVO createReqVO) {
        return this.create("/wms/inventory/create",createReqVO);
    }

    public CommonResult<Long> createStockBinMove(WmsStockBinMoveSaveReqVO createReqVO) {
        return this.create("/wms/stock-bin-move/create",createReqVO);
    }

    public CommonResult<Boolean> updateInventory(WmsInventorySaveReqVO updateReqVO) {
        return this.update("/wms/inventory/update",updateReqVO);
    }

    public CommonResult<Long> createStockOwnershipMove(WmsStockOwnershipMoveSaveReqVO createReqVO) {
        return this.create("/wms/stock-ownership-move/create",createReqVO);
    }


    protected CommonResult<WmsInventoryRespVO> getInventory(Long inventoryId) {
        return getOne("/wms/inventory/get", inventoryId,WmsInventoryRespVO.class);
    }
}
