package cn.iocoder.yudao.module.im.controller.admin.rtc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 通话中添加成员 Request VO")
@Data
public class ImRtcCallInviteMoreReqVO {

    @Schema(description = "LiveKit 房间名", requiredMode = Schema.RequiredMode.REQUIRED, example = "call_group_2048")
    @NotBlank(message = "房间名不能为空")
    private String roomName;

    // TODO @AI：targetIds？
    @Schema(description = "新邀请的用户编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请至少选择一位成员")
    private Set<Long> inviteeIds;

}
