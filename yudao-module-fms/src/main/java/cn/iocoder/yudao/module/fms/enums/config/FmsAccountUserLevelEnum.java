package cn.iocoder.yudao.module.fms.enums.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 账套用户权限级别枚举
 *
 * OWNER > WRITE > READ
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsAccountUserLevelEnum implements ArrayValuable<Integer> {

    OWNER(1, "主管"),
    READ(2, "查看者"),
    WRITE(3, "会计");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsAccountUserLevelEnum::getLevel).toArray(Integer[]::new);

    /**
     * 级别
     */
    private final Integer level;
    /**
     * 级别名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isOwner(Integer level) {
        return ObjUtil.equal(OWNER.level, level);
    }

    public static boolean isRead(Integer level) {
        return ObjUtil.equal(READ.level, level);
    }

    public static boolean isWrite(Integer level) {
        return ObjUtil.equal(WRITE.level, level);
    }

    /**
     * 判断是否可读
     *
     * @param level 权限级别
     * @return 是否可读
     */
    public static boolean isReadable(Integer level) {
        return valueOf(level) != null;
    }

    /**
     * 判断是否可写
     *
     * @param level 权限级别
     * @return 是否可写
     */
    public static boolean isWritable(Integer level) {
        return isOwner(level) || isWrite(level);
    }

    public static FmsAccountUserLevelEnum valueOf(Integer level) {
        return CollUtil.findOne(CollUtil.newArrayList(values()),
                item -> ObjUtil.equal(item.level, level));
    }

    public static String getNameByLevel(Integer level) {
        FmsAccountUserLevelEnum levelEnum = valueOf(level);
        return levelEnum == null ? null : levelEnum.getName();
    }

}
