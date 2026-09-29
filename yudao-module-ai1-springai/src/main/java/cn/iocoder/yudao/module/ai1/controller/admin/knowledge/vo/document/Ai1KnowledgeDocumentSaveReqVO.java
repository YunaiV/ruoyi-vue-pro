package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI1 知识文档新增/修改 Request VO")
@Data
public class Ai1KnowledgeDocumentSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "知识库编号；修改时不允许变更", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeBaseId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体.txt")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体舰队距离地球还有四光年")
    @NotEmpty(message = "内容不能为空")
    private String content;

}
