package cn.iocoder.yudao.module.system.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色标识枚举
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    ADMIN("admin"), // 超级管理员
    ;

    /**
     * 角色编码
     */
    private final String key;

}
