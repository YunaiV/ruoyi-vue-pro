package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识内容协作权限 Response VO")
@Data
public class PmsKnowledgeContentPermissionRespVO {

    @Schema(description = "协作权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long libraryId;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean openStatus;

    @Schema(description = "公开协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer openLevel;

    @Schema(description = "创建人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long creatorUserId;

    @Schema(description = "当前用户协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer currentUserLevel;

    @Schema(description = "协作者列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Member> members;

    @Schema(description = "管理后台 - PMS 知识内容协作者 Response VO")
    @Data
    public static class Member {

        @Schema(description = "协作者编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "用户编号", example = "1")
        private Long userId;

        @Schema(description = "用户昵称", example = "芋道")
        private String userName;

        @Schema(description = "部门编号", example = "100")
        private Long deptId;

        @Schema(description = "部门名称", example = "研发部")
        private String deptName;

        @Schema(description = "协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer level;

    }

}
