package cn.iocoder.yudao.module.trade.dal.mysql.brokerage.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
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

}
