package cn.iocoder.yudao.module.crm.controller.admin.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - CRM 数据权限（团队成员） Response VO")
@Data
public class CrmPermissionRespVO extends CrmPermissionBaseVO {

    @Schema(description = "数据权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;

    // TODO @puhui999：搞到字典里；
    @Schema(description = "团队级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "负责人")
    private String permissionLevelName;

    // TODO @puhui999：deptId、postIds 是不是要提供中文名哈；
    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deptId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String nickname;

    @Schema(description = "岗位编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3]")
    private Set<Long> postIds;

}
