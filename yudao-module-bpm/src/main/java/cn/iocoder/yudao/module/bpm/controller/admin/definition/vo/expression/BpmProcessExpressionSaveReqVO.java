package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.expression;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - BPM 流程表达式新增/修改 Request VO")
@Data
public class BpmProcessExpressionSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3870")
    private Long id;

    @Schema(description = "表达式名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "表达式名字不能为空")
    private String name;

    @Schema(description = "表达式状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "表达式状态不能为空")
    private Integer status;

    @Schema(description = "表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "表达式不能为空")
    private String expression;

}