package cn.iocoder.yudao.module.wms.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.WmsBaseTest;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.test.Profile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:46
 * @description: 盘点测试
 */

public class WmsInventoryTest extends WmsBaseTest {

    private final Long warehouseId = 32L;

    @Test
    public void testInventoryAll() {
        // 创建与更新盘点单
        WmsInventoryRespVO inventoryRespVO = testInventoryCreateAndUpdate();
        // 设置盘点量
        updateActualQuantity(inventoryRespVO);

    }

    private void updateActualQuantity(WmsInventoryRespVO inventoryRespVO) {



    }


    public WmsInventoryRespVO testInventoryCreateAndUpdate() {


        // 确定测试的产品清单
        CommonResult<PageResult<WmsStockBinRespVO>> stockBinPageResult = this.stockBinClient().getStockBinPage(warehouseId);
        if(stockBinPageResult.isError()) {
            System.err.println("缺少库存数据，无法继续测试");
            return null;
        }

        List<WmsStockBinRespVO> stockBinList = stockBinPageResult.getData().getList();
        Set<Long> testProductIds1 = new HashSet<>();
        Set<Long> testProductIds2 = new HashSet<>();
        for (WmsStockBinRespVO stockBinRespVO : stockBinList) {
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
        CommonResult<Long> postResult=this.inventoryClient().createInventory(createReqVO);

        if(postResult.isError()) {
            Assert.assertTrue("创建盘点单失败",false);
        }

        Long inventoryId = postResult.getData();

        CommonResult<WmsInventoryRespVO> inventoryResult = this.inventoryClient().getInventory(inventoryId);

        WmsInventoryRespVO inventoryRespVO = inventoryResult.getData();




        WmsInventorySaveReqVO updateReqVO = BeanUtils.toBean(inventoryRespVO, WmsInventorySaveReqVO.class);
        updateReqVO.setBinItemList(null);

        updateReqVO.getProductItemList().remove(0);
        for (Long testProductId : testProductIds2) {
            WmsInventoryProductSaveReqVO productSaveReqVO = new WmsInventoryProductSaveReqVO();
            productSaveReqVO.setProductId(testProductId);
            updateReqVO.getProductItemList().add(productSaveReqVO);
        }
        CommonResult<Boolean> updateResult = this.inventoryClient().updateInventory(updateReqVO);

        if(updateResult.isError()) {
            Assert.assertTrue("更新盘点单失败",false);
        }


        inventoryResult = this.inventoryClient().getInventory(inventoryId);

        inventoryRespVO = inventoryResult.getData();

        return inventoryRespVO;

    }




    public static void main(String[] args) {
        WmsInventoryTest wmsInventoryTest = new WmsInventoryTest();
        wmsInventoryTest.setProfile(Profile.LOCAL);
        // wmsInventoryTest.testMove();
    }


}
