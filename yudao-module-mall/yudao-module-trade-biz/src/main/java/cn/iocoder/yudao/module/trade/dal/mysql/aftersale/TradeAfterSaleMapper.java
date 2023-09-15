package cn.iocoder.yudao.module.trade.dal.mysql.aftersale;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSalePageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

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

    default PageResult<TradeAfterSaleDO> selectPage(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<TradeAfterSaleDO>()
                .eqIfPresent(TradeAfterSaleDO::getUserId, userId)
                .orderByDesc(TradeAfterSaleDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer status, TradeAfterSaleDO update) {
        return update(update, new LambdaUpdateWrapper<TradeAfterSaleDO>()
                .eq(TradeAfterSaleDO::getId, id).eq(TradeAfterSaleDO::getStatus, status));
    }

    default TradeAfterSaleDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(TradeAfterSaleDO::getId, id,
                TradeAfterSaleDO::getUserId, userId);
    }

    default Long selectCountByUserIdAndStatus(Long userId, Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<TradeAfterSaleDO>()
                .eq(TradeAfterSaleDO::getUserId, userId)
                .in(TradeAfterSaleDO::getStatus, statuses));
    }

}
