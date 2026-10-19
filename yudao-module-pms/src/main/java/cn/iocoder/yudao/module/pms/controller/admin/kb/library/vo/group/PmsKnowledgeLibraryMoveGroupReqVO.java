package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库移动分组 Request VO")
@Data
public class PmsKnowledgeLibraryMoveGroupReqVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "知识库编号不能为空")
    private Long libraryId;

    @Schema(description = "目标分组编号；为空表示移出分组", example = "2048")
    private Long groupId;

}
