package cn.iocoder.yudao.module.crm.controller.admin.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - CRM 数据权限 Response VO")
@Data
public class CrmPermissionRespVO extends CrmPermissionBaseVO {

    @Schema(description = "数据权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String nickname;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
    private String deptName;

    @Schema(description = "岗位名称数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[BOOS,经理]")
    private Set<String> postNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-01-01 00:00:00")
    private LocalDateTime createTime;

}
