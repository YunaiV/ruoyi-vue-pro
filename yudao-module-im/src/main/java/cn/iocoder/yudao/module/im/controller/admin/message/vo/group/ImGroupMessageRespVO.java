package cn.iocoder.yudao.module.im.controller.admin.message.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群聊消息 Response VO
 */
@Schema(description = "管理后台 - 群聊消息 Response VO")
@Data
public class ImGroupMessageRespVO {

    @Schema(description = "消息编号", example = "1")
    private Long id;

    @Schema(description = "客户端消息编号", example = "uuid-xxx")
    private String clientMessageId;

    @Schema(description = "发送人编号", example = "1")
    private Long senderId;

    @Schema(description = "群编号", example = "1")
    private Long groupId;

    @Schema(description = "消息类型", example = "0")
    private Integer type;

    @Schema(description = "消息内容", example = "{\"content\":\"你好\"}")
    private String content;

    @Schema(description = "消息状态", example = "0")
    private Integer status;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "@目标用户编号列表", example = "[1,2,3]")
    private List<Long> atUserIds;

    @Schema(description = "定向接收用户编号列表", example = "[1,2]")
    private List<Long> receiverUserIds;

    @Schema(description = "回执状态", example = "0")
    private Integer receiptStatus;

    @Schema(description = "已读人数（回执消息、且发送人为当前用户时有值）", example = "3")
    private Integer readCount;

}
