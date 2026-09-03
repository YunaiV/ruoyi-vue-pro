package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 知识库文件夹 Response VO")
@Data
public class PmsKnowledgeFolderRespVO {

    @Schema(description = "文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long libraryId;

    @Schema(description = "协作权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long permissionId;

    @Schema(description = "当前用户协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer currentUserLevel;

    @Schema(description = "父文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long parentId;

    @Schema(description = "文件夹标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品文档")
    private String title;

    @Schema(description = "直属子文件夹数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer childFolderCount;

    @Schema(description = "直属文档数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer documentCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "当前用户是否已关注", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean favoriteStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
