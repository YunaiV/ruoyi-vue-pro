package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 知识库 Response VO")
@Data
public class PmsKnowledgeLibraryRespVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品知识库")
    private String name;

    @Schema(description = "知识库简介")
    private String description;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean openStatus;

    @Schema(description = "知识库封面")
    private String coverUrl;

    @Schema(description = "创建人用户编号", example = "1")
    private Long creatorUserId;

    @Schema(description = "创建人姓名", example = "芋道源码")
    private String creatorUserName;

    @Schema(description = "成员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer memberCount;

    @Schema(description = "文档数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long documentCount;

    @Schema(description = "文件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long fileCount;

    @Schema(description = "当前用户是否可以编辑内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean writeStatus;

    @Schema(description = "当前用户是否为知识库管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean adminStatus;

    @Schema(description = "当前用户是否可以主动退出知识库", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean exitStatus;

    @Schema(description = "当前用户是否已关注", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean favoriteStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
