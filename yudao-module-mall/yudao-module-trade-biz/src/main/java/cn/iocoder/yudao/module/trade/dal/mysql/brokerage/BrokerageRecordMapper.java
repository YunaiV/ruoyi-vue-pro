package cn.iocoder.yudao.module.trade.dal.mysql.brokerage;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryRespBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 佣金记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageRecordMapper extends BaseMapperX<BrokerageRecordDO> {

    default PageResult<BrokerageRecordDO> selectPage(BrokerageRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageRecordDO>()
                .eqIfPresent(BrokerageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(BrokerageRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(BrokerageRecordDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BrokerageRecordDO::getSourceUserLevel, reqVO.getSourceUserLevel())
                .betweenIfPresent(BrokerageRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BrokerageRecordDO::getId));
    }

    default List<BrokerageRecordDO> selectListByStatusAndUnfreezeTimeLt(Integer status, LocalDateTime unfreezeTime) {
        return selectList(new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getStatus, status)
                .lt(BrokerageRecordDO::getUnfreezeTime, unfreezeTime));
    }

    default int updateByIdAndStatus(Integer id, Integer status, BrokerageRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getId, id)
                .eq(BrokerageRecordDO::getStatus, status));
    }

    default BrokerageRecordDO selectByBizTypeAndBizIdAndUserId(Integer bizType, String bizId, Long userId) {
        return selectOne(BrokerageRecordDO::getBizType, bizType,
                BrokerageRecordDO::getBizId, bizId,
                BrokerageRecordDO::getUserId, userId);
    }

    default List<UserBrokerageSummaryRespBO> selectCountAndSumPriceByUserIdInAndBizTypeAndStatus(Collection<Long> userIds,
                                                                                                 Integer bizType,
                                                                                                 Integer status) {
        List<Map<String, Object>> list = selectMaps(MPJWrappers.lambdaJoin(BrokerageRecordDO.class)
                .select(BrokerageRecordDO::getUserId)
                .selectCount(BrokerageRecordDO::getId, UserBrokerageSummaryRespBO::getCount)
                .selectSum(BrokerageRecordDO::getPrice)
                .in(BrokerageRecordDO::getUserId, userIds)
                .eq(BrokerageRecordDO::getBizId, bizType)
                .eq(BrokerageRecordDO::getStatus, status)
                .groupBy(BrokerageRecordDO::getUserId)); // 按照 userId 聚合
        return BeanUtil.copyToList(list, UserBrokerageSummaryRespBO.class);
        // selectJoinList有BUG，会与租户插件冲突：解析SQL时，发生异常 https://gitee.com/best_handsome/mybatis-plus-join/issues/I84GYW
//            return selectJoinList(UserBrokerageSummaryBO.class, MPJWrappers.lambdaJoin(BrokerageRecordDO.class)
//                    .select(BrokerageRecordDO::getUserId)
//                    .selectCount(BrokerageRecordDO::getId, UserBrokerageSummaryBO::getCount)
//                    .selectSum(BrokerageRecordDO::getPrice)
//                    .in(BrokerageRecordDO::getUserId, userIds)
//                    .eq(BrokerageRecordDO::getBizId, bizType)
//                    .eq(BrokerageRecordDO::getStatus, status)
//                    .groupBy(BrokerageRecordDO::getUserId));
    }

    @Select("SELECT SUM(price) FROM trade_brokerage_record " +
            "WHERE user_id = #{userId} AND biz_type = #{bizType} AND status = #{status} " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} AND deleted = FALSE")
    Integer selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(@Param("userId") Long userId,
                                                                     @Param("bizType") Integer bizType,
                                                                     @Param("status") Integer status,
                                                                     @Param("beginTime") LocalDateTime beginTime,
                                                                     @Param("endTime") LocalDateTime endTime);

    @Select("SELECT user_id AS id, SUM(price) AS brokeragePrice FROM trade_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id " +
            "ORDER BY brokeragePrice DESC")
    IPage<AppBrokerageUserRankByPriceRespVO> selectSummaryPricePageGroupByUserId(IPage<?> page,
                                                                                 @Param("bizType") Integer bizType,
                                                                                 @Param("status") Integer status,
                                                                                 @Param("beginTime") LocalDateTime beginTime,
                                                                                 @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(1) FROM trade_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id HAVING SUM(price) > #{brokeragePrice}")
    Integer selectCountByPriceGt(@Param("brokeragePrice") Integer brokeragePrice,
                                 @Param("bizType") Integer bizType,
                                 @Param("status") Integer status,
                                 @Param("beginTime") LocalDateTime beginTime,
                                 @Param("endTime") LocalDateTime endTime);

}
