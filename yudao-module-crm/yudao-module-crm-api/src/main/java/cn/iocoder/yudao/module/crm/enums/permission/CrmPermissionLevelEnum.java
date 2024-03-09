package cn.iocoder.yudao.module.crm.enums.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 数据权限级别枚举
 *
 * OWNER > WRITE > READ
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum CrmPermissionLevelEnum implements IntArrayValuable {

    OWNER(1, "负责人"),
    READ(2, "只读"),
    WRITE(3, "读写");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmPermissionLevelEnum::getLevel).toArray();

    /**
     * 级别
     */
    private final Integer level;
    /**
     * 级别名称
     */
    private final String name;

    @Override
    public int[] array() {
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

    public static String getNameByLevel(Integer level) {
        CrmPermissionLevelEnum typeEnum = CollUtil.findOne(CollUtil.newArrayList(CrmPermissionLevelEnum.values()),
                item -> ObjUtil.equal(item.level, level));
        return typeEnum == null ? null : typeEnum.getName();
    }

}
