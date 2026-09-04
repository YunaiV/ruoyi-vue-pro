package cn.iocoder.yudao.module.pms.controller.admin.kb.recycle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库回收站内容预览 Response VO")
@Data
public class PmsKnowledgeRecyclePreviewRespVO {

    @Schema(description = "内容编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer type;

    @Schema(description = "内容名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
    private String name;

    @Schema(description = "文档正文或文件地址")
    private String content;

    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    @Schema(description = "文件大小，单位：字节", example = "102400")
    private Long fileSize;

}
