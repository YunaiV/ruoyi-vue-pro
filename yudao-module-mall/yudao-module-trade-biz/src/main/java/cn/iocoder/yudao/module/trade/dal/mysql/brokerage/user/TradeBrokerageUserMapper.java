package cn.iocoder.yudao.module.trade.dal.mysql.brokerage.user;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分销用户 Mapper
 *
 * @author owen
 */
@Mapper
public interface TradeBrokerageUserMapper extends BaseMapperX<TradeBrokerageUserDO> {

    default PageResult<TradeBrokerageUserDO> selectPage(TradeBrokerageUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TradeBrokerageUserDO>()
                .eqIfPresent(TradeBrokerageUserDO::getBrokerageUserId, reqVO.getBrokerageUserId())
                .eqIfPresent(TradeBrokerageUserDO::getBrokerageEnabled, reqVO.getBrokerageEnabled())
                .betweenIfPresent(TradeBrokerageUserDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TradeBrokerageUserDO::getId));
    }

    /**
     * 更新用户可用佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（正数）
     */
    default void updateBrokeragePriceIncr(Long id, int incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<TradeBrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<TradeBrokerageUserDO>()
                .setSql(" brokerage_price = brokerage_price + " + incrCount)
                .eq(TradeBrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户可用佣金（减少）
     * 注意：理论上佣金可能已经提现，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（负数）
     */
    default void updateBrokeragePriceDecr(Long id, int incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<TradeBrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<TradeBrokerageUserDO>()
                .setSql(" brokerage_price = brokerage_price + " + incrCount) // 负数，所以使用 + 号
                .eq(TradeBrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 增加冻结佣金（正数）
     */
    default void updateFrozenBrokeragePriceIncr(Long id, int incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<TradeBrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<TradeBrokerageUserDO>()
                .setSql(" frozen_brokerage_price = frozen_brokerage_price + " + incrCount)
                .eq(TradeBrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（减少）
     * 注意：理论上冻结佣金可能已经解冻，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 减少冻结佣金（负数）
     */
    default void updateFrozenBrokeragePriceDecr(Long id, int incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<TradeBrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<TradeBrokerageUserDO>()
                .setSql(" frozen_brokerage_price = frozen_brokerage_price + " + incrCount) // 负数，所以使用 + 号
                .eq(TradeBrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（减少）, 更新用户佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 减少冻结佣金（负数）
     * @return 更新条数
     */
    default int updateFrozenBrokeragePriceDecrAndBrokeragePriceIncr(Long id, int incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<TradeBrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<TradeBrokerageUserDO>()
                .setSql(" frozen_brokerage_price = frozen_brokerage_price + " + incrCount + // 负数，所以使用 + 号
                        ", brokerage_price = brokerage_price + " + -incrCount) // 负数，所以使用 - 号
                .eq(TradeBrokerageUserDO::getId, id)
                .ge(TradeBrokerageUserDO::getFrozenBrokeragePrice, -incrCount); // cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

}
