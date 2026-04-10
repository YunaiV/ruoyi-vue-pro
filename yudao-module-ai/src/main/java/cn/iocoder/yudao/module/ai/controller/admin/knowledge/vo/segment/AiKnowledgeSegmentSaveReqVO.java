package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - AI 新增/修改知识库段落 request VO")
@Data
public class AiKnowledgeSegmentSaveReqVO {

    @Schema(description = "编号", example = "24790")
    private Long id;

    @Schema(description = "知识库文档编号", example = "1024")
    private Long documentId;

    @Schema(description = "切片内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 开发手册")
    @NotEmpty(message = "切片内容不能为空")
    private String content;

}
