package cn.iocoder.yudao.module.im.controller.admin.rtc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 创建通话 Request VO")
@Data
public class ImRtcCallCreateReqVO {

    @Schema(description = "会话类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会话类型不能为空")
    private Integer conversationType; // 参见 ImConversationTypeEnum 枚举类

    @Schema(description = "媒体类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "媒体类型不能为空")
    private Integer mediaType; // 参见 ImCallMediaTypeEnum 枚举类

    @Schema(description = "群编号；群聊场景必填", example = "2048")
    private Long groupId;

    @Schema(description = "被邀请的用户编号集合；私聊必传 1 个对端，群聊必传至少 1 人")
    private Set<Long> inviteeIds;

}
