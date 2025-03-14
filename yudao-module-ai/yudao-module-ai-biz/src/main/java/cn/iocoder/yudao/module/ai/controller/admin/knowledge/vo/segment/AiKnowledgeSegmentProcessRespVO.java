package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库段落向量进度 Response VO")
@Data
public class AiKnowledgeSegmentProcessRespVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long documentId;

    @Schema(description = "总段落数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long count;

    @Schema(description = "已向量化段落数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long embeddingCount;

}