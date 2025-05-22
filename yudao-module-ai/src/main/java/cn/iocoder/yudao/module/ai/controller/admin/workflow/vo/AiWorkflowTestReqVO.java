package cn.iocoder.yudao.module.ai.controller.admin.workflow.vo;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI 工作流测试 Request VO")
@Data
public class AiWorkflowTestReqVO {

    @Schema(description = "工作流编号", example = "1024")
    private Long id;

    @Schema(description = "工作流模型", example = "{}")
    private String graph;

    @Schema(description = "参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private Map<String, Object> params;

    @AssertTrue(message = "工作流或模型，必须传递一个")
    public boolean isGraphValid() {
        return id != null || StrUtil.isNotEmpty(graph);
    }

}
