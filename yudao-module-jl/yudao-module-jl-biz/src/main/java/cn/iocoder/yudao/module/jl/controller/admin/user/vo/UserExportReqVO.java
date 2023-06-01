package cn.iocoder.yudao.module.jl.controller.admin.user.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 用户信息 Excel 导出 Request VO，参数和 UserPageReqVO 是一致的")
@Data
public class UserExportReqVO {

    @Schema(description = "用户ID", example = "22805")
    private Long id;

    @Schema(description = "用户账号", example = "王五")
    private String username;

    @Schema(description = "用户昵称", example = "赵六")
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

    @Schema(description = "帐号状态（0正常 1停用）", example = "2")
    private Byte status;

}
