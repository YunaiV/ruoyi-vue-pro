package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库文档移动 Request VO")
@Data
public class PmsKnowledgeDocumentMoveReqVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文档编号不能为空")
    private Long id;

    @Schema(description = "目标知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "目标知识库编号不能为空")
    private Long targetLibraryId;

    @Schema(description = "目标文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "目标文件夹编号不能为空")
    private Long targetFolderId;

    @Schema(description = "目标父文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "目标父文档编号不能为空")
    private Long targetParentId;

}
