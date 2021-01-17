package cn.iocoder.dashboard.modules.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登陆结果的枚举类
 */
@Getter
@AllArgsConstructor
public enum SysLoginResultEnum {

    SUCCESS(0), // 成功
    ;

    /**
     * 结果
     */
    private final Integer result;

}
