package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
    * 代码生成 Base VO，提供给添加、修改、详细的子 VO 使用
    * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
    */
@Data
public class CodegenTableBaseVO {

    @Schema(description = "生成场景,参见 CodegenSceneEnum 枚举", required = true, example = "1")
    @NotNull(message = "导入类型不能为空")
    private Integer scene;

    @Schema(description = "表名称", required = true, example = "yudao")
    @NotNull(message = "表名称不能为空")
    private String tableName;

    @Schema(description = "表描述", required = true, example = "芋道")
    @NotNull(message = "表描述不能为空")
    private String tableComment;

    @Schema(description = "备注", example = "我是备注")
    private String remark;

    @Schema(description = "模块名", required = true, example = "system")
    @NotNull(message = "模块名不能为空")
    private String moduleName;

    @Schema(description = "业务名", required = true, example = "codegen")
    @NotNull(message = "业务名不能为空")
    private String businessName;

    @Schema(description = "类名称", required = true, example = "CodegenTable")
    @NotNull(message = "类名称不能为空")
    private String className;

    @Schema(description = "类描述", required = true, example = "代码生成器的表定义")
    @NotNull(message = "类描述不能为空")
    private String classComment;

    @Schema(description = "作者", required = true, example = "芋道源码")
    @NotNull(message = "作者不能为空")
    private String author;

    @Schema(description = "模板类型，参见 CodegenTemplateTypeEnum 枚举", required = true, example = "1")
    @NotNull(message = "模板类型不能为空")
    private Integer templateType;

    @Schema(description = "父菜单编号", example = "1024")
    private Long parentMenuId;

}
