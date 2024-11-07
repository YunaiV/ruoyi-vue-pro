package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - AI 更新 知识库-段落 request VO")
@Data
public class AiKnowledgeSegmentUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long id;

    @Schema(description = "切片内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 开发手册")
    private String content;

}
