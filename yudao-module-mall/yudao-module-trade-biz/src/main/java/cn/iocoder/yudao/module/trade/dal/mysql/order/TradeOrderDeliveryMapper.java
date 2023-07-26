package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDeliveryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// TODO @puhui999：应该去掉啦
/**
 * 交易订单发货记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface TradeOrderDeliveryMapper extends BaseMapperX<TradeOrderDeliveryDO> {

    default List<TradeOrderDeliveryDO> selsectListByOrderIdAndItemIds(Long orderId, List<Long> orderItemIds) {
        return selectList(new LambdaQueryWrapperX<TradeOrderDeliveryDO>()
                .eq(TradeOrderDeliveryDO::getOrderId, orderId).in(TradeOrderDeliveryDO::getOrderItemId, orderItemIds));
    }

}
