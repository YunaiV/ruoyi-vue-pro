package cn.iocoder.yudao.module.system.enums.permission;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 数据范围枚举类
 *
 * 用于实现数据级别的权限
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum implements IntArrayValuable {

    ALL(1), // 全部数据权限

    DEPT_CUSTOM(2), // 指定部门数据权限
    DEPT_ONLY(3), // 部门数据权限
    DEPT_AND_CHILD(4), // 部门及以下数据权限

    SELF(5); // 仅本人数据权限

    /**
     * 范围
     */
    private final Integer scope;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DataScopeEnum::getScope).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
