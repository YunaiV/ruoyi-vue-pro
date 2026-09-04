package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库文档标签新增/修改 Request VO")
@Data
public class PmsKnowledgeDocumentLabelSaveReqVO {

    @Schema(description = "文档标签编号", example = "1024")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品")
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 255, message = "标签名称不能超过 255 个字符")
    private String name;

    @Schema(description = "标签颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "#409EFF")
    @NotBlank(message = "标签颜色不能为空")
    @Size(max = 20, message = "标签颜色不能超过 20 个字符")
    private String color;

}
