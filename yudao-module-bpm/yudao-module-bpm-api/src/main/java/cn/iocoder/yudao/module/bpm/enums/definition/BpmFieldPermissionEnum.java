package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 表单权限的枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmFieldPermissionEnum {

    READ(1, "只读"),
    WRITE(2, "可编辑"),
    NONE(3, "隐藏");

    /**
     * 权限
     */
    private final Integer permission;
    /**
     * 名字
     */
    private final String name;

    public static BpmFieldPermissionEnum valueOf(Integer permission) {
        return ArrayUtil.firstMatch(item -> item.getPermission().equals(permission), values());
    }

}
