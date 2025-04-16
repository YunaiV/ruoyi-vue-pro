package cn.iocoder.yudao.module.wms.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.WmsBaseTest;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.test.Profile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:46
 * @description: 盘点测试
 */

public class WmsInventoryTest extends WmsBaseTest {

    private final Long warehouseId = 32L;





    /**
     * 设置盘点量
     **/
    private Map<String, WmsStockBinRespVO> updateActualQuantity(WmsInventoryRespVO inventoryRespVO) {

        Map<String, WmsStockBinRespVO> binMapBefore = new HashMap<>();

        List<WmsInventoryBinSaveReqVO> saveReqVOS=new ArrayList<>();

        int i=0;
        for (WmsInventoryBinRespVO binItemVO : inventoryRespVO.getBinItemList()) {

            WmsInventoryBinSaveReqVO saveReqVO = BeanUtils.toBean(binItemVO, WmsInventoryBinSaveReqVO.class);



            int sign=i%3 - 1;

            if(inventoryRespVO.getProductItemList().size()==1) {
                sign = 1;
            }

            int actualQty=binItemVO.getExpectedQty()+sign;
            saveReqVO.setActualQty(actualQty);
            System.out.println(saveReqVO.getExpectedQty()+" -> "+saveReqVO.getActualQty());
            i++;
            saveReqVOS.add(saveReqVO);



            CommonResult<List<WmsStockBinRespVO>> stockBinResult = this.stockBinClient().getStockBin(warehouseId, binItemVO.getBinId(), binItemVO.getProductId());
            binMapBefore.put(binItemVO.getProductId()+"-"+binItemVO.getBinId(),stockBinResult.getData().get(0));
        }




        this.inventoryClient().updateInventoryActualQuantity(saveReqVOS);

        return binMapBefore;
    }


    /**
     * 创建与更新盘点单
     **/
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
//                if(testProductIds2.size()<3) {
//                    testProductIds2.add(stockBinRespVO.getProductId());
//                }
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

    @Test
    public void testInventoryAll() {
        // 创建与更新盘点单
        WmsInventoryRespVO inventoryRespVO = testInventoryCreateAndUpdate();
        // 提交盘点单
        submit(inventoryRespVO.getId());
        // 设置盘点量
        Map<String, WmsStockBinRespVO> binMapBefore = updateActualQuantity(inventoryRespVO);
        // 生效盘点单
        agree(inventoryRespVO.getId(),binMapBefore);
    }

    private void submit(Long inventoryId) {
        CommonResult<Boolean> result = this.inventoryClient().submit(inventoryId);
    }

    private void agree(Long inventoryId,Map<String, WmsStockBinRespVO> binMapBefore) {
        //
        CommonResult<Boolean> result = this.inventoryClient().agree(inventoryId);
        System.out.println("完成盘点 : "+result.getMsg());
        //
        CommonResult<WmsInventoryRespVO> inventoryResult = this.inventoryClient().getInventory(inventoryId);
        Map<String, WmsStockBinRespVO> binMapAfter = new HashMap<>();
        Map<String, Integer> deltaMap = StreamX.from(inventoryResult.getData().getBinItemList()).toMap(e->e.getProductId()+"-"+e.getBinId(),x->x.getActualQty()-x.getExpectedQty());
        for (WmsInventoryBinRespVO binItemVO : inventoryResult.getData().getBinItemList()) {
            CommonResult<List<WmsStockBinRespVO>> stockBinResult = this.stockBinClient().getStockBin(warehouseId, binItemVO.getBinId(), binItemVO.getProductId());
            binMapAfter.put(binItemVO.getProductId()+"-"+binItemVO.getBinId(),stockBinResult.getData().get(0));
        }
        //
        for (Map.Entry<String, WmsStockBinRespVO> entryBefore : binMapBefore.entrySet()) {
            WmsStockBinRespVO stockBinRespVOBefore = entryBefore.getValue();
            WmsStockBinRespVO stockBinRespVOAfter = binMapAfter.get(entryBefore.getKey());
            Integer delta = deltaMap.get(entryBefore.getKey());
            System.out.println(stockBinRespVOBefore.getProductId()+"@"+stockBinRespVOBefore.getBinId()+"\tdelta:"+delta+"; SellableQty : "+stockBinRespVOBefore.getSellableQty()+" -> "+stockBinRespVOAfter.getSellableQty()+"; AvailableQty : "+stockBinRespVOBefore.getAvailableQty()+" -> "+stockBinRespVOAfter.getAvailableQty());
        }
        System.out.println();

    }


    public static void main(String[] args) {
        WmsInventoryTest wmsInventoryTest = new WmsInventoryTest();
        wmsInventoryTest.setProfile(Profile.LOCAL);
        wmsInventoryTest.testInventoryAll();
    }


}
