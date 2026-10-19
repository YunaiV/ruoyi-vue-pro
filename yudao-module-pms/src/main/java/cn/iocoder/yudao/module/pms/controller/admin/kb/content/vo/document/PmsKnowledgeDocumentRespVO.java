package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 知识库文档 Response VO")
@Data
public class PmsKnowledgeDocumentRespVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long libraryId;

    @Schema(description = "协作权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long permissionId;

    @Schema(description = "当前用户协作等级", example = "3")
    private Integer currentUserLevel;

    @Schema(description = "当前用户是否可下载", example = "true")
    private Boolean downloadStatus;

    @Schema(description = "知识库名称", example = "产品知识库")
    private String libraryName;

    @Schema(description = "文件夹编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long folderId;

    @Schema(description = "父文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long parentId;

    @Schema(description = "文档标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
    private String title;

    @Schema(description = "正文摘要")
    private String contentSummary;

    @Schema(description = "文档内容或文件地址")
    private String content;

    @Schema(description = "文件预览地址（下载权限不足时仍可用于在线预览）")
    private String previewUrl;

    @Schema(description = "文档类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer type;

    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    @Schema(description = "文件大小，单位：字节", example = "102400")
    private Long fileSize;

    @Schema(description = "文档状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "标签编号列表", example = "[1024, 2048]")
    private List<Long> labelIds;

    @Schema(description = "创建人用户编号", example = "1")
    private Long creatorUserId;

    @Schema(description = "创建人姓名", example = "芋道")
    private String creatorUserName;

    @Schema(description = "当前用户是否已关注")
    private Boolean favoriteStatus;

    @Schema(description = "当前用户是否已点赞")
    private Boolean likeStatus;

    @Schema(description = "点赞用户列表")
    private List<LikeUser> likeUsers;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "管理后台 - PMS 知识文档点赞用户 Response VO")
    @Data
    public static class LikeUser {

        @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "用户昵称", example = "芋道")
        private String nickname;

        @Schema(description = "用户头像")
        private String avatar;

    }

}
