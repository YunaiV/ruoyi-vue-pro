package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库文档新增 Request VO")
@Data
public class PmsKnowledgeDocumentCreateReqVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "知识库编号不能为空")
    private Long libraryId;

    @Schema(description = "文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文件夹编号不能为空")
    private Long folderId;

    @Schema(description = "父文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "父文档编号不能为空")
    private Long parentId;

    @Schema(description = "文档标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 255, message = "文档标题不能超过 255 个字符")
    private String title;

    @Schema(description = "文档类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "文档类型不能为空")
    @InEnum(PmsKnowledgeDocumentTypeEnum.class)
    private Integer type;

    @Schema(description = "文档内容或文件地址")
    private String content;

    @Schema(description = "文件类型", example = "pdf")
    @Size(max = 50, message = "文件类型不能超过 50 个字符")
    private String fileType;

    @Schema(description = "文件大小，单位：字节", example = "102400")
    @Min(value = 0, message = "文件大小不能小于 0")
    @Max(value = 104857600, message = "文件大小不能超过 100 MB")
    private Long fileSize;

}
