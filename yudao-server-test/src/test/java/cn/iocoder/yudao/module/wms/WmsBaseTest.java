package cn.iocoder.yudao.module.wms;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
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



    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage() {
        WmsStockBinPageReqVO pageReqVO=new WmsStockBinPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        return getPage("/admin-api/wms/stock-bin/page",pageReqVO, WmsStockBinRespVO.class);
    }

    public CommonResult<List<WmsStockBinRespVO>> getStockBin(Long warehouseId, Long binId, Long productId) {
        WmsStockBinPageReqVO pageReqVO=new WmsStockBinPageReqVO();
        pageReqVO.setWarehouseId(warehouseId).setBinId(binId).setProductId(productId);
        return getSimpleList("/admin-api/wms/stock-bin/stocks",pageReqVO, WmsStockBinRespVO.class);
    }


    public CommonResult<List<WmsWarehouseBinSimpleRespVO>> getBinSimpleList(Long warehouseId) {
        WmsWarehouseBinPageReqVO pageReqVO=new WmsWarehouseBinPageReqVO();
        pageReqVO.setWarehouseId(warehouseId);
        return getSimpleList("/wms/warehouse-bin//simple-list",pageReqVO, WmsWarehouseBinSimpleRespVO.class);
    }

    public CommonResult<Long> createStockBinMove(WmsStockBinMoveSaveReqVO createReqVO) {
        return this.create("/wms/stock-bin-move/create",createReqVO);
    }


}
