package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - 群创建 Request VO")
@Data
public class ImGroupCreateReqVO {

    @Schema(description = "群名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道技术交流群")
    @NotBlank(message = "群名称不能为空")
    @Size(max = 64, message = "群名称长度不能超过 64")
    private String name;

    @Schema(description = "初始成员用户编号列表（建群同时邀请的好友，不含创建者自己）", example = "[1024, 2048]")
    private List<Long> memberUserIds;

    @Schema(description = "进群是否需群主 / 管理员审批；不传默认 false 自由进群", example = "false")
    private Boolean joinApproval;

}
