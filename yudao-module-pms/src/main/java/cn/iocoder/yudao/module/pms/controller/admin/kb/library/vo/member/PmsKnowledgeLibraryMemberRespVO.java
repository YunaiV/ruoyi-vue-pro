package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库成员 Response VO")
@Data
public class PmsKnowledgeLibraryMemberRespVO {

    @Schema(description = "成员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "用户姓名", example = "芋道源码")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "父部门编号")
    private Long parentDeptId;

    @Schema(description = "父部门名称")
    private String parentDeptName;

    @Schema(description = "成员等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer level;

}
