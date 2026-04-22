package cn.iocoder.yudao.module.im.controller.admin.group.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 群成员更新 Request VO")
@Data
public class ImGroupMemberUpdateReqVO {

    @Schema(description = "群成员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17071")
    @NotNull(message = "群成员编号不能为空")
    private Long id;

    @Schema(description = "是否免打扰")
    private Boolean muted;

}
