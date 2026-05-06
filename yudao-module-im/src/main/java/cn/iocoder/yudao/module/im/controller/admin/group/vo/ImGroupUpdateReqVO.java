package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupJoinTypeEnum;
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

    @Schema(description = "加群方式", example = "1")
    @InEnum(ImGroupJoinTypeEnum.class)
    private Integer joinType; // 参见 ImGroupJoinTypeEnum 枚举类

}
