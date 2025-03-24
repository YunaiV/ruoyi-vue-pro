package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 采购入库项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseInItemMapper extends BaseMapperX<ErpPurchaseInItemDO> {

    default List<ErpPurchaseInItemDO> selectListByInId(Long inId) {
        return selectList(ErpPurchaseInItemDO::getInId, inId);
    }

    default List<ErpPurchaseInItemDO> selectListByInIds(Collection<Long> inIds) {
        return selectList(ErpPurchaseInItemDO::getInId, inIds);
    }

    default int deleteByInId(Long inId) {
        return delete(ErpPurchaseInItemDO::getInId, inId);
    }

    //根据订单项id获得入库项item数量
    default Long sumInItemsByItemId(Long orderItemId) {
        return selectCount(ErpPurchaseInItemDO::getOrderItemId, orderItemId);
    }

    //根据orderItemId找对应的入库项
    default List<ErpPurchaseInItemDO> selectListByOrderItemId(Long orderItemId) {
        return selectList(ErpPurchaseInItemDO::getOrderItemId, orderItemId);
    }

    //根据orderItemId是否存在入库项
    default boolean existsByOrderItemId(Long orderItemId) {
        return selectCount(ErpPurchaseInItemDO::getOrderItemId, orderItemId) > 0;
    }
}