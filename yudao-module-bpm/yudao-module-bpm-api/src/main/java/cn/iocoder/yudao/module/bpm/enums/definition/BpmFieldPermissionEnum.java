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

    // TODO @jason：改成 WRITE、READ、NONE，更符合权限的感觉哈
    EDITABLE(1, "可编辑"),
    READONLY(2, "只读"),
    HIDE(3, "隐藏");

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
