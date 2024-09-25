package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库创建 Request VO")
@Data
public class AiKnowledgeCreateReqVO {

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ruoyi-vue-pro 用户指南")
    @NotBlank(message = "知识库名称不能为空")
    private String name;

    @Schema(description = "知识库描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "存储 ruoyi-vue-pro 操作文档")
    private String description;

    @Schema(description = "可见权限，只能选择哪些人可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    private String visibilityPermissions;

    @Schema(description = "嵌入模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "嵌入模型不能为空")
    private Long modelId;

    @Schema(description = "相似性阈值", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.5")
    @NotNull(message = "相似性阈值不能为空")
    private Double similarityThreshold;

    @Schema(description = "topK", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "topK 不能为空")
    private Integer topK;

}
