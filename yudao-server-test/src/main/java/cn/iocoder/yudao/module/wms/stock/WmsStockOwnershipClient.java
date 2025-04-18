package cn.iocoder.yudao.module.wms.stock;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:50
 * @description:
 */
public class WmsStockOwnershipClient extends RestClient {

    public WmsStockOwnershipClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }




    public CommonResult<PageResult<WmsStockOwnershipRespVO>> getStockOwnershipPage(Long warehouseId) {
        WmsStockOwnershipPageReqVO pageReqVO=new WmsStockOwnershipPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(100);
        pageReqVO.setWarehouseId(warehouseId);
        return getPage("/admin-api/wms/stock-ownership/page",pageReqVO, WmsStockOwnershipRespVO.class);
    }



    public CommonResult<List<WmsStockOwnershipRespVO>> getStockOwnershipList(Long warehouseId,Long productId) {
        WmsStockOwnershipPageReqVO pageReqVO=new WmsStockOwnershipPageReqVO();
        pageReqVO.setWarehouseId(warehouseId).setProductId(productId);
        return getSimpleList("/admin-api/wms/stock-ownership/stocks",pageReqVO, WmsStockOwnershipRespVO.class);
    }

    public CommonResult<WmsStockOwnershipRespVO> getStockOwnership(Long warehouseId, Long companyId , Long deptId, Long productId) {
        WmsStockOwnershipPageReqVO pageReqVO=new WmsStockOwnershipPageReqVO();
        pageReqVO.setWarehouseId(warehouseId).setCompanyId(companyId).setDeptId(deptId).setProductId(productId);
        Map<String,Object> param=BeanUtil.beanToMap(pageReqVO);
        return getOne("/admin-api/wms/stock-ownership/stock", param,WmsStockOwnershipRespVO.class);
    }


    public CommonResult<Long> createStockOwnershipMove(WmsStockOwnershipMoveSaveReqVO createReqVO) {
        return this.create("/wms/stock-ownership-move/create",createReqVO);
    }

}
