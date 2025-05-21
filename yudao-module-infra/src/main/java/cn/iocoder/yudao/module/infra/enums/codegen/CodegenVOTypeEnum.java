package cn.iocoder.yudao.module.infra.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成的 VO 类型枚举
 *
 * 目前的作用：Controller 新增、修改、响应时，使用 VO 还是 DO
 * 注意：不包括 Controller 的分页参数！
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CodegenVOTypeEnum {

    VO(10, "VO"),
    DO(20, "DO");

    /**
     * 场景
     */
    private final Integer type;
    /**
     * 场景名
     */
    private final String name;

}
