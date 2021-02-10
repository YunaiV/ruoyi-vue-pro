package cn.iocoder.dashboard.modules.tool.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成模板类型
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum ToolCodegenTemplateTypeEnum {

    CRUD(1), // 单表（增删改查）
    TREE(2), // 树表（增删改查）
    // TODO 主子表，暂时不考虑支持。原因是较为灵活，形态较多，很难评估。
    SUB(3) // 主子表（增删改查）
    ;

    /**
     * 类型
     */
    private final Integer type;

}
