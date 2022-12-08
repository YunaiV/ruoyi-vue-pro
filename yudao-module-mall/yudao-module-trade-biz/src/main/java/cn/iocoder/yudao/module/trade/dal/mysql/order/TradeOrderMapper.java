package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeOrderMapper extends BaseMapperX<TradeOrderDO> {

    default int updateByIdAndStatus(Long id, Integer status, TradeOrderDO update) {
        return update(update, new LambdaUpdateWrapper<TradeOrderDO>()
                .eq(TradeOrderDO::getId, id).eq(TradeOrderDO::getStatus, status));
    }

    default TradeOrderDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(TradeOrderDO::getId, id, TradeOrderDO::getUserId, userId);
    }

    default PageResult<TradeOrderDO> selectPage(TradeOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TradeOrderDO>()
                .eqIfPresent(TradeOrderDO::getStatus, reqVO.getStatus()));
    }

}
