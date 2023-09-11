package cn.iocoder.yudao.module.trade.api.brokerage;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.trade.api.brokerage.dto.BrokerageUserDTO;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
     * 【会员】绑定推广员
     *
     * @param userId       用户编号
     * @param bindUserId   推广员编号
     * @param registerTime 用户注册时间
     * @return 是否绑定
     */
    default boolean bindUser(@NotNull Long userId, @NotNull Long bindUserId, @NotNull LocalDateTime registerTime) {
        // 注册时间在30秒内的，都算新用户
        boolean isNewUser = LocalDateTimeUtils.afterNow(registerTime.minusSeconds(30));
        return bindUser(userId, bindUserId, isNewUser);
    }

    /**
     * 绑定推广员
     *
     * @param userId     用户编号
     * @param bindUserId 推广员编号
     * @param isNewUser  是否为新用户
     * @return 是否绑定
     */
    boolean bindUser(@NotNull Long userId, @NotNull Long bindUserId, @NotNull Boolean isNewUser);
}
