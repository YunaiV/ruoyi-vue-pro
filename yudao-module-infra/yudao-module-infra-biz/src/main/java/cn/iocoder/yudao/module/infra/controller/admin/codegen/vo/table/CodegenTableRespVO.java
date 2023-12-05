package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 代码生成表定义 Response VO")
@Data
public class CodegenTableRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "生成场景，参见 CodegenSceneEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer scene;

    @Schema(description = "表名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String tableName;

    @Schema(description = "表描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String tableComment;

    @Schema(description = "备注", example = "我是备注")
    private String remark;

    @Schema(description = "模块名", requiredMode = Schema.RequiredMode.REQUIRED, example = "system")
    private String moduleName;

    @Schema(description = "业务名", requiredMode = Schema.RequiredMode.REQUIRED, example = "codegen")
    private String businessName;

    @Schema(description = "类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CodegenTable")
    private String className;

    @Schema(description = "类描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "代码生成器的表定义")
    private String classComment;

    @Schema(description = "作者", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String author;

    @Schema(description = "模板类型，参见 CodegenTemplateTypeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer templateType;

    @Schema(description = "前端类型，参见 CodegenFrontTypeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer frontType;

    @Schema(description = "父菜单编号", example = "1024")
    private Long parentMenuId;

    @Schema(description = "主表的编号", example = "2048")
    private Long masterTableId;
    @Schema(description = "子表关联主表的字段编号", example = "4096")
    private Long subJoinColumnId;
    @Schema(description = "主表与子表是否一对多", example = "4096")
    private Boolean subJoinMany;

    @Schema(description = "树表的父字段编号", example = "8192")
    private Long treeParentColumnId;
    @Schema(description = "树表的名字字段编号", example = "16384")
    private Long treeNameColumnId;

    @Schema(description = "主键编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer dataSourceConfigId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
