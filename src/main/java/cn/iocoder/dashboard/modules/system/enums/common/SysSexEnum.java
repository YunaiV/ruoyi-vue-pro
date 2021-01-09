package cn.iocoder.dashboard.modules.system.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysSexEnum {

    MALE(1), // 男
    FEMALE(2); // 女

    private final Integer SEX;

}
