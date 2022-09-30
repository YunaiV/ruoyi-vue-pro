package cn.iocoder.yudao.module.trade.dal.mysql.orderitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeOrderItemMapper extends BaseMapperX<TradeOrderItemDO> {
}
