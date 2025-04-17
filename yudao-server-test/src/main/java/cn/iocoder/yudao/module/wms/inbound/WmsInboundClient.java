package cn.iocoder.yudao.module.wms.inbound;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
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
public class WmsInboundClient extends RestClient {

    public WmsInboundClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }

    public CommonResult<Long> createInbound(WmsInboundSaveReqVO createReqVO) {
        CommonResult<Long> result = this.create("/wms/inbound/create",createReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData() > 0);
        return result;
    }



    public CommonResult<Boolean> updateInbound(WmsInboundSaveReqVO updateReqVO) {
        CommonResult<Boolean> result = this.update("/wms/inbound/update",updateReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

    protected CommonResult<WmsInboundRespVO> getInbound(Long inboundId) {
        CommonResult<WmsInboundRespVO> result = getOne("/wms/inbound/get", inboundId,WmsInboundRespVO.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertNotNull(result.getData());
        Assert.assertEquals(result.getData().getId(), inboundId);
        return result;
    }

    public CommonResult<Boolean> updateInventoryActualQuantity(List<WmsInboundItemSaveReqVO> itemSaveReqVOS) {
        CommonResult<Boolean> result = this.update("/wms/inbound-item/update-actual-quantity",itemSaveReqVOS);
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
        CommonResult<Boolean> result = this.update("/wms/inbound/submit",approvalReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

    public CommonResult<Boolean> agree(Long inventoryId) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inventoryId);
        approvalReqVO.setComment("同意入库");
        CommonResult<Boolean> result = this.update("/wms/inbound/agree",approvalReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }
}
