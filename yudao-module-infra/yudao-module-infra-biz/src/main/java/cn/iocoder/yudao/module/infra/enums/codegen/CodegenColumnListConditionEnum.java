package cn.iocoder.yudao.module.infra.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成器的字段过滤条件枚举
 */
@AllArgsConstructor
@Getter
public enum CodegenColumnListConditionEnum {

    EQ("="),
    NE("!="),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    LIKE("LIKE"),
    BETWEEN("BETWEEN");

    /**
     * 条件
     */
    private final String condition;

}
