package cn.iocoder.yudao.module.wms.dal.mysql.order.receipt;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * WMS 入库单明细 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsReceiptOrderDetailMapper extends BaseMapperX<WmsReceiptOrderDetailDO> {

    default List<WmsReceiptOrderDetailDO> selectListByOrderId(Long orderId) {
        return selectList(WmsReceiptOrderDetailDO::getOrderId, orderId);
    }

    default List<WmsReceiptOrderDetailDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(new LambdaQueryWrapperX<WmsReceiptOrderDetailDO>()
                .inIfPresent(WmsReceiptOrderDetailDO::getOrderId, orderIds)
                .orderByAsc(WmsReceiptOrderDetailDO::getOrderId)
                .orderByAsc(WmsReceiptOrderDetailDO::getId));
    }

    default void deleteByOrderId(Long orderId) {
        delete(WmsReceiptOrderDetailDO::getOrderId, orderId);
    }

    default Long selectCountBySkuId(Long skuId) {
        return selectCount(WmsReceiptOrderDetailDO::getSkuId, skuId);
    }

}
