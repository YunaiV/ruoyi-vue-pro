package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 知识交互对象 Response VO")
@Data
public class PmsKnowledgeInteractionItemRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer type;

    @Schema(description = "对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long entityId;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long libraryId;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品知识库")
    private String libraryName;

    @Schema(description = "对象名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
    private String name;

    @Schema(description = "知识库简介")
    private String description;

    @Schema(description = "文件夹编号", example = "1024")
    private Long folderId;

    @Schema(description = "文档编号", example = "1024")
    private Long documentId;

    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    @Schema(description = "文件大小，单位：字节")
    private Long fileSize;

    @Schema(description = "对象更新时间")
    private LocalDateTime targetUpdateTime;

    @Schema(description = "记录时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
