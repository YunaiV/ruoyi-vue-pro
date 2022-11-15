package cn.iocoder.yudao.module.trade.dal.mysql.aftersale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeAfterSaleMapper extends BaseMapperX<TradeAfterSaleDO> {
}
