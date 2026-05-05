package cn.iocoder.yudao.module.im.controller.admin.group.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 群成员更新 Request VO")
@Data
@Accessors(chain = true)
public class ImGroupMemberUpdateReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "群编号不能为空")
    private Long groupId;

    @Schema(description = "群内昵称", example = "芋头")
    private String displayUserName;

    @Schema(description = "群备注", example = "公司群")
    private String groupRemark;

    @Schema(description = "是否免打扰")
    private Boolean silent;

}
