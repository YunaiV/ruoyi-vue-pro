package cn.iocoder.yudao.module.wms.exchange;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectiveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeType;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;
import cn.iocoder.yudao.test.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * @author: LeeFJ
 * @date: 2025/4/18 9:56
 * @description:
 */
public class ExchangeTest extends BaseRestIntegrationTest {

    private final Long warehouseId = 32L;

    public void testExchange() {

        Map<Long, WmsExchangeDefectiveSaveReqVO> defectiveDOMap = generateDefectiveMap();
        WmsExchangeRespVO exchangeVO = createAndUpdate(defectiveDOMap);
        System.out.println();

    }

    private WmsExchangeRespVO createAndUpdate(Map<Long, WmsExchangeDefectiveSaveReqVO> defectiveDOMap) {

        WmsExchangeSaveReqVO saveReqVO = new WmsExchangeSaveReqVO();
        saveReqVO.setWarehouseId(warehouseId);
        saveReqVO.setType(WmsExchangeType.TO_DEFECTIVE.getValue());
        saveReqVO.setDefectiveList(new ArrayList<>(defectiveDOMap.values()));

        CommonResult<Long> exchangeCreateResult = this.wms().exchangeClient().createExchange(saveReqVO);

        CommonResult<WmsExchangeRespVO> exchangeGetResult = this.wms().exchangeClient().getExchange(exchangeCreateResult.getData());

        saveReqVO = BeanUtils.toBean(exchangeGetResult.getData(), WmsExchangeSaveReqVO.class);
        this.wms().exchangeClient().updateExchange(saveReqVO);

        return this.wms().exchangeClient().getExchange(exchangeCreateResult.getData()).getData();

    }

    private Map<Long, WmsExchangeDefectiveSaveReqVO> generateDefectiveMap() {

        Map<Long, WmsExchangeDefectiveSaveReqVO> map = new HashMap<>();


        CommonResult<PageResult<WmsStockBinRespVO>> binListResult = this.wms().stockBinClient().getStockBinPage(this.warehouseId);
        List<WmsStockBinRespVO> binList = binListResult.getData().getList();


        List<ErpProductRespVO> productList = new ArrayList<>();
        // 取300个产品
        CommonResult<PageResult<ErpProductRespVO>> productPageResult = this.erp().productClient().getProductPage(10,1);
        productList.addAll(productPageResult.getData().getList());
        Random random = new Random();
        //
        Integer productCount = random.nextInt(productList.size());
        if(productCount<4) {
            productCount=4;
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



            WmsExchangeDefectiveSaveReqVO itemVO=new WmsExchangeDefectiveSaveReqVO();

            itemVO.setProductId(productId);
            WmsStockBinRespVO fromBin =null;
            Integer quantity=0;
            while (true) {
                fromBin =binList.get(random.nextInt(binList.size()));
                quantity = random.nextInt(fromBin.getSellableQty());
                if(quantity>0) {
                    break;
                }
            }

            if(quantity>4) {
                quantity=4;
            }

            itemVO.setFromBinId(fromBin.getId());
            itemVO.setQty(quantity);

            Long toBinId = null;
            while (true) {
                toBinId = binList.get(random.nextInt(binList.size())).getId();
                if(!Objects.equals(itemVO.getFromBinId(),toBinId)) {
                    break;
                }
            }
            itemVO.setToBinId(toBinId);
            //
            map.put(productId,itemVO);

            if(productIds.size()>productCount) {
                break;
            }

        }

        return map;
    }


    public static void main(String[] args) {
        ExchangeTest wmsInventoryTest = new ExchangeTest();
        wmsInventoryTest.setProfile(Profile.LOCAL);
        wmsInventoryTest.testExchange();
    }

}
