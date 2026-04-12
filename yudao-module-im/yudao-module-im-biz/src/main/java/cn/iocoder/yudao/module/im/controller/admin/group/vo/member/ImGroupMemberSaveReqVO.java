package cn.iocoder.yudao.module.im.controller.admin.group.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 群成员新增/修改 Request VO")
@Data
public class ImGroupMemberSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17071")
    private Long id;

    @Schema(description = "群编号", example = "13279")
    private Long groupId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21730")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "是否免打扰")
    private Boolean muted;

}