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

    // TODO @jason：这个顺序要不要改下，和页面保持一致；只读（1）、编辑（2）、隐藏（3）
    // @芋艿 我看钉钉页面的顺序 是 可编辑 只读 隐藏
    WRITE(1, "可编辑"),
    READ(2, "只读"),
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
