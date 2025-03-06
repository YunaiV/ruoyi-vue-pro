package cn.iocoder.yudao.module.erp.convert.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestOrderReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ErpOrderConvert {
    ErpOrderConvert INSTANCE = Mappers.getMapper(ErpOrderConvert.class);


    default ErpPurchaseOrderSaveReqVO.Item convertToErpPurchaseOrderSaveReqVOItem(ErpPurchaseRequestItemsDO itemDO, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap) {
        ErpPurchaseOrderSaveReqVO.Item item = new ErpPurchaseOrderSaveReqVO.Item();
        // 设置产品ID
        item.setProductId(itemDO.getProductId());
        // 设置申请数量
        item.setApplyCount(itemDO.getCount());
        // 设置仓库ID
        item.setWarehouseId(itemDO.getWarehouseId());
        // 设置审批数量
//        item.setApproveCount(itemDO.getApproveCount());
        // 设置实际含税单价
        item.setActTaxPrice(itemDO.getActTaxPrice());
        // 设置税率
        item.setTaxPercent(itemDO.getTaxPercent());

        //下单数量
//        item.setCount(requestItemsMap.get(itemDO.getId()).getOrderQuantity());

        return item;
    }

    //list
    default List<ErpPurchaseOrderSaveReqVO.Item> convertToErpPurchaseOrderSaveReqVOItemList(List<ErpPurchaseRequestItemsDO> itemDOList, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap) {
        return itemDOList.stream().map(itemDO -> convertToErpPurchaseOrderSaveReqVOItem(itemDO, requestItemsMap)).collect(Collectors.toList());
    }
}
