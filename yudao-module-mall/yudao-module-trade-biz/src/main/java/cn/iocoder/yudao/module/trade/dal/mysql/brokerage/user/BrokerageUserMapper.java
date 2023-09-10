package cn.iocoder.yudao.module.trade.dal.mysql.brokerage.user;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageUserTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分销用户 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageUserMapper extends BaseMapperX<BrokerageUserDO> {

    default PageResult<BrokerageUserDO> selectPage(BrokerageUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageUserDO>()
                .eqIfPresent(BrokerageUserDO::getBrokerageEnabled, reqVO.getBrokerageEnabled())
                .betweenIfPresent(BrokerageUserDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(BrokerageUserDO::getBindUserTime, reqVO.getBindUserTime())
                .and(reqVO.getBindUserId() != null, w -> buildBindUserCondition(reqVO, w))
                .orderByDesc(BrokerageUserDO::getId));
    }

    static void buildBindUserCondition(BrokerageUserPageReqVO reqVO, LambdaQueryWrapper<BrokerageUserDO> wrapper) {
        if (BrokerageUserTypeEnum.FIRST.getType().equals(reqVO.getUserType())) {
            buildFirstBindUserCondition(reqVO.getBindUserId(), wrapper);
        } else if (BrokerageUserTypeEnum.SECOND.getType().equals(reqVO.getUserType())) {
            buildSecondBindUserCondition(reqVO.getBindUserId(), wrapper);
        } else {
            buildFirstBindUserCondition(reqVO.getBindUserId(), wrapper);
            buildSecondBindUserCondition(reqVO.getBindUserId(), wrapper.or());
        }
    }

    static void buildFirstBindUserCondition(Long bindUserId, LambdaQueryWrapper<BrokerageUserDO> wrapper) {
        wrapper.eq(BrokerageUserDO::getBindUserId, bindUserId);
    }

    static void buildSecondBindUserCondition(Long bindUserId, LambdaQueryWrapper<BrokerageUserDO> w) {
        w.inSql(BrokerageUserDO::getBindUserId, StrUtil.format("SELECT id FROM trade_brokerage_user WHERE bind_user_id = {}", bindUserId));
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
                .setSql(" price = price + " + incrCount)
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户可用佣金（减少）
     * 注意：理论上佣金可能已经提现，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（负数）
     */
    default void updatePriceDecr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" price = price + " + incrCount) // 负数，所以使用 + 号
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
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
                        ", price = price + " + -incrCount) // 负数，所以使用 - 号
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
}
