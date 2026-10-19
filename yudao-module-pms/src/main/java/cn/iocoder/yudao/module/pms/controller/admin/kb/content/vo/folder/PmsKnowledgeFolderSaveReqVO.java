package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库文件夹新增/修改 Request VO")
@Data
public class PmsKnowledgeFolderSaveReqVO {

    @Schema(description = "文件夹编号", example = "1024")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "知识库编号不能为空")
    private Long libraryId;

    @Schema(description = "父文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "父文件夹编号不能为空")
    private Long parentId;

    @Schema(description = "文件夹标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品文档")
    @NotBlank(message = "文件夹标题不能为空")
    @Size(max = 255, message = "文件夹标题不能超过 255 个字符")
    private String title;

}
