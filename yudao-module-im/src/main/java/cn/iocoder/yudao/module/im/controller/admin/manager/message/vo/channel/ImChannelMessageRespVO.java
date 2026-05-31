package cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IM 频道消息 Response VO")
@Data
public class ImChannelMessageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "频道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long channelId;

    @Schema(description = "频道名称（关联查询填充）")
    private String channelName;

    @Schema(description = "素材编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long materialId;

    @Schema(description = "素材标题（关联查询填充）")
    private String materialTitle;

    @Schema(description = "素材封面 URL（关联查询填充）")
    private String materialCoverUrl;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "125")
    private Integer type; // 参见 ImMessageTypeEnum 枚举类

    @Schema(description = "消息内容；payload JSON 快照")
    private String content;

    @Schema(description = "接收人编号列表；为空表示全员")
    private List<Long> receiverUserIds;

    @Schema(description = "发送时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime sendTime;

}
