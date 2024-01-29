package cn.iocoder.yudao.module.trade.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface TradeOrderMapper extends BaseMapperX<TradeOrderDO> {

    default int updateByIdAndStatus(Long id, Integer status, TradeOrderDO update) {
        return update(update, new LambdaUpdateWrapper<TradeOrderDO>()
                .eq(TradeOrderDO::getId, id).eq(TradeOrderDO::getStatus, status));
    }

    default TradeOrderDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(TradeOrderDO::getId, id, TradeOrderDO::getUserId, userId);
    }

    default PageResult<TradeOrderDO> selectPage(TradeOrderPageReqVO reqVO, Set<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TradeOrderDO>()
                .likeIfPresent(TradeOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(TradeOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(TradeOrderDO::getDeliveryType, reqVO.getDeliveryType())
                .inIfPresent(TradeOrderDO::getUserId, userIds)
                .eqIfPresent(TradeOrderDO::getType, reqVO.getType())
                .eqIfPresent(TradeOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TradeOrderDO::getPayChannelCode, reqVO.getPayChannelCode())
                .eqIfPresent(TradeOrderDO::getTerminal, reqVO.getTerminal())
                .eqIfPresent(TradeOrderDO::getLogisticsId, reqVO.getLogisticsId())
                .inIfPresent(TradeOrderDO::getPickUpStoreId, reqVO.getPickUpStoreIds())
                .likeIfPresent(TradeOrderDO::getPickUpVerifyCode, reqVO.getPickUpVerifyCode())
                .betweenIfPresent(TradeOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TradeOrderDO::getId));
    }

    // TODO @疯狂：如果用 map 返回，要不这里直接用 TradeOrderSummaryRespVO 返回？也算合理，就当  sql 查询出这么个玩意~~
    default List<Map<String, Object>> selectOrderSummaryGroupByRefundStatus(TradeOrderPageReqVO reqVO, Set<Long> userIds) {
        return selectMaps(new MPJLambdaWrapperX<TradeOrderDO>()
                .selectAs(TradeOrderDO::getRefundStatus, TradeOrderDO::getRefundStatus)  // 售后状态
                .selectCount(TradeOrderDO::getId, "count") // 售后状态对应的数量
                .selectSum(TradeOrderDO::getPayPrice, "price")  // 售后状态对应的支付金额
                .likeIfPresent(TradeOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(TradeOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(TradeOrderDO::getDeliveryType, reqVO.getDeliveryType())
                .inIfPresent(TradeOrderDO::getUserId, userIds)
                .eqIfPresent(TradeOrderDO::getType, reqVO.getType())
                .eqIfPresent(TradeOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TradeOrderDO::getPayChannelCode, reqVO.getPayChannelCode())
                .eqIfPresent(TradeOrderDO::getTerminal, reqVO.getTerminal())
                .eqIfPresent(TradeOrderDO::getLogisticsId, reqVO.getLogisticsId())
                .inIfPresent(TradeOrderDO::getPickUpStoreId, reqVO.getPickUpStoreIds())
                .likeIfPresent(TradeOrderDO::getPickUpVerifyCode, reqVO.getPickUpVerifyCode())
                .betweenIfPresent(TradeOrderDO::getCreateTime, reqVO.getCreateTime())
                .groupBy(TradeOrderDO::getRefundStatus)); // 按售后状态分组
    }

    default PageResult<TradeOrderDO> selectPage(AppTradeOrderPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TradeOrderDO>()
                .eq(TradeOrderDO::getUserId, userId)
                .eqIfPresent(TradeOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TradeOrderDO::getCommentStatus, reqVO.getCommentStatus())
                .orderByDesc(TradeOrderDO::getId)); // TODO 芋艿：未来不同的 status，不同的排序
    }

    default Long selectCountByUserIdAndStatus(Long userId, Integer status, Boolean commentStatus) {
        return selectCount(new LambdaQueryWrapperX<TradeOrderDO>()
                .eq(TradeOrderDO::getUserId, userId)
                .eqIfPresent(TradeOrderDO::getStatus, status)
                .eqIfPresent(TradeOrderDO::getCommentStatus, commentStatus));
    }

    default TradeOrderDO selectOrderByIdAndUserId(Long orderId, Long loginUserId) {
        return selectOne(new LambdaQueryWrapperX<TradeOrderDO>()
                .eq(TradeOrderDO::getId, orderId)
                .eq(TradeOrderDO::getUserId, loginUserId));
    }

    default List<TradeOrderDO> selectListByStatusAndCreateTimeLt(Integer status, LocalDateTime createTime) {
        return selectList(new LambdaUpdateWrapper<TradeOrderDO>()
                .eq(TradeOrderDO::getStatus, status)
                .lt(TradeOrderDO::getCreateTime, createTime));
    }

    default List<TradeOrderDO> selectListByStatusAndDeliveryTimeLt(Integer status, LocalDateTime deliveryTime) {
        return selectList(new LambdaUpdateWrapper<TradeOrderDO>()
                .eq(TradeOrderDO::getStatus, status)
                .lt(TradeOrderDO::getDeliveryTime, deliveryTime));
    }

    default List<TradeOrderDO> selectListByStatusAndReceiveTimeLt(Integer status, LocalDateTime receive,
                                                                  Boolean commentStatus) {
        return selectList(new LambdaUpdateWrapper<TradeOrderDO>()
                .eq(TradeOrderDO::getStatus, status)
                .lt(TradeOrderDO::getReceiveTime, receive)
                .eq(TradeOrderDO::getCommentStatus, commentStatus));
    }

    default List<TradeOrderDO> selectListByUserIdAndSeckillActivityId(Long userId, Long seckillActivityId) {
        return selectList(new LambdaUpdateWrapper<>(TradeOrderDO.class)
                .eq(TradeOrderDO::getUserId, userId)
                .eq(TradeOrderDO::getSeckillActivityId, seckillActivityId));
    }

    default TradeOrderDO selectOneByPickUpVerifyCode(String pickUpVerifyCode) {
        return selectOne(TradeOrderDO::getPickUpVerifyCode, pickUpVerifyCode);
    }

    default TradeOrderDO selectByUserIdAndCombinationActivityIdAndStatus(Long userId, Long combinationActivityId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<TradeOrderDO>()
                .eq(TradeOrderDO::getUserId, userId)
                .eq(TradeOrderDO::getStatus, status)
                .eq(TradeOrderDO::getCombinationActivityId, combinationActivityId)
        );
    }

}
