package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识库文档修改 Request VO")
@Data
public class PmsKnowledgeDocumentUpdateReqVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文档编号不能为空")
    private Long id;

    @Schema(description = "文档标题", example = "产品说明")
    @Size(max = 255, message = "文档标题不能超过 255 个字符")
    private String title;

    @Schema(description = "文档内容或文件地址")
    private String content;

    @Schema(description = "标签编号列表；传空集合表示清空标签")
    private List<Long> labelIds;

    @Schema(description = "文件类型", example = "pdf")
    @Size(max = 50, message = "文件类型不能超过 50 个字符")
    private String fileType;

    @Schema(description = "文件大小，单位：字节", example = "102400")
    @Min(value = 0, message = "文件大小不能小于 0")
    @Max(value = 104857600, message = "文件大小不能超过 100 MB")
    private Long fileSize;

}
