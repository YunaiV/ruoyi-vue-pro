package cn.iocoder.yudao.module.wms.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.junit.Assert;
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
        CommonResult<Long> result = this.create("/wms/inventory/create",createReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData() > 0);
        return result;
    }



    public CommonResult<Boolean> updateInventory(WmsInventorySaveReqVO updateReqVO) {
        CommonResult<Boolean> result = this.update("/wms/inventory/update",updateReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

    protected CommonResult<WmsInventoryRespVO> getInventory(Long inventoryId) {
        CommonResult<WmsInventoryRespVO> result = getOne("/wms/inventory/get", inventoryId,WmsInventoryRespVO.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertNotNull(result.getData());
        Assert.assertSame(result.getData().getId(), inventoryId);
        return result;
    }

    public CommonResult<Boolean> updateInventoryActualQuantity(List<WmsInventoryBinSaveReqVO> binSaveReqVOS) {
        CommonResult<Boolean> result = this.update("/wms/inventory-bin/update-actual-quantity",binSaveReqVOS);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

    public CommonResult<Boolean> submit(Long inventoryId) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inventoryId);
        approvalReqVO.setComment("提交审批");
        //
        CommonResult<Boolean> result = this.update("/wms/inventory/submit",approvalReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

    public CommonResult<Boolean> agree(Long inventoryId) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inventoryId);
        approvalReqVO.setComment("调整库存");
        CommonResult<Boolean> result = this.update("/wms/inventory/agree",approvalReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }
}
