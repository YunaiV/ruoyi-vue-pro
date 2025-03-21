package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ErpOrderInConvert {

    ErpOrderInConvert INSTANCE = Mappers.getMapper(ErpOrderInConvert.class);


    default ErpPurchaseInSaveReqVO convertToErpPurchaseInSaveReqVO() {

        return null;
    }


    default ErpPurchaseInSaveReqVO.Item convertToErpPurchaseInSaveReqVOItem(@Param("oldItem") ErpPurchaseOrderItemDO oldItem) {
        if (oldItem == null) {
            return null;
        }
        ErpPurchaseInSaveReqVO.Item target = new ErpPurchaseInSaveReqVO.Item();
        BeanUtils.copyProperties(oldItem, target); // 复制相同属性
        target.setId(null); // 创建时 ID 需要为 null
        target.setSource("采购项合并入库"); // 单据来源，默认写死
//        target.setOrderId(oldItem.getOrderId()); // 订单编号转 int 作为源单号
        return target;
    }

    // 批量转换方法
    default List<ErpPurchaseInSaveReqVO.Item> convertToErpPurchaseInSaveReqVOItems(List<ErpPurchaseOrderItemDO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
            .map(this::convertToErpPurchaseInSaveReqVOItem)
            .collect(Collectors.toList());
    }


}
