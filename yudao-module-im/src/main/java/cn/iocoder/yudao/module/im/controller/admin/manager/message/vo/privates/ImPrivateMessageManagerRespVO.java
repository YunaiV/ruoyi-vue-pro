package cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 私聊消息 Response VO")
@Data
public class ImPrivateMessageManagerRespVO {

    @Schema(description = "消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户端消息编号", example = "c-uuid-xxx")
    private String clientMessageId;

    @Schema(description = "发送人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long senderId;

    @Schema(description = "发送人昵称", example = "张三")
    private String senderNickname;

    @Schema(description = "接收人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long receiverId;

    @Schema(description = "接收人昵称", example = "李四")
    private String receiverNickname;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type; // 参见 ImMessageTypeEnum 枚举类

    @Schema(description = "消息内容（JSON 格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "消息状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status; // 参见 ImMessageStatusEnum 枚举类

    @Schema(description = "发送时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime sendTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
