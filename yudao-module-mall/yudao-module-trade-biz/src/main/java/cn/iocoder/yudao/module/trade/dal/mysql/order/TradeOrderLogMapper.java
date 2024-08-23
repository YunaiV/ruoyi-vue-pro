package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TradeOrderLogMapper extends BaseMapperX<TradeOrderLogDO> {

    default List<TradeOrderLogDO> selectListByOrderId(Long orderId) {
        LambdaQueryWrapper<TradeOrderLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeOrderLogDO::getOrderId, orderId);
        queryWrapper.orderByDesc(TradeOrderLogDO::getCreateTime);
        return selectList(queryWrapper);
    }

}
