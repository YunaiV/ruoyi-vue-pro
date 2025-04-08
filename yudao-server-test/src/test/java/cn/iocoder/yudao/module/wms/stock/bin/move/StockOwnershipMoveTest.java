package cn.iocoder.yudao.module.wms.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.WmsBaseTest;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.test.Profile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:46
 * @description:
 */


public class StockOwnershipMoveTest extends WmsBaseTest {


    @Test
    public void testMove() {

        Long warehouseId = 32L;
        Integer qty=1;


        // 确定从哪个所有者库存出哪个产品
        CommonResult<PageResult<WmsStockOwnershipRespVO>> stockOwnershipPageResult = this.getStockOwnershipPage();
        if(stockOwnershipPageResult.isError()) {
            System.err.println("缺少库存数据，无法继续测试");
            return;
        }
        List<WmsStockOwnershipRespVO> stockOwnershipList = stockOwnershipPageResult.getData().getList();
        WmsStockOwnershipRespVO fromStockOwnershipVOBefore = stockOwnershipList.get(0);
        for (WmsStockOwnershipRespVO stockOwnershipRespVO : stockOwnershipList) {
            if(stockOwnershipRespVO.getAvailableQty()>0) {
                fromStockOwnershipVOBefore=stockOwnershipRespVO;
                break;
            }
        }


        // 找一个合适的目标仓位
        WmsStockOwnershipRespVO toStockOwnershipBefore = null;
        for (WmsStockOwnershipRespVO stockOwnershipRespVO : stockOwnershipList) {
            if(stockOwnershipRespVO.getCompanyId().equals(fromStockOwnershipVOBefore.getCompanyId()) && stockOwnershipRespVO.getDeptId().equals(fromStockOwnershipVOBefore.getDeptId())) {
                continue;
            }
            toStockOwnershipBefore = stockOwnershipRespVO;
            break;
        }

        if(toStockOwnershipBefore==null) {
            System.err.println("缺少目标，无法继续测试");
            return;
        }



        // 装配请求参数
        WmsStockOwnershipMoveSaveReqVO createReqVO = new WmsStockOwnershipMoveSaveReqVO();
        createReqVO.setWarehouseId(warehouseId);

        WmsStockOwnershipMoveItemSaveReqVO itemSaveReqVO = new WmsStockOwnershipMoveItemSaveReqVO();
        itemSaveReqVO.setProductId(fromStockOwnershipVOBefore.getProductId());
        itemSaveReqVO.setFromCompanyId(fromStockOwnershipVOBefore.getCompanyId());
        itemSaveReqVO.setFromDeptId(fromStockOwnershipVOBefore.getDeptId());
        itemSaveReqVO.setToCompanyId(toStockOwnershipBefore.getCompanyId());
        itemSaveReqVO.setToDeptId(toStockOwnershipBefore.getDeptId());
        itemSaveReqVO.setQty(qty);

        createReqVO.setItemList(List.of(itemSaveReqVO));
        CommonResult<Long> postResult=createStockOwnershipMove(createReqVO);

        if(postResult.isError()) {
            Assert.assertTrue("创建所有者库存移动失败",false);
        }


        CommonResult<List<WmsStockOwnershipRespVO>> fromStockOwnershipResultAfter = getStockOwnership(warehouseId, fromStockOwnershipVOBefore.getCompanyId(),fromStockOwnershipVOBefore.getDeptId(), fromStockOwnershipVOBefore.getProductId());
        CommonResult<List<WmsStockOwnershipRespVO>> toStockOwnershipResultAfter = getStockOwnership(warehouseId, toStockOwnershipBefore.getCompanyId(),toStockOwnershipBefore.getDeptId(), fromStockOwnershipVOBefore.getProductId());

        WmsStockOwnershipRespVO fromStockOwnershipVOAfter = fromStockOwnershipResultAfter.getData().get(0);
        WmsStockOwnershipRespVO toStockOwnershipVOAfter = toStockOwnershipResultAfter.getData().get(0);

        Assert.assertTrue("库存数量-FROM",fromStockOwnershipVOAfter.getAvailableQty()==fromStockOwnershipVOBefore.getAvailableQty()-qty);
        Assert.assertTrue("库存数量-TO",toStockOwnershipVOAfter.getAvailableQty()==toStockOwnershipBefore.getAvailableQty()+qty);



        System.out.println();


    }




    public static void main(String[] args) {
        StockOwnershipMoveTest stockBinMoveTest = new StockOwnershipMoveTest();
        stockBinMoveTest.setProfile(Profile.LOCAL);
        stockBinMoveTest.testMove();
    }


}
