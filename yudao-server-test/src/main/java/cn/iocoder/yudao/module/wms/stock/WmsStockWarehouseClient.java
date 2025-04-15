package cn.iocoder.yudao.module.wms.stock;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseRespVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:43
 * @description:
 */
public class WmsStockWarehouseClient extends RestClient {


    public WmsStockWarehouseClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }

    public CommonResult<PageResult<WmsStockWarehouseRespVO>> getStockWarehousePage(Long warehouseId) {
        WmsStockWarehousePageReqVO pageReqVO=new WmsStockWarehousePageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        pageReqVO.setWarehouseId(warehouseId);
        return getPage("/admin-api/wms/stock-warehouse/page",pageReqVO, WmsStockWarehouseRespVO.class);
    }

}
