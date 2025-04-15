package cn.iocoder.yudao.module.wms.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:36
 * @description:
 */
public class WmsInventoryClient extends RestClient {

    public WmsInventoryClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }

    public CommonResult<Long> createInventory(WmsInventorySaveReqVO createReqVO) {
        return this.create("/wms/inventory/create",createReqVO);
    }



    public CommonResult<Boolean> updateInventory(WmsInventorySaveReqVO updateReqVO) {
        return this.update("/wms/inventory/update",updateReqVO);
    }

    protected CommonResult<WmsInventoryRespVO> getInventory(Long inventoryId) {
        return getOne("/wms/inventory/get", inventoryId,WmsInventoryRespVO.class);
    }

}
