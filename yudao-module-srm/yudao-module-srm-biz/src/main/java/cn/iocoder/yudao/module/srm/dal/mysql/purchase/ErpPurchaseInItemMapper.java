package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseInItemDO;
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
}