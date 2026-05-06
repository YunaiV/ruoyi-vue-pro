package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupJoinTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 群创建 Request VO")
@Data
public class ImGroupCreateReqVO {

    @Schema(description = "群名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道技术交流群")
    @NotBlank(message = "群名称不能为空")
    private String name;

    @Schema(description = "初始成员用户编号列表（建群同时邀请的好友，不含创建者自己）", example = "[1024, 2048]")
    private List<Long> memberUserIds;

    @Schema(description = "加群方式；不传默认 0 自由进群", example = "0")
    @InEnum(ImGroupJoinTypeEnum.class)
    private Integer joinType; // 参见 ImGroupJoinTypeEnum 枚举类

}
