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
    ORDER(2, "下单奖励", "下单获得 {} 经验", true),
    REFUND(3, "退单扣除", "退单获得 {} 经验", false),
    SIGN_IN(4, "签到奖励", "签到获得 {} 经验", true),
    LOTTERY(5, "抽奖奖励", "抽奖获得 {} 经验", true),
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
