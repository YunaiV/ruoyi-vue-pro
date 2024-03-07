package cn.iocoder.yudao.module.infra.enums.codegen;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 代码生成模板类型
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CodegenTemplateTypeEnum {

    ONE(1), // 单表（增删改查）
    TREE(2), // 树表（增删改查）

    MASTER_NORMAL(10), // 主子表 - 主表 - 普通模式
    MASTER_ERP(11), // 主子表 - 主表 - ERP 模式
    MASTER_INNER(12), // 主子表 - 主表 - 内嵌模式
    SUB(15), // 主子表 - 子表
    ;

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 是否为主表
     *
     * @param type 类型
     * @return 是否主表
     */
    public static boolean isMaster(Integer type) {
        return ObjectUtils.equalsAny(type,
                MASTER_NORMAL.type, MASTER_ERP.type, MASTER_INNER.type);
    }

    /**
     * 是否为树表
     *
     * @param type 类型
     * @return 是否树表
     */
    public static boolean isTree(Integer type) {
        return Objects.equals(type, TREE.type);
    }

}
