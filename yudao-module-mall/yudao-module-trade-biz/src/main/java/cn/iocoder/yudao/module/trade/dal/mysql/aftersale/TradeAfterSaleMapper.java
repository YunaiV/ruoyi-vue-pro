package cn.iocoder.yudao.module.trade.dal.mysql.aftersale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSalePageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeAfterSaleMapper extends BaseMapperX<TradeAfterSaleDO> {

    default PageResult<TradeAfterSaleDO> selectPage(TradeAfterSalePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TradeAfterSaleDO>()
                .likeIfPresent(TradeAfterSaleDO::getNo, reqVO.getNo())
                .eqIfPresent(TradeAfterSaleDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TradeAfterSaleDO::getType, reqVO.getType())
                .eqIfPresent(TradeAfterSaleDO::getWay, reqVO.getWay())
                .likeIfPresent(TradeAfterSaleDO::getOrderNo, reqVO.getOrderNo())
                .likeIfPresent(TradeAfterSaleDO::getSpuName, reqVO.getSpuName())
                .betweenIfPresent(TradeAfterSaleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TradeAfterSaleDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer status, TradeAfterSaleDO update) {
        return update(update, new LambdaUpdateWrapper<TradeAfterSaleDO>()
                .eq(TradeAfterSaleDO::getId, id).eq(TradeAfterSaleDO::getStatus, status));
    }

    default TradeAfterSaleDO selectByPayRefundId(Long payRefundId) {
        return selectOne(TradeAfterSaleDO::getPayRefundId, payRefundId);
    }

}
