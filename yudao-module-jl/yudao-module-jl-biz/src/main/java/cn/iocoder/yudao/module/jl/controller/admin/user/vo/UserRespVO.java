package cn.iocoder.yudao.module.jl.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRespVO extends UserBaseVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22805")
    private Long id;

    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "王五")
    private String username;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String nickname;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "部门ID", example = "25468")
    private Long deptId;

    @Schema(description = "岗位编号数组")
    private String postIds;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "用户性别")
    private Byte sex;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "帐号状态（0正常 1停用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Byte status;

}
