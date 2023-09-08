package cn.iocoder.yudao.module.trade.convert.brokerage.user;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import cn.iocoder.yudao.module.trade.service.brokerage.record.bo.UserBrokerageSummaryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 分销用户 Convert
 *
 * @author owen
 */
@Mapper
public interface TradeBrokerageUserConvert {

    TradeBrokerageUserConvert INSTANCE = Mappers.getMapper(TradeBrokerageUserConvert.class);

    TradeBrokerageUserRespVO convert(TradeBrokerageUserDO bean);

    List<TradeBrokerageUserRespVO> convertList(List<TradeBrokerageUserDO> list);

    PageResult<TradeBrokerageUserRespVO> convertPage(PageResult<TradeBrokerageUserDO> page);

    default PageResult<TradeBrokerageUserRespVO> convertPage(PageResult<TradeBrokerageUserDO> pageResult,
                                                             Map<Long, MemberUserRespDTO> userMap,
                                                             Map<Long, Long> brokerageUserCountMap,
                                                             Map<Long, UserBrokerageSummaryBO> userOrderSummaryMap) {
        PageResult<TradeBrokerageUserRespVO> result = convertPage(pageResult);
        for (TradeBrokerageUserRespVO vo : result.getList()) {
            // 用户信息
            Optional.ofNullable(userMap.get(vo.getId()))
                    .ifPresent(user -> {
                        vo.setNickname(user.getNickname());
                        vo.setAvatar(user.getAvatar());
                    });

            // 推广用户数量（一级）
            vo.setBrokerageUserCount(MapUtil.getInt(brokerageUserCountMap, vo.getId(), 0));

            Optional<UserBrokerageSummaryBO> orderSummaryOptional = Optional.ofNullable(userOrderSummaryMap.get(vo.getId()));
            // 推广订单数量
            vo.setBrokerageOrderCount(orderSummaryOptional.map(UserBrokerageSummaryBO::getCount).orElse(0));
            // 推广订单金额
            vo.setBrokerageOrderPrice(orderSummaryOptional.map(UserBrokerageSummaryBO::getPrice).orElse(0));

            // todo 已提现次数
            vo.setWithdrawCount(0);
            // todo 已提现金额
            vo.setWithdrawPrice(0);
        }
        return result;
    }
}
