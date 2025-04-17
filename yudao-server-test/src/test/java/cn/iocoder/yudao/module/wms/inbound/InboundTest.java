package cn.iocoder.yudao.module.wms.inbound;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundType;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;
import cn.iocoder.yudao.test.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author: LeeFJ
 * @date: 2025/4/17 10:52
 * @description:
 */
public class InboundTest extends BaseRestIntegrationTest {

    /**
     * all 全部 ,random : 随机,fix-1 : 固定值1, 依此类推 fix-n
     **/
    private final String quantityMode="random";
    private final Long warehouseId = 32L;



    private final Long companyId=1L;
    private final Long deptId=50012L;




    public void testInbound() {

        // 生成随机入库产品清单
        Map<Long, WmsInboundItemDO> quantityMap = generateQuantityMap();
        // 创建并更新
        WmsInboundRespVO inboundRespVO = createAndUpdate(quantityMap);
        // 提交入库单
        this.wms().inboundClient().submit(inboundRespVO.getId());
        // 更新实际入库数量
        updateActualQuantity(inboundRespVO);
        // 同意入库单
        this.wms().inboundClient().agree(inboundRespVO.getId());
        //

    }

    private void updateActualQuantity(WmsInboundRespVO inboundRespVO) {


        List<WmsInboundItemSaveReqVO> itemList = new ArrayList<>();
        for (WmsInboundItemRespVO itemRespVO : inboundRespVO.getItemList()) {
            WmsInboundItemSaveReqVO saveReqVO=new WmsInboundItemSaveReqVO();
            saveReqVO.setId(itemRespVO.getId());
            saveReqVO.setActualQty(itemRespVO.getPlanQty());
            saveReqVO.setInboundId(itemRespVO.getInboundId());
            itemList.add(saveReqVO);
        }

        this.wms().inboundClient().updateInventoryActualQuantity(itemList);

    }

    private WmsInboundRespVO createAndUpdate(Map<Long, WmsInboundItemDO> quantityMap) {

        WmsInboundSaveReqVO inboundSaveReqVO=new WmsInboundSaveReqVO();
        inboundSaveReqVO.setWarehouseId(warehouseId);
        inboundSaveReqVO.setType(WmsInboundType.MANUAL.getValue());
        inboundSaveReqVO.setCompanyId(companyId);
        inboundSaveReqVO.setDeptId(deptId);
        //
        List<WmsInboundItemSaveReqVO> itemList = BeanUtils.toBean(new ArrayList<>(quantityMap.values()), WmsInboundItemSaveReqVO.class);
        inboundSaveReqVO.setItemList(itemList);
        CommonResult<Long> createResult = this.wms().inboundClient().createInbound(inboundSaveReqVO);
        Long inboundId = createResult.getData();

        CommonResult<WmsInboundRespVO> inboundResult = this.wms().inboundClient().getInbound(inboundId);

        inboundSaveReqVO = BeanUtils.toBean(inboundResult.getCheckedData(), WmsInboundSaveReqVO.class);

        Random random = new Random();
        for (WmsInboundItemSaveReqVO inboundItemSaveReqVO : itemList) {
            inboundItemSaveReqVO.setPlanQty(inboundItemSaveReqVO.getPlanQty()+random.nextInt(30));
            //
            quantityMap.get(inboundItemSaveReqVO.getProductId()).setPlanQty(inboundItemSaveReqVO.getPlanQty());
            quantityMap.get(inboundItemSaveReqVO.getProductId()).setActualQty(inboundItemSaveReqVO.getPlanQty());
        }
        this.wms().inboundClient().updateInbound(inboundSaveReqVO);

        inboundResult = this.wms().inboundClient().getInbound(inboundId);

        return inboundResult.getCheckedData();
    }


    private Map<Long, WmsInboundItemDO> generateQuantityMap() {

        Map<Long, WmsInboundItemDO> quantityMap = new HashMap<>();

        List<ErpProductRespVO> productList = new ArrayList<>();
        // 取300个产品
        CommonResult<PageResult<ErpProductRespVO>> productPageResult = null;
        productPageResult = this.erp().productClient().getProductPage(100,1);
        productList.addAll(productPageResult.getCheckedData().getList());
        productPageResult = this.erp().productClient().getProductPage(100,2);
        productList.addAll(productPageResult.getCheckedData().getList());
        productPageResult = this.erp().productClient().getProductPage(100,3);
        productList.addAll(productPageResult.getCheckedData().getList());

        Random random = new Random();
        //
        Integer productCount = random.nextInt(productList.size());
        if(productCount<10) {
            productCount=10;
        }

        Set<Long> productIds = new HashSet<>();


        Integer index=-1;
        while (true) {
            Long productId = null;
            while (true) {
                index = random.nextInt(productList.size());
                productId = productList.get(index).getId();
                if (productIds.contains(productId)) {
                    continue;
                }
                productIds.add(productId);
                break;
            }

            var quantity = 5 + random.nextInt(30);

            WmsInboundItemDO inboundItemDO=new WmsInboundItemDO();
            inboundItemDO.setPlanQty(quantity);
            inboundItemDO.setActualQty(quantity);
            inboundItemDO.setProductId(productId);
            quantityMap.put(productId,inboundItemDO);
            if(productIds.size()>productCount) {
                break;
            }
        }

        return quantityMap;
    }



    public static void main(String[] args) {
        InboundTest wmsInventoryTest = new InboundTest();
        wmsInventoryTest.setProfile(Profile.LOCAL);
        wmsInventoryTest.testInbound();
    }




}
