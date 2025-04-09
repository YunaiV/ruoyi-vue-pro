package cn.iocoder.yudao.module.wms.stock.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.WmsBaseTest;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseRespVO;
import cn.iocoder.yudao.test.Profile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:46
 * @description: 盘点测试
 */

public class WmsInventoryTest extends WmsBaseTest {


    @Test
    public void testMove() {

        Long warehouseId = 32L;


        // 确定测试的产品清单
        CommonResult<PageResult<WmsStockWarehouseRespVO>> stockBinPageResult = this.getStockWarehousePage(warehouseId);
        if(stockBinPageResult.isError()) {
            System.err.println("缺少库存数据，无法继续测试");
            return;
        }

        List<WmsStockWarehouseRespVO> stockWarehouseList = stockBinPageResult.getData().getList();
        List<Long> testProductIds1 = new ArrayList<>();
        List<Long> testProductIds2 = new ArrayList<>();
        for (WmsStockWarehouseRespVO stockBinRespVO : stockWarehouseList) {
            if(testProductIds1.size()<2) {
                testProductIds1.add(stockBinRespVO.getProductId());
            } else {
                if(testProductIds2.size()<3) {
                    testProductIds2.add(stockBinRespVO.getProductId());
                }
            }
        }

        WmsInventorySaveReqVO createReqVO = new WmsInventorySaveReqVO();

        createReqVO.setWarehouseId(warehouseId);

        List<WmsInventoryProductSaveReqVO> productSaveReqVOList = new ArrayList<>();
        for (Long testProductId : testProductIds1) {
            WmsInventoryProductSaveReqVO productSaveReqVO = new WmsInventoryProductSaveReqVO();
            productSaveReqVO.setProductId(testProductId);
            productSaveReqVOList.add(productSaveReqVO);
        }
        createReqVO.setProductItemList(productSaveReqVOList);
        CommonResult<Long> postResult=createInventory(createReqVO);

        if(postResult.isError()) {
            Assert.assertTrue("创建盘点单失败",false);
        }


//        CommonResult<List<WmsStockBinRespVO>> fromStockBinResultAfter = getStockBin(warehouseId, fromStockBinVOBefore.getBinId(), fromStockBinVOBefore.getProductId());
//        CommonResult<List<WmsStockBinRespVO>> toStockBinResultAfter = getStockBin(warehouseId, toStockBinVOBefore.getBinId(), toStockBinVOBefore.getProductId());
//
//        WmsStockBinRespVO fromStockBinVOAfter = fromStockBinResultAfter.getData().get(0);
//        WmsStockBinRespVO toStockBinVOAfter = toStockBinResultAfter.getData().get(0);
//
//        Assert.assertTrue("库存数量-FROM",fromStockBinVOAfter.getAvailableQty()==fromStockBinVOBefore.getAvailableQty()-qty);
//        Assert.assertTrue("库存数量-TO",toStockBinVOAfter.getAvailableQty()==toStockBinVOBefore.getAvailableQty()+qty);
//
//        Assert.assertTrue("可售数量-FROM",fromStockBinVOAfter.getSellableQty()==fromStockBinVOBefore.getSellableQty()-qty);
//        Assert.assertTrue("可售数量-TO",toStockBinVOAfter.getSellableQty()==toStockBinVOBefore.getSellableQty()+qty);

        System.out.println();


    }


    public static void main(String[] args) {
        WmsInventoryTest wmsInventoryTest = new WmsInventoryTest();
        wmsInventoryTest.setProfile(Profile.LOCAL);
        wmsInventoryTest.testMove();
    }


}
