package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识库目录树 Response VO")
@Data
public class PmsKnowledgeTreeRespVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long libraryId;

    @Schema(description = "当前用户是否可编辑", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean writeStatus;

    @Schema(description = "当前用户是否可管理目录结构", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean manageStatus;

    @Schema(description = "根文件夹列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<FolderNode> folders;

    @Schema(description = "根文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DocumentNode> documents;

    @Schema(description = "管理后台 - PMS 知识库文件夹树节点 Response VO")
    @Data
    public static class FolderNode {

        @Schema(description = "文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "协作权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long permissionId;

        @Schema(description = "当前用户协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer currentUserLevel;

        @Schema(description = "父文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long parentId;

        @Schema(description = "文件夹标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品文档")
        private String title;

        @Schema(description = "子文件夹列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<FolderNode> children;

        @Schema(description = "文件夹下的文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<DocumentNode> documents;
    }

    @Schema(description = "管理后台 - PMS 知识库文档树节点 Response VO")
    @Data
    public static class DocumentNode {

        @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "协作权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long permissionId;

        @Schema(description = "当前用户协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer currentUserLevel;

        @Schema(description = "父文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long parentId;

        @Schema(description = "文档标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
        private String title;

        @Schema(description = "文档类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer type;

        @Schema(description = "文件类型", example = "pdf")
        private String fileType;

        @Schema(description = "子文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<DocumentNode> children;
    }

}
