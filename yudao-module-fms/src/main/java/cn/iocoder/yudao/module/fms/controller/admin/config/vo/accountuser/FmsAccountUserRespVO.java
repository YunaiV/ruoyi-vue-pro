package cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - FMS 账套用户 Response VO")
@Data
public class FmsAccountUserRespVO {

    @Schema(description = "后台用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道")
    private String nickname;

    @Schema(description = "部门名称", example = "财务部")
    private String deptName;

    @Schema(description = "手机号码", example = "15601691399")
    private String mobile;

    @Schema(description = "用户邮箱", example = "finance@example.com")
    private String email;

    @Schema(description = "用户状态", example = "0")
    private Integer status;

    @Schema(description = "是否默认账套", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean defaultStatus;

    @Schema(description = "是否账套创建人", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean founder;

    @Schema(description = "成员权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer level;

}
