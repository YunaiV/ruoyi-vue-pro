package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - AI1 知识库批量向量化 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ai1KnowledgeDocumentVectorizeRespVO {

    @Schema(description = "成功数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer successCount;

    @Schema(description = "失败数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer failureCount;

}
