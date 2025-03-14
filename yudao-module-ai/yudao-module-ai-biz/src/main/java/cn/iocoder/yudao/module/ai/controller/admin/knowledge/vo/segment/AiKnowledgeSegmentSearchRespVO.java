package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库段落搜索 Response VO")
@Data
public class AiKnowledgeSegmentSearchRespVO extends AiKnowledgeSegmentRespVO {

    @Schema(description = "文档名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品使用手册")
    private String documentName;

    @Schema(description = "相似度分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.95")
    private Double score;

}