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
     *
     */
    SYSTEM(0, "系统"),
    ORDER(1, "订单"),
    SIGN_IN(2, "签到"),
    ;

    private final int value;
    private final String name;
}
