package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库文件夹移动 Request VO")
@Data
public class PmsKnowledgeFolderMoveReqVO {

    @Schema(description = "文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文件夹编号不能为空")
    private Long id;

    @Schema(description = "目标知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "目标知识库编号不能为空")
    private Long targetLibraryId;

    @Schema(description = "目标父文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "目标父文件夹编号不能为空")
    private Long targetParentId;

}
