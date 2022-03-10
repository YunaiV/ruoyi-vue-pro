package cn.iocoder.yudao.module.infra.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成的导入类型
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CodegenImportTypeEnum {

    DB(1), // 从 information_schema 的 table 和 columns 表导入
    SQL(2); // 基于建表 SQL 语句导入

    /**
     * 类型
     */
    private final Integer type;

}
