package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "PMS 知识库文档公开分享 Response VO")
@Data
public class PmsKnowledgeDocumentSharePublicRespVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "文档标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
    private String title;

    @Schema(description = "文档内容或文件地址")
    private String content;

    @Schema(description = "文件临时预览地址")
    private String previewUrl;

    @Schema(description = "文档类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer type;

    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
