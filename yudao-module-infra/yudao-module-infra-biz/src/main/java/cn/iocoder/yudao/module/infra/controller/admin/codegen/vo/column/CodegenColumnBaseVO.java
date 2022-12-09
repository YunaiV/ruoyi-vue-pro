package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 代码生成字段定义 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class CodegenColumnBaseVO {

    @Schema(title = "表编号", required = true, example = "1")
    @NotNull(message = "表编号不能为空")
    private Long tableId;

    @Schema(title = "字段名", required = true, example = "user_age")
    @NotNull(message = "字段名不能为空")
    private String columnName;

    @Schema(title = "字段类型", required = true, example = "int(11)")
    @NotNull(message = "字段类型不能为空")
    private String dataType;

    @Schema(title = "字段描述", required = true, example = "年龄")
    @NotNull(message = "字段描述不能为空")
    private String columnComment;

    @Schema(title = "是否允许为空", required = true, example = "true")
    @NotNull(message = "是否允许为空不能为空")
    private Boolean nullable;

    @Schema(title = "是否主键", required = true, example = "false")
    @NotNull(message = "是否主键不能为空")
    private Boolean primaryKey;

    @Schema(title = "是否自增", required = true, example = "true")
    @NotNull(message = "是否自增不能为空")
    private String autoIncrement;

    @Schema(title = "排序", required = true, example = "10")
    @NotNull(message = "排序不能为空")
    private Integer ordinalPosition;

    @Schema(title = "Java 属性类型", required = true, example = "userAge")
    @NotNull(message = "Java 属性类型不能为空")
    private String javaType;

    @Schema(title = "Java 属性名", required = true, example = "Integer")
    @NotNull(message = "Java 属性名不能为空")
    private String javaField;

    @Schema(title = "字典类型", example = "sys_gender")
    private String dictType;

    @Schema(title = "数据示例", example = "1024")
    private String example;

    @Schema(title = "是否为 Create 创建操作的字段", required = true, example = "true")
    @NotNull(message = "是否为 Create 创建操作的字段不能为空")
    private Boolean createOperation;

    @Schema(title = "是否为 Update 更新操作的字段", required = true, example = "false")
    @NotNull(message = "是否为 Update 更新操作的字段不能为空")
    private Boolean updateOperation;

    @Schema(title = "是否为 List 查询操作的字段", required = true, example = "true")
    @NotNull(message = "是否为 List 查询操作的字段不能为空")
    private Boolean listOperation;

    @Schema(title = "List 查询操作的条件类型", required = true, example = "LIKE", description = "参见 CodegenColumnListConditionEnum 枚举")
    @NotNull(message = "List 查询操作的条件类型不能为空")
    private String listOperationCondition;

    @Schema(title = "是否为 List 查询操作的返回字段", required = true, example = "true")
    @NotNull(message = "是否为 List 查询操作的返回字段不能为空")
    private Boolean listOperationResult;

    @Schema(title = "显示类型", required = true, example = "input")
    @NotNull(message = "显示类型不能为空")
    private String htmlType;

}
