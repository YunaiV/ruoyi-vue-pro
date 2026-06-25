package cn.iocoder.yudao.module.im.enums.group;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * IM 群成员角色枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImGroupMemberRoleEnum implements ArrayValuable<Integer> {

    OWNER(1, "群主"),
    ADMIN(2, "管理员"),
    NORMAL(3, "普通成员");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImGroupMemberRoleEnum::getRole).toArray(Integer[]::new);

    /**
     * 角色
     */
    private final Integer role;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isOwner(Integer role) {
        return Objects.equals(OWNER.role, role);
    }

    public static boolean isAdmin(Integer role) {
        return Objects.equals(ADMIN.role, role);
    }

    public static boolean isOwnerOrAdmin(Integer role) {
        return isOwner(role) || isAdmin(role);
    }

}
