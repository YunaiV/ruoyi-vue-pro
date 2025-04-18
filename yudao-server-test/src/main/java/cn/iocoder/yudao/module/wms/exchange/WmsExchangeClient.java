package cn.iocoder.yudao.module.wms.exchange;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.junit.Assert;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:36
 * @description:
 */
public class WmsExchangeClient extends RestClient {

    public WmsExchangeClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }

    public CommonResult<Long> createExchange(WmsExchangeSaveReqVO createReqVO) {
        CommonResult<Long> result = this.create("/wms/exchange/create",createReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData() > 0);
        return result;
    }



    public CommonResult<Boolean> updateExchange(WmsExchangeSaveReqVO updateReqVO) {
        CommonResult<Boolean> result = this.update("/wms/exchange/update",updateReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

    protected CommonResult<WmsExchangeRespVO> getExchange(Long exchangeId) {
        CommonResult<WmsExchangeRespVO> result = getOne("/wms/exchange/get", exchangeId,WmsExchangeRespVO.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertNotNull(result.getData());
        Assert.assertEquals(result.getData().getId(), exchangeId);
        return result;
    }

    public CommonResult<Boolean> submit(Long inventoryId) {
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inventoryId);
        approvalReqVO.setComment("提交审批");
        //
        CommonResult<Boolean> result = this.update("/wms/exchange/submit",approvalReqVO);
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
        CommonResult<Boolean> result = this.update("/wms/exchange/agree",approvalReqVO);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertTrue(result.getData());
        return result;
    }

}
