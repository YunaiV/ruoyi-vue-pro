package cn.iocoder.yudao.module.trade.dal.mysql.brokerage;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.user.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserChildSummaryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByUserCountRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 分销用户 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageUserMapper extends BaseMapperX<BrokerageUserDO> {

    default PageResult<BrokerageUserDO> selectPage(BrokerageUserPageReqVO reqVO, List<Long> ids) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageUserDO>()
                .inIfPresent(BrokerageUserDO::getId, ids)
                .eqIfPresent(BrokerageUserDO::getBrokerageEnabled, reqVO.getBrokerageEnabled())
                .betweenIfPresent(BrokerageUserDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(BrokerageUserDO::getBindUserTime, reqVO.getBindUserTime())
                .orderByDesc(BrokerageUserDO::getId));
    }

    /**
     * 更新用户可用佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（正数）
     */
    default void updatePriceIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" brokerage_price = brokerage_price + " + incrCount)
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户可用佣金（减少）
     * 注意：理论上佣金可能已经提现，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（负数）
     * @return 更新行数
     */
    default int updatePriceDecr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" brokerage_price = brokerage_price + " + incrCount) // 负数，所以使用 + 号
                .eq(BrokerageUserDO::getId, id);
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 增加冻结佣金（正数）
     */
    default void updateFrozenPriceIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" frozen_price = frozen_price + " + incrCount)
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（减少）
     * 注意：理论上冻结佣金可能已经解冻，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 减少冻结佣金（负数）
     */
    default void updateFrozenPriceDecr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" frozen_price = frozen_price + " + incrCount) // 负数，所以使用 + 号
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（减少）, 更新用户佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 减少冻结佣金（负数）
     * @return 更新条数
     */
    default int updateFrozenPriceDecrAndPriceIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" frozen_price = frozen_price + " + incrCount + // 负数，所以使用 + 号
                        ", brokerage_price = brokerage_price + " + -incrCount) // 负数，所以使用 - 号
                .eq(BrokerageUserDO::getId, id)
                .ge(BrokerageUserDO::getFrozenPrice, -incrCount); // cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

    default void updateBindUserIdAndBindUserTimeToNull(Long id) {
        update(null, new LambdaUpdateWrapper<BrokerageUserDO>()
                .eq(BrokerageUserDO::getId, id)
                .set(BrokerageUserDO::getBindUserId, null).set(BrokerageUserDO::getBindUserTime, null));
    }

    default void updateEnabledFalseAndBrokerageTimeToNull(Long id) {
        update(null, new LambdaUpdateWrapper<BrokerageUserDO>()
                .eq(BrokerageUserDO::getId, id)
                .set(BrokerageUserDO::getBrokerageEnabled, false).set(BrokerageUserDO::getBrokerageTime, null));
    }

    @Select("SELECT bind_user_id AS id, COUNT(1) AS brokerageUserCount FROM trade_brokerage_user " +
            "WHERE bind_user_id IS NOT NULL AND deleted = FALSE " +
            "AND bind_user_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY bind_user_id " +
            "ORDER BY brokerageUserCount DESC")
    IPage<AppBrokerageUserRankByUserCountRespVO> selectCountPageGroupByBindUserId(Page<?> page,
                                                                                  @Param("beginTime") LocalDateTime beginTime,
                                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 下级分销统计（分页）
     *
     * @param bizType      业务类型
     * @param status       状态
     * @param ids          用户编号列表
     * @param sortingField 排序字段
     * @return 下级分销统计分页列表
     */
    IPage<AppBrokerageUserChildSummaryRespVO> selectSummaryPageByUserId(Page<?> page,
                                                                        @Param("bizType") Integer bizType,
                                                                        @Param("status") Integer status,
                                                                        @Param("ids") Collection<Long> ids,
                                                                        @Param("sortingField") SortingField sortingField);

    /**
     * 获得被 bindUserIds 推广的用户编号数组
     *
     * @param bindUserIds 推广员编号数组
     * @return 用户编号数组
     */
    default List<Long> selectIdListByBindUserIdIn(Collection<Long> bindUserIds) {
        return Convert.toList(Long.class,
                selectObjs(new LambdaQueryWrapperX<BrokerageUserDO>()
                        .select(Collections.singletonList(BrokerageUserDO::getId)) // 只查询 id 字段，加速返回速度
                        .in(BrokerageUserDO::getBindUserId, bindUserIds)));
    }

}
