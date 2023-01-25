package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface TradeOrderItemMapper extends BaseMapperX<TradeOrderItemDO> {

    default int updateAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus) {
        return update(new TradeOrderItemDO().setAfterSaleStatus(newAfterSaleStatus),
                new LambdaUpdateWrapper<>(new TradeOrderItemDO().setId(id).setAfterSaleStatus(oldAfterSaleStatus)));
    }

    default List<TradeOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(TradeOrderItemDO::getOrderId, orderId);
    }

    default List<TradeOrderItemDO> selectListByOrderId(Collection<Long> orderIds) {
        return selectList(TradeOrderItemDO::getOrderId, orderIds);
    }

}
