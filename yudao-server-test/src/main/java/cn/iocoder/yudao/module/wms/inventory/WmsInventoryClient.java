package cn.iocoder.yudao.module.wms.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;

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

    public CommonResult<Boolean> updateInventoryActualQuantity(List<WmsInventoryBinSaveReqVO> binSaveReqVOS) {
        return this.update("/wms/inventory-bin/update-actual-quantity",binSaveReqVOS);
    }

    public CommonResult<Boolean> submit(Long inventoryId) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inventoryId);
        approvalReqVO.setComment("提交审批");
        return this.update("/wms/inventory/submit",approvalReqVO);
    }

    public CommonResult<Boolean> agree(Long inventoryId) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inventoryId);
        approvalReqVO.setComment("调整库存");
        return this.update("/wms/inventory/agree",approvalReqVO);
    }
}
