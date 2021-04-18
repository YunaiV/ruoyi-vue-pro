package cn.iocoder.dashboard.modules.system.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别的枚举值
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SysSexEnum {

    MALE(1), // 男
    FEMALE(2); // 女

    /**
     * 性别
     */
    private final Integer sex;

}
