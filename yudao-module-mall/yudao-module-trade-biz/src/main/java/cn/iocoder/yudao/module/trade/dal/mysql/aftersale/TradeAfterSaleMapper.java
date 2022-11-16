package cn.iocoder.yudao.module.trade.dal.mysql.aftersale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeAfterSaleMapper extends BaseMapperX<TradeAfterSaleDO> {

    default int updateByIdAndStatus(Long id, Integer status, TradeAfterSaleDO update) {
        return update(update, new LambdaUpdateWrapper<TradeAfterSaleDO>()
                .eq(TradeAfterSaleDO::getId, id).eq(TradeAfterSaleDO::getStatus, status));
    }

    default TradeAfterSaleDO selectByPayRefundId(Long payRefundId) {
        return selectOne(TradeAfterSaleDO::getPayRefundId, payRefundId);
    }

}
