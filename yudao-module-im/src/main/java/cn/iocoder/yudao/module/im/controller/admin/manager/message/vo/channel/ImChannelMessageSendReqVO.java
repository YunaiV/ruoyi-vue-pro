package cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IM 频道消息推送 Request VO")
@Data
public class ImChannelMessageSendReqVO {

    @Schema(description = "素材编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "素材编号不能为空")
    private Long materialId;

    @Schema(description = "接收人编号列表；为空表示全员", example = "[1024, 2048]")
    private List<Long> receiverUserIds;

}
