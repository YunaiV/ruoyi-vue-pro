package cn.iocoder.yudao.module.im.controller.admin.message.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - IM 频道消息拉取 Response VO")
@Data
public class ImChannelMessagePullRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9001")
    private Long id;

    @Schema(description = "频道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long channelId;

    @Schema(description = "素材编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long materialId;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "125")
    private Integer type;

    @Schema(description = "消息内容；payload JSON 快照", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "当前用户回执 / 已读态；按 Redis 读位置计算（已读 DONE，未读 PENDING）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer receiptStatus; // 参见 ImMessageReceiptStatusEnum 枚举类

    @Schema(description = "发送时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime sendTime;

}
