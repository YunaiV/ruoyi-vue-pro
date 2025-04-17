package cn.iocoder.yudao.module.wms.stock.ownership.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;
import cn.iocoder.yudao.test.Profile;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:46
 * @description: 库存所有者库存移动测试
 */


public class WmsStockOwnershipMoveTest extends BaseRestIntegrationTest {


    @Test
    public void testMove() {

        Long warehouseId = 32L;
        Integer qty=1;


        // 确定从哪个所有者库存出哪个产品
        CommonResult<PageResult<WmsStockOwnershipRespVO>> stockOwnershipPageResult = this.wms().stockOwnershipClient().getStockOwnershipPage(warehouseId);
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


        // 找一个合适的目标归属
        WmsStockOwnershipRespVO toStockOwnershipVOBefore = null;
        for (WmsStockOwnershipRespVO stockOwnershipRespVO : stockOwnershipList) {
            if(stockOwnershipRespVO.getCompanyId().equals(fromStockOwnershipVOBefore.getCompanyId()) && stockOwnershipRespVO.getDeptId().equals(fromStockOwnershipVOBefore.getDeptId())) {
                continue;
            }
            toStockOwnershipVOBefore = stockOwnershipRespVO;
            break;
        }

        if(toStockOwnershipVOBefore==null) {
            System.err.println("缺少目标，无法继续测试");
            return;
        }

        System.out.println("FromBefore("+fromStockOwnershipVOBefore.getProductId()+"):"+fromStockOwnershipVOBefore.getAvailableQty()+",companyId="+fromStockOwnershipVOBefore.getCompanyId()+",deptId="+fromStockOwnershipVOBefore.getDeptId());
        System.out.println("toBefore("+toStockOwnershipVOBefore.getProductId()+"):"+toStockOwnershipVOBefore.getAvailableQty()+",companyId="+toStockOwnershipVOBefore.getCompanyId()+",deptId="+toStockOwnershipVOBefore.getDeptId());


        CommonResult<List<WmsStockOwnershipRespVO>> stockOwnershipResult = this.wms().stockOwnershipClient().getStockOwnership(warehouseId, toStockOwnershipVOBefore.getCompanyId(), toStockOwnershipVOBefore.getDeptId(), fromStockOwnershipVOBefore.getProductId());
        if(stockOwnershipResult.isError()) {
            toStockOwnershipVOBefore.setProductId(fromStockOwnershipVOBefore.getProductId());
            toStockOwnershipVOBefore.setAvailableQty(0);
        } else {
            for (WmsStockOwnershipRespVO e : stockOwnershipResult.getData()) {
                if(Objects.equals(e.getCompanyId(), toStockOwnershipVOBefore.getCompanyId()) && Objects.equals(e.getDeptId(), toStockOwnershipVOBefore.getDeptId())) {
                    toStockOwnershipVOBefore=e;
                    break;
                }
            }
        }




        System.out.println("FromBefore("+fromStockOwnershipVOBefore.getProductId()+"):"+fromStockOwnershipVOBefore.getAvailableQty()+",companyId="+fromStockOwnershipVOBefore.getCompanyId()+",deptId="+fromStockOwnershipVOBefore.getDeptId());
        System.out.println("toBefore("+toStockOwnershipVOBefore.getProductId()+"):"+toStockOwnershipVOBefore.getAvailableQty()+",companyId="+toStockOwnershipVOBefore.getCompanyId()+",deptId="+toStockOwnershipVOBefore.getDeptId());



        // 装配请求参数
        WmsStockOwnershipMoveSaveReqVO createReqVO = new WmsStockOwnershipMoveSaveReqVO();
        createReqVO.setWarehouseId(warehouseId);

        WmsStockOwnershipMoveItemSaveReqVO itemSaveReqVO = new WmsStockOwnershipMoveItemSaveReqVO();
        itemSaveReqVO.setProductId(fromStockOwnershipVOBefore.getProductId());
        itemSaveReqVO.setFromCompanyId(fromStockOwnershipVOBefore.getCompanyId());
        itemSaveReqVO.setFromDeptId(fromStockOwnershipVOBefore.getDeptId());
        itemSaveReqVO.setToCompanyId(toStockOwnershipVOBefore.getCompanyId());
        itemSaveReqVO.setToDeptId(toStockOwnershipVOBefore.getDeptId());
        itemSaveReqVO.setQty(qty);

        createReqVO.setItemList(List.of(itemSaveReqVO));




        CommonResult<Long> postResult=this.wms().stockOwnershipClient().createStockOwnershipMove(createReqVO);

        if(postResult.isError()) {
            Assert.assertTrue("创建所有者库存移动失败",false);
        }


        CommonResult<List<WmsStockOwnershipRespVO>> fromStockOwnershipResultAfter = this.wms().stockOwnershipClient().getStockOwnership(warehouseId, fromStockOwnershipVOBefore.getCompanyId(),fromStockOwnershipVOBefore.getDeptId(), fromStockOwnershipVOBefore.getProductId());
        CommonResult<List<WmsStockOwnershipRespVO>> toStockOwnershipResultAfter = this.wms().stockOwnershipClient().getStockOwnership(warehouseId, toStockOwnershipVOBefore.getCompanyId(),toStockOwnershipVOBefore.getDeptId(), fromStockOwnershipVOBefore.getProductId());

        WmsStockOwnershipRespVO fromStockOwnershipVOAfter = fromStockOwnershipResultAfter.getData().get(0);
        WmsStockOwnershipRespVO toStockOwnershipVOAfter = toStockOwnershipResultAfter.getData().get(0);

        System.out.println("FromAfter("+fromStockOwnershipVOAfter.getProductId()+"):"+fromStockOwnershipVOAfter.getAvailableQty()+",companyId="+fromStockOwnershipVOAfter.getCompanyId()+",deptId="+fromStockOwnershipVOAfter.getDeptId()+"");
        System.out.println("toAfter("+toStockOwnershipVOAfter.getProductId()+"):"+toStockOwnershipVOAfter.getAvailableQty()+",companyId="+toStockOwnershipVOAfter.getCompanyId()+",deptId="+toStockOwnershipVOAfter.getDeptId());

        System.out.println("FROM\t"+fromStockOwnershipVOBefore.getAvailableQty() +" -> " +  fromStockOwnershipVOAfter.getAvailableQty()+" , "+qty);
        System.out.println("TO\t"+toStockOwnershipVOBefore.getAvailableQty() +" -> " +  toStockOwnershipVOAfter.getAvailableQty()+" , "+qty);

        Assert.assertTrue("库存数量-FROM",fromStockOwnershipVOAfter.getAvailableQty()==fromStockOwnershipVOBefore.getAvailableQty()-qty);
        Assert.assertTrue("库存数量-TO",toStockOwnershipVOAfter.getAvailableQty()==toStockOwnershipVOBefore.getAvailableQty()+qty);



        System.out.println();


    }




    public static void main(String[] args) {
        WmsStockOwnershipMoveTest stockBinMoveTest = new WmsStockOwnershipMoveTest();
        stockBinMoveTest.setProfile(Profile.LOCAL);
        stockBinMoveTest.testMove();
    }


}
