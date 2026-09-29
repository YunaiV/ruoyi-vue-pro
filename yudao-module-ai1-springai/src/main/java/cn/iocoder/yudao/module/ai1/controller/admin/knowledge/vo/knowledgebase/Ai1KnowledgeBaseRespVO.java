package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgebase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 知识库 Response VO")
@Data
public class Ai1KnowledgeBaseRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品手册")
    private String name;

    @Schema(description = "描述", example = "产品使用说明")
    private String description;

    @Schema(description = "向量化 Provider 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long embeddingProviderId;

    @Schema(description = "向量化 Provider 名称", example = "Ollama")
    private String embeddingProviderName;

    @Schema(description = "向量化模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long embeddingModelId;

    @Schema(description = "向量化模型名称", example = "bge-m3")
    private String embeddingModelName;

    @Schema(description = "分片大小，单位：字符", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer chunkSize;

    @Schema(description = "分片重叠，单位：字符", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer chunkOverlap;

    @Schema(description = "检索数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer topK;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
