package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 采购订单明项目 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseOrderItemMapper extends BaseMapperX<ErpPurchaseOrderItemDO> {
    //根据id来获得list
    default List<ErpPurchaseOrderItemDO> selectListByItemIds(Collection<Long> itemIds) {
        return selectList(ErpPurchaseOrderItemDO::getId, itemIds);
    }

    default List<ErpPurchaseOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(ErpPurchaseOrderItemDO::getOrderId, orderId);
    }

    default List<ErpPurchaseOrderItemDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(ErpPurchaseOrderItemDO::getOrderId, orderIds);
    }

    default int deleteByOrderId(Long orderId) {
        return delete(ErpPurchaseOrderItemDO::getOrderId, orderId);
    }

    //根据purchaseApplyItemId查找
    default List<ErpPurchaseOrderItemDO> selectListByPurchaseApplyItemIds(Collection<Long> purchaseApplyItemIds) {
        return selectList(ErpPurchaseOrderItemDO::getPurchaseApplyItemId, purchaseApplyItemIds);
    }

    //根据ApplyItemId 查询数量
    default Long selectCountByPurchaseApplyItemId(Long purchaseApplyItemId) {
        return selectCount(ErpPurchaseOrderItemDO::getPurchaseApplyItemId, purchaseApplyItemId);
    }

    default MPJLambdaWrapper<ErpPurchaseOrderItemDO> getDOWrapper() {
        return new MPJLambdaWrapperX<ErpPurchaseOrderItemDO>()
            .selectAll(ErpPurchaseOrderItemDO.class);
    }

//    //BO
//    default MPJLambdaWrapper<ErpPurchaseOrderItemDO> getBOWrapper() {
//        return getDOWrapper()
//            .leftJoin(ErpPurchaseOrderDO.class, ErpPurchaseOrderDO::getId, ErpPurchaseOrderItemDO::getOrderId)
//            .selectAsClass(ErpPurchaseOrderDO.class, ErpPurchaseOrderItemBO.class);
//    }
}