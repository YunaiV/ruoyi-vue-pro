package cn.iocoder.yudao.module.wms.stock;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:45
 * @description:
 */
public class WmsStockBinClient extends RestClient {

    public WmsStockBinClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }

    public CommonResult<Long> createStockBinMove(WmsStockBinMoveSaveReqVO createReqVO) {
        return this.create("/wms/stock-bin-move/create",createReqVO);
    }

    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage(Long warehouseId) {
        WmsStockBinPageReqVO pageReqVO=new WmsStockBinPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        pageReqVO.setWarehouseId(warehouseId);
        return getPage("/admin-api/wms/stock-bin/page",pageReqVO, WmsStockBinRespVO.class);
    }

    public CommonResult<List<WmsStockBinRespVO>> getStockBin(Long warehouseId, Long binId, Long productId) {
        WmsStockBinPageReqVO pageReqVO=new WmsStockBinPageReqVO();
        pageReqVO.setWarehouseId(warehouseId).setBinId(binId).setProductId(productId);
        return getSimpleList("/admin-api/wms/stock-bin/stocks",pageReqVO, WmsStockBinRespVO.class);
    }

}
