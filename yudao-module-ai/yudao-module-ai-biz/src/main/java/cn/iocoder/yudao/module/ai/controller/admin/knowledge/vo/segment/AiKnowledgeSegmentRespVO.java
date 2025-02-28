package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库文档分片 Response VO")
@Data
public class AiKnowledgeSegmentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long id;

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long documentId;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long knowledgeId;

    @Schema(description = "向量库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1858496a-1dde-4edf-a43e-0aed08f37f8c")
    private String vectorId;

    @Schema(description = "切片内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 开发手册")
    private String content;

    @Schema(description = "切片内容长度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer contentLength;

    @Schema(description = "token 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer tokens;

    @Schema(description = "召回次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer retrievalCount;

    @Schema(description = "文档状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

}
