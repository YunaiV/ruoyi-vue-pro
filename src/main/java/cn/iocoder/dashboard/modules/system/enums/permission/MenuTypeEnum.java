package cn.iocoder.dashboard.modules.system.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举类
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    DIR("M"), // 目录
    MENU("C"), // 菜单
    BUTTON("F") // 按钮
    ;

    /**
     * 类型
     */
    private final String type;

}
