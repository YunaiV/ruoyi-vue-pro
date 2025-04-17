package cn.iocoder.yudao.module.erp.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author: LeeFJ
 * @date: 2025/4/17 11:03
 * @description:
 */
public class ErpProductClient extends RestClient {

    public ErpProductClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }


    public CommonResult<PageResult<ErpProductRespVO>> getProductPage(int pageSize,int pageNo) {
        ErpProductPageReqVO pageReqVO=new ErpProductPageReqVO();
        pageReqVO.setPageNo(pageNo);
        pageReqVO.setPageSize(pageSize);
        return getPage("/erp/product/page",pageReqVO, ErpProductRespVO.class);
    }


}
