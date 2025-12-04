package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 代码生成预览 Request VO")
@Data
public class CodegenPreviewReqVO {

    @Schema(description = "表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long tableId;

    @Schema(description = "忽略类名称重复问题，默认值 false，即不忽略。", example = "false")
    private Boolean ignoreDuplicatedClassName;

}
