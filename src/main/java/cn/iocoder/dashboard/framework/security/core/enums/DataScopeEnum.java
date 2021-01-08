package cn.iocoder.dashboard.framework.security.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据范围枚举类
 *
 * 用于实现数据级别的权限
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL(1), // 全部数据权限
    CUSTOM(2), // 自定数据权限
    DEPT(3), // 部门数据权限
    DEPT_AND_CHILD(4), // 部门及以下数据权限
    SELF(5); // 仅本人数据权限

    /**
     * 范围
     */
    private final Integer score;

}
