package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - PMS 项目成员 Response VO")
@Data
public class PmsProjectMemberRespVO {

    @Schema(description = "后台用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "成员权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer level;

    @Schema(description = "是否项目创建人", example = "true")
    private Boolean creatorStatus;

}
