package cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 发送客服消息 Request VO")
@Data
public class KeFuMessageSendReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23202")
    private Long id;

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12580")
    @NotNull(message = "会话编号不能为空")
    private Long conversationId;

    @Schema(description = "发送人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24571")
    @NotNull(message = "发送人编号不能为空")
    private Long senderId;

    @Schema(description = "发送人类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发送人类型不能为空")
    private Integer senderType;

    @Schema(description = "接收人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29124")
    @NotNull(message = "接收人编号不能为空")
    private Long receiverId;

    @Schema(description = "接收人类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "接收人类型不能为空")
    private Integer receiverType;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "消息类型不能为空")
    private Integer contentType;

    @Schema(description = "消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息不能为空")
    private String content;

}