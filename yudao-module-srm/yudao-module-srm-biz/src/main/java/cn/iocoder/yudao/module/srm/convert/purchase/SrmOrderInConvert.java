package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SrmOrderInConvert {

    SrmOrderInConvert INSTANCE = Mappers.getMapper(SrmOrderInConvert.class);

    default SrmPurchaseInSaveReqVO convertToErpPurchaseInSaveReqVO() {

        return null;
    }

    /**
     * 订单项转换成采购入库项vo.item
     *
     * @param orderItemDO 订单项
     * @return 入库项vo
     */
    default SrmPurchaseInSaveReqVO.Item convertToErpPurchaseInSaveReqVOItem(@Param("orderItemDO") SrmPurchaseOrderItemDO orderItemDO) {
        if (orderItemDO == null) {
            return null;
        }
        SrmPurchaseInSaveReqVO.Item inVOItem = new SrmPurchaseInSaveReqVO.Item();
        BeanUtils.copyProperties(orderItemDO, inVOItem);
        inVOItem.setId(null); // 创建时 ID 需要为 null
        inVOItem.setSource("采购项合并入库"); // 单据来源，默认写死
        //        inVOItem.setOrderId(orderItemDO.getOrderId()); // 订单编号转 int 作为源单号
        return inVOItem;
    }

    // 批量转换方法
    default List<SrmPurchaseInSaveReqVO.Item> convertToErpPurchaseInSaveReqVOItems(List<SrmPurchaseOrderItemDO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream().map(this::convertToErpPurchaseInSaveReqVOItem).collect(Collectors.toList());
    }

}
