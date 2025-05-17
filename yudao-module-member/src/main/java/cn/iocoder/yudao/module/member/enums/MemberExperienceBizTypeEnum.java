package cn.iocoder.yudao.module.member.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 会员经验 - 业务类型
 *
 * @author owen
 */
@Getter
@AllArgsConstructor
public enum MemberExperienceBizTypeEnum {

    /**
     * 管理员调整、邀请新用户、下单、退单、签到、抽奖
     */
    ADMIN(0, "管理员调整", "管理员调整获得 {} 经验", true),
    INVITE_REGISTER(1, "邀新奖励", "邀请好友获得 {} 经验", true),
    SIGN_IN(4, "签到奖励", "签到获得 {} 经验", true),
    LOTTERY(5, "抽奖奖励", "抽奖获得 {} 经验", true),
    ORDER_GIVE(11, "下单奖励", "下单获得 {} 经验", true),
    ORDER_GIVE_CANCEL(12, "下单奖励（整单取消）", "取消订单获得 {} 经验", false), // ORDER_GIVE 的取消
    ORDER_GIVE_CANCEL_ITEM(13, "下单奖励（单个退款）", "退款订单获得 {} 经验", false), // ORDER_GIVE 的取消
    ;

    /**
     * 业务类型
     */
    private final int type;
    /**
     * 标题
     */
    private final String title;
    /**
     * 描述
     */
    private final String description;
    /**
     * 是否为扣减积分
     */
    private final boolean add;

    public static MemberExperienceBizTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(MemberExperienceBizTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }
}
