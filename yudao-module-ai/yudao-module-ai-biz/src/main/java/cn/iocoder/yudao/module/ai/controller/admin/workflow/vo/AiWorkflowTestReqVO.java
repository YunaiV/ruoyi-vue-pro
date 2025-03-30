package cn.iocoder.yudao.module.ai.controller.admin.workflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI 工作流测试 Request VO")
@Data
public class AiWorkflowTestReqVO {

    @Schema(description = "工作流模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @NotEmpty(message = "工作流模型不能为空")
    private String graph;

    @Schema(description = "参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private Map<String, Object> params;

}
