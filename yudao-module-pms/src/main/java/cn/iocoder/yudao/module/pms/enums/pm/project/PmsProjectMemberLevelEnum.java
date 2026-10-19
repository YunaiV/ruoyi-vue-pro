package cn.iocoder.yudao.module.pms.enums.pm.project;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * PMS 项目成员权限级别枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsProjectMemberLevelEnum implements ArrayValuable<Integer> {

    OWNER(1, "项目拥有者"),
    ADMIN(2, "项目管理员"),
    WRITE(3, "编辑"),
    READ(4, "只读");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsProjectMemberLevelEnum::getLevel).toArray(Integer[]::new);

    /**
     * 可以管理项目的成员级别
     */
    public static final List<Integer> MANAGER_LEVELS = Arrays.asList(OWNER.level, ADMIN.level);
    /**
     * 可以编辑项目业务数据的成员级别
     */
    public static final List<Integer> WRITABLE_LEVELS = Arrays.asList(OWNER.level, ADMIN.level, WRITE.level);

    /**
     * 权限级别
     */
    private final Integer level;
    /**
     * 权限级别名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isOwner(Integer level) {
        return ObjUtil.equal(OWNER.level, level);
    }

    public static boolean isAdmin(Integer level) {
        return ObjUtil.equal(ADMIN.level, level);
    }

    public static boolean isManager(Integer level) {
        return isOwner(level) || isAdmin(level);
    }

    public static boolean isWritable(Integer level) {
        return isManager(level) || ObjUtil.equal(WRITE.level, level);
    }

    public static boolean isReadable(Integer level) {
        return valueOf(level) != null;
    }

    public static PmsProjectMemberLevelEnum valueOf(Integer level) {
        return ArrayUtil.firstMatch(item -> ObjUtil.equal(item.level, level), values());
    }

}
