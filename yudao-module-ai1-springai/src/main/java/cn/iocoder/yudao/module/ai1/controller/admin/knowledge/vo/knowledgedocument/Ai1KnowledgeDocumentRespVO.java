package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 知识文档 Response VO")
@Data
public class Ai1KnowledgeDocumentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long knowledgeBaseId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体.txt")
    private String name;

    @Schema(description = "内容；分页列表不返回，编辑时通过获取接口加载", example = "三体舰队距离地球还有四光年")
    private String content;

    @Schema(description = "内容长度，单位：字符", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer contentLength;

    @Schema(description = "分片数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer chunkCount;

    @Schema(description = "向量化状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
