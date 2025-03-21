package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.ErpPurchaseRequestOrderReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ErpOrderConvert {
    ErpOrderConvert INSTANCE = Mappers.getMapper(ErpOrderConvert.class);


    /**
     * 让采购项关联申请项 采购项:申请项 N:1
     *
     * @param itemDO          采购项
     * @param requestItemsMap ?
     * @param itemDOMap       采购项Map
     * @return 采购订单的入参reqVO的item
     */
    default ErpPurchaseOrderSaveReqVO.Item convertToErpPurchaseOrderSaveReqVOItem(ErpPurchaseRequestItemsDO itemDO, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap, Map<Long, ErpPurchaseRequestItemsDO> itemDOMap) {
        ErpPurchaseOrderSaveReqVO.Item item = new ErpPurchaseOrderSaveReqVO.Item();
        // 设置产品ID
        item.setProductId(itemDO.getProductId());
        // 设置税率
        item.setTaxPercent(itemDO.getTaxPercent());
        //采购订单的申请数量
        //产品项
        item.setPurchaseApplyItemId(itemDO.getId());
        //含税单价
        item.setActTaxPrice(itemDOMap.get(itemDO.getId()).getActTaxPrice());
        //税率
        item.setTaxPercent(itemDOMap.get(itemDO.getId()).getTaxPercent());
        // 设置下单数量(采购) == 申请项批准数量
        item.setCount(itemDO.getApproveCount());
        item.setWarehouseId(itemDOMap.get(itemDO.getId()).getWarehouseId());
        //价税合计
        item.setAllAmount(itemDO.getAllAmount());
        // 自动映射其他属性
        item.setTaxPercent(itemDOMap.get(itemDO.getId()).getTaxPercent());
        item.setTaxPrice(itemDOMap.get(itemDO.getId()).getTaxPrice());
        item.setDeliveryTime(itemDOMap.get(itemDO.getId()).getExpectArrivalDate());
        //
        //申请项id
        item.setPurchaseApplyItemId(itemDO.getId());

        return item;
    }

    //list
    default List<ErpPurchaseOrderSaveReqVO.Item> convertToErpPurchaseOrderSaveReqVOItemList(List<ErpPurchaseRequestItemsDO> itemDOList, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap, Map<Long, ErpPurchaseRequestItemsDO> itemDOMap) {
        return itemDOList.stream().map(itemDO -> convertToErpPurchaseOrderSaveReqVOItem(itemDO, requestItemsMap, itemDOMap)).collect(Collectors.toList());
    }

    //ErpPurchaseOrderSaveReqVO.Item->ErpPurchaseOrderItemDO

//    ErpPurchaseOrderItemDO convertToErpPurchaseOrderItemDO(ErpPurchaseOrderSaveReqVO.Item item);
}
