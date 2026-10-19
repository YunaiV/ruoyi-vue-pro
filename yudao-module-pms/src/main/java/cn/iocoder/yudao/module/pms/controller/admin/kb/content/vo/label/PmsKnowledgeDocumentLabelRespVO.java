package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 知识库文档标签 Response VO")
@Data
public class PmsKnowledgeDocumentLabelRespVO {

    @Schema(description = "文档标签编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品")
    private String name;

    @Schema(description = "标签颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "#409EFF")
    private String color;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
