package cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI1 会话消息发送 Request VO")
@Data
public class Ai1SessionMessageSendReqVO {

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "会话编号不能为空")
    private Long sessionId;

    @Schema(description = "提问内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体舰队还有多久到达地球？")
    @NotBlank(message = "请输入内容")
    private String content;

}
