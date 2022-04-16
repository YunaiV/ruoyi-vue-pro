package cn.iocoder.yudao.module.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户收件地址的类型枚举
 */
@Getter
@AllArgsConstructor
public enum AddressTypeEnum {

    DEFAULT(1, "默认收件地址"),
    NORMAL(2, "普通收件地址"), // 即非默认收件地址

    ;

    private final Integer type;
    private final String desc;

}
