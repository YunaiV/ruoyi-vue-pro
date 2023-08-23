package cn.iocoder.yudao.module.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    ADMIN(0, "管理员调整","管理员调整获得 {} 经验"),
    INVITE_REGISTER(1, "邀新奖励","邀请好友获得 {} 经验"),
    ORDER(2, "下单奖励", "下单获得 {} 经验"),
    REFUND(3, "退单扣除","退单获得 {} 经验"),
    SIGN_IN(4, "签到奖励","签到获得 {} 经验"),
    LOTTERY(5, "抽奖奖励","抽奖获得 {} 经验"),
    ;

    private final int type;
    private final String title;
    private final String description;

}
