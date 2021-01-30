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
public enum ToolCodeGenTemplateTypeEnum {

    CRUD(1), // 基础 CRUD
    TREE(2), // 树形 CRUD
    SUB(3) // 子表 CRUD
    ;

    /**
     * 类型
     */
    private final Integer type;

}
