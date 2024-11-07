package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - AI 知识库段落召回 Request VO")
@Data
public class AiKnowledgeSegmentSearchReqVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long knowledgeId;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 学习路线")
    private String content;

}
