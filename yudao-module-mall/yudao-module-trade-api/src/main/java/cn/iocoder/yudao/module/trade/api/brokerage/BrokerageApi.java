package cn.iocoder.yudao.module.trade.api.brokerage;

import cn.iocoder.yudao.module.trade.api.brokerage.dto.BrokerageUserDTO;

/**
 * 分销 API 接口
 *
 * @author owen
 */
public interface BrokerageApi {

    /**
     * 获得分销用户
     *
     * @param userId 用户编号
     * @return 分销用户信息
     */
    BrokerageUserDTO getBrokerageUser(Long userId);

    /**
     * 绑定推广员
     *
     * @param userId     用户编号
     * @param bindUserId 推广员编号
     * @param isNewUser  是否为新用户
     * @return 是否绑定
     */
    boolean bindUser(Long userId, Long bindUserId, Boolean isNewUser);
}
