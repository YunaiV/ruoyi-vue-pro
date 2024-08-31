package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 知识库更新【我的】 Request VO")
@Data
public class AiKnowledgeUpdateMyReqVO {

    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1204")
    @NotNull(message = "知识库编号不能为空")
    private Long id;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    @NotBlank(message = "知识库名称不能为空")
    private String name;

    @Schema(description = "知识库描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    private String description;

    @Schema(description = "可见权限，只能选择哪些人可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1]")
    private List<Long> visibilityPermissions;

    @Schema(description = "嵌入模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "嵌入模型不能为空")
    private Long modelId;

}
