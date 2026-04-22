package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 群更新 Request VO")
@Data
public class ImGroupUpdateReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1003")
    @NotNull(message = "群编号不能为空")
    private Long id;

    @Schema(description = "群名称", example = "芋道技术交流群")
    private String name;

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "群公告")
    private String notice;

    // ========== 以下字段来自 im_group_member 表（当前用户在该群的个人设置） ==========

    @Schema(description = "我在本群的昵称")
    private String displayUserName;

    @Schema(description = "群名备注")
    private String displayGroupName;

}
