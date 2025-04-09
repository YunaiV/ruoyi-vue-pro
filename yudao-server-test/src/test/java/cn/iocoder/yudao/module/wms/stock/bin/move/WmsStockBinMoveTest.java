package cn.iocoder.yudao.module.wms.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.WmsBaseTest;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSimpleRespVO;
import cn.iocoder.yudao.test.Profile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:46
 * @description: 库位库存移动测试
 */


public class WmsStockBinMoveTest extends WmsBaseTest {


    @Test
    public void testMove() {

        Long warehouseId = 32L;
        Integer qty=1;


        // 确定从哪个仓位出哪个产品
        CommonResult<PageResult<WmsStockBinRespVO>> stockBinPageResult = this.getStockBinPage(warehouseId);
        if(stockBinPageResult.isError()) {
            System.err.println("缺少库存数据，无法继续测试");
            return;
        }
        List<WmsStockBinRespVO> stockBinList = stockBinPageResult.getData().getList();
        WmsStockBinRespVO fromStockBinVOBefore = stockBinList.get(0);
        for (WmsStockBinRespVO stockBinRespVO : stockBinList) {
            if(stockBinRespVO.getAvailableQty()>0) {
                fromStockBinVOBefore=stockBinRespVO;
                break;
            }
        }

        // 找一个合适的目标仓位
        CommonResult<List<WmsWarehouseBinSimpleRespVO>> binSimpleList = this.getBinSimpleList(warehouseId);
        WmsWarehouseBinSimpleRespVO toStockBin = null;
        for (WmsWarehouseBinSimpleRespVO wmsWarehouseBinSimpleRespVO : binSimpleList.getData()) {
            if(wmsWarehouseBinSimpleRespVO.getId().equals(fromStockBinVOBefore.getBinId())) {
                continue;
            }
            toStockBin = wmsWarehouseBinSimpleRespVO;
            break;
        }

        // 目标仓位库存情况
        WmsStockBinRespVO toStockBinVOBefore = null;
        CommonResult<List<WmsStockBinRespVO>> toStockBinPageResult = this.getStockBin(warehouseId, toStockBin.getId(),fromStockBinVOBefore.getProductId());
        if(toStockBinPageResult.isError() || toStockBinPageResult.getData().isEmpty()) {
            toStockBinVOBefore = new WmsStockBinRespVO();
            toStockBinVOBefore.setProductId(fromStockBinVOBefore.getProductId());
            toStockBinVOBefore.setBinId(toStockBin.getId());
            toStockBinVOBefore.setAvailableQty(0);
            toStockBinVOBefore.setSellableQty(0);
        } else {
            toStockBinVOBefore = toStockBinPageResult.getData().get(0);
        }


        // 装配请求参数
        WmsStockBinMoveSaveReqVO createReqVO = new WmsStockBinMoveSaveReqVO();
        createReqVO.setWarehouseId(warehouseId);

        WmsStockBinMoveItemSaveReqVO itemSaveReqVO = new WmsStockBinMoveItemSaveReqVO();
        itemSaveReqVO.setProductId(fromStockBinVOBefore.getProductId());
        itemSaveReqVO.setFromBinId(fromStockBinVOBefore.getBinId());
        itemSaveReqVO.setToBinId(toStockBin.getId());
        itemSaveReqVO.setQty(qty);
        createReqVO.setItemList(List.of(itemSaveReqVO));
        CommonResult<Long> postResult=createStockBinMove(createReqVO);

        if(postResult.isError()) {
            Assert.assertTrue("创建库位库存移动失败",false);
        }


        CommonResult<List<WmsStockBinRespVO>> fromStockBinResultAfter = getStockBin(warehouseId, fromStockBinVOBefore.getBinId(), fromStockBinVOBefore.getProductId());
        CommonResult<List<WmsStockBinRespVO>> toStockBinResultAfter = getStockBin(warehouseId, toStockBinVOBefore.getBinId(), toStockBinVOBefore.getProductId());

        WmsStockBinRespVO fromStockBinVOAfter = fromStockBinResultAfter.getData().get(0);
        WmsStockBinRespVO toStockBinVOAfter = toStockBinResultAfter.getData().get(0);

        Assert.assertTrue("库存数量-FROM",fromStockBinVOAfter.getAvailableQty()==fromStockBinVOBefore.getAvailableQty()-qty);
        Assert.assertTrue("库存数量-TO",toStockBinVOAfter.getAvailableQty()==toStockBinVOBefore.getAvailableQty()+qty);

        Assert.assertTrue("可售数量-FROM",fromStockBinVOAfter.getSellableQty()==fromStockBinVOBefore.getSellableQty()-qty);
        Assert.assertTrue("可售数量-TO",toStockBinVOAfter.getSellableQty()==toStockBinVOBefore.getSellableQty()+qty);

        System.out.println();


    }


    public static void main(String[] args) {
        WmsStockBinMoveTest wmsStockBinMoveTest = new WmsStockBinMoveTest();
        wmsStockBinMoveTest.setProfile(Profile.LOCAL);
        wmsStockBinMoveTest.testMove();
    }


}
