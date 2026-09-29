package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgebase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI1 知识库检索命中 Response VO")
@Data
public class Ai1KnowledgeSearchRespVO {

    @Schema(description = "分片文本", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体舰队距离地球还有四光年")
    private String text;

    @Schema(description = "文档编号", example = "1")
    private Long documentId;

    @Schema(description = "文档名称", example = "三体.txt")
    private String documentName;

    @Schema(description = "分片序号", example = "0")
    private Integer chunkIndex;

    @Schema(description = "相似度得分", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.86")
    private Double score;

}
