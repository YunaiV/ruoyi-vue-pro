package cn.iocoder.yudao.module.wms.warehouse;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSimpleRespVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:48
 * @description:
 */
public class WmsWarehouseBinClient extends RestClient {

    public WmsWarehouseBinClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }

    public CommonResult<List<WmsWarehouseBinSimpleRespVO>> getBinSimpleList(Long warehouseId) {
        WmsWarehouseBinPageReqVO pageReqVO=new WmsWarehouseBinPageReqVO();
        pageReqVO.setWarehouseId(warehouseId);
        return getSimpleList("/wms/warehouse-bin//simple-list",pageReqVO, WmsWarehouseBinSimpleRespVO.class);
    }

}
