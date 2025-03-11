package cn.iocoder.yudao.module.erp.convert.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestOrderReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ErpOrderConvert {
    ErpOrderConvert INSTANCE = Mappers.getMapper(ErpOrderConvert.class);


    default ErpPurchaseOrderSaveReqVO.Item convertToErpPurchaseOrderSaveReqVOItem(ErpPurchaseRequestItemsDO itemDO, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap, Map<Long, ErpPurchaseRequestItemsDO> itemDOMap) {
        ErpPurchaseOrderSaveReqVO.Item item = new ErpPurchaseOrderSaveReqVO.Item();
        // 设置产品ID
        item.setProductId(itemDO.getProductId());
        // 设置下单数量(采购)
//        item.setCount(itemDO.getCount());
        // 设置仓库ID
        item.setWarehouseId(itemDO.getWarehouseId());
        // 设置审批数量
//        item.setApproveCount(itemDO.getApproveCount());
        // 设置税率
        item.setTaxPercent(itemDO.getTaxPercent());
        //采购订单的申请数量
        //产品项
        item.setPurchaseApplyItemId(itemDO.getId());
        //含税单价
        item.setProductPrice(itemDOMap.get(itemDO.getId()).getActTaxPrice());
        //税率
        item.setTaxPercent(itemDOMap.get(itemDO.getId()).getTaxPercent());
        // 设置下单数量(采购)
        item.setCount(requestItemsMap.get(itemDO.getId()).getOrderQuantity());
        item.setWarehouseId(itemDOMap.get(itemDO.getId()).getWarehouseId());
        // 自动映射其他属性
//        item.setRemark(itemDOMap.get(itemDO.getId()).getRemark());
//        item.setUnitId(itemDOMap.get(itemDO.getId()).getUnitId());
        item.setTaxPercent(itemDOMap.get(itemDO.getId()).getTaxPercent());
        item.setTaxPrice(itemDOMap.get(itemDO.getId()).getTaxPrice());
        item.setDeliveryTime(itemDOMap.get(itemDO.getId()).getExpectArrivalDate());
        return item;
    }

    //list
    default List<ErpPurchaseOrderSaveReqVO.Item> convertToErpPurchaseOrderSaveReqVOItemList(List<ErpPurchaseRequestItemsDO> itemDOList, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap, Map<Long, ErpPurchaseRequestItemsDO> itemDOMap) {
        return itemDOList.stream().map(itemDO -> convertToErpPurchaseOrderSaveReqVOItem(itemDO, requestItemsMap, itemDOMap)).collect(Collectors.toList());
    }

    //ErpPurchaseOrderSaveReqVO.Item->ErpPurchaseOrderItemDO

//    ErpPurchaseOrderItemDO convertToErpPurchaseOrderItemDO(ErpPurchaseOrderSaveReqVO.Item item);
}
