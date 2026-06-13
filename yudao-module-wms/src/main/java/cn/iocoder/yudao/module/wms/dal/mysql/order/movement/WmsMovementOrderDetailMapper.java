package cn.iocoder.yudao.module.wms.dal.mysql.order.movement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * WMS 移库单明细 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsMovementOrderDetailMapper extends BaseMapperX<WmsMovementOrderDetailDO> {

    default List<WmsMovementOrderDetailDO> selectListByOrderId(Long orderId) {
        return selectList(WmsMovementOrderDetailDO::getOrderId, orderId);
    }

    default List<WmsMovementOrderDetailDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(new LambdaQueryWrapperX<WmsMovementOrderDetailDO>()
                .inIfPresent(WmsMovementOrderDetailDO::getOrderId, orderIds)
                .orderByAsc(WmsMovementOrderDetailDO::getOrderId)
                .orderByAsc(WmsMovementOrderDetailDO::getId));
    }

    default void deleteByOrderId(Long orderId) {
        delete(WmsMovementOrderDetailDO::getOrderId, orderId);
    }

    default Long selectCountBySkuId(Long skuId) {
        return selectCount(WmsMovementOrderDetailDO::getSkuId, skuId);
    }

}
