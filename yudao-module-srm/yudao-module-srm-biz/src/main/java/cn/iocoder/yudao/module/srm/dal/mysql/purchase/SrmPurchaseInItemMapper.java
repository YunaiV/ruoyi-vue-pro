package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 采购入库项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseInItemMapper extends BaseMapperX<SrmPurchaseInItemDO> {

    default List<SrmPurchaseInItemDO> selectListByInId(Long inId) {
        return selectList(SrmPurchaseInItemDO::getInId, inId);
    }

    default List<SrmPurchaseInItemDO> selectListByInIds(Collection<Long> inIds) {
        return selectList(SrmPurchaseInItemDO::getInId, inIds);
    }

    default int deleteByInId(Long inId) {
        return delete(SrmPurchaseInItemDO::getInId, inId);
    }

    //根据订单项id获得入库项item数量
    default Long sumInItemsByItemId(Long orderItemId) {
        return selectCount(SrmPurchaseInItemDO::getOrderItemId, orderItemId);
    }

    //根据orderItemId找对应的入库项
    default List<SrmPurchaseInItemDO> selectListByOrderItemId(Long orderItemId) {
        return selectList(SrmPurchaseInItemDO::getOrderItemId, orderItemId);
    }

    //根据orderItemId是否存在入库项
    default boolean existsByOrderItemId(Long orderItemId) {
        return selectCount(SrmPurchaseInItemDO::getOrderItemId, orderItemId) > 0;
    }
}