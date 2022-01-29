package cn.iocoder.yudao.module.system.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Menu 编号枚举
 */
@Getter
@AllArgsConstructor
public enum MenuIdEnum {

    /**
     * 根节点
     */
    ROOT(0L);

    private final Long id;

}
