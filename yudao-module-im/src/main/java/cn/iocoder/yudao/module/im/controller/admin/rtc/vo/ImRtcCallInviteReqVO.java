package cn.iocoder.yudao.module.im.controller.admin.rtc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "管理后台 - 通话中追加邀请 Request VO；仅群通话可用")
@Data
public class ImRtcCallInviteReqVO {

    @Schema(description = "业务通话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "f47ac10b58cc4372a567")
    @NotBlank(message = "通话编号不能为空")
    private String room;

    @Schema(description = "新邀请的用户编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请至少选择一位成员")
    private Set<@NotNull(message = "被邀请用户编号不能为空") Long> inviteeIds;

}
