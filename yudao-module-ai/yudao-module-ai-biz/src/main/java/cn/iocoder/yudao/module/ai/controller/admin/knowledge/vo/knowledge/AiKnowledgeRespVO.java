package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - AI 知识库 Response VO")
@Data
public class AiKnowledgeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long id;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ruoyi-vue-pro 用户指南")
    private String name;

    @Schema(description = "知识库描述", example = "帮助你快速构建系统")
    private String description;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "14")
    private Long modelId;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "qwen-72b-chat")
    private String model;

}
