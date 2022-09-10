package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Mapper
public interface TradeOrderMapper extends BaseMapperX<TradeOrderDO> {
}
