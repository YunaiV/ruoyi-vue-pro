package cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 发送客服消息 Request VO")
@Data
public class AppKeFuMessageSendReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23202")
    private Long id;

    @Schema(description = "发送人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24571")
    private Long senderId;
    @Schema(description = "发送人类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer senderType;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "消息类型不能为空")
    private Integer contentType;
    @Schema(description = "消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息不能为空")
    private String content;

}